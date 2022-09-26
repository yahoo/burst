/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.compile

import org.burstsys.felt.compile.artifact.{FeltArtifactKey, FeltArtifactTag}
import org.burstsys.felt.model.tree.code.FeltCode
import org.burstsys.vitals.errors.{VitalsException, _}

import java.io.{File, FileOutputStream}
import java.net.URL
import java.nio.file.{Files, Paths}
import java.util.jar.{Attributes, JarOutputStream}
import java.util.zip.ZipEntry
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.{Global, Settings}
import scala.util.{Failure, Success, Try}

/**
 * One of a fixed set of scala compiler instances that are managed in a pool.
 */
trait FeltCompiler extends Any {

  /**
   * this is used when code generating classes that are used by other code generated classes. They have to
   * not only be compiled, but made available in the compiler classpath for binding in subsequent compiles.
   * This is not that easy and involves creating temporary jar files and restarting the compilers. This
   * restarting is done via tracking of logical compiler versions. Tmp jar files URLs are added to the new
   * compiler version's classpaths.
   *
   * @param source
   * @return an array of classnames added classpath
   */
  def generatedSourceToTravelerClassNames(key: FeltArtifactKey, tag: FeltArtifactTag, source: FeltCode): Try[Array[String]]

  /**
   * This is used when code generating classes classes that are not used by other code generations but just
   * require instantiation and immediate application as an object instance.
   *
   * @param source
   * @return
   */
  def generatedSourceToSweepInstance(key: FeltArtifactKey, tag: FeltArtifactTag, source: FeltCode): Try[Array[Any]]

}

object FeltCompiler {
  def apply(version: Int): FeltCompiler = FeltCompilerContext(version)
}

private final case
class FeltCompilerContext(version: Int) extends FeltCompiler {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val offset: Int = 0

  private[this]
  val _target: FeltCompileDirectory = FeltCompileDirectory()

  private[this]
  val _settings = new Settings
  _settings.verbose.value = true
  _settings.bootclasspath.value = _scalaClassPath.mkString(File.pathSeparator)
  _settings.classpath.value = (_scalaClassPath ::: _feltFilteredClassPath ::: FeltCompileEngine.jarFileUrls.toList).mkString(File.pathSeparator)
  _settings.outputDirs.setSingleOutput(_target)
  //  _settings.usejavacp.value = true

  private[this]
  val _reporter = FeltCompileReporter(_settings, offset)

  private[this]
  val _global = {
    new Global(_settings, _reporter)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def generatedSourceToTravelerClassNames(key: FeltArtifactKey, tag: FeltArtifactTag, source: FeltCode): Try[Array[FeltClassName]] = {
    reset
    compileClassSourceToClassSpecs(tag, source) match {
      case Failure(t) =>
        Failure(t)
      case Success(classSpecs) =>
        addByteCodesToClassPath(key, tag, classSpecs, source)
        Success(classSpecs.map(_._1))
    }
  }

  override
  def generatedSourceToSweepInstance(key: FeltArtifactKey, tag: FeltArtifactTag, source: FeltCode): Try[Array[Any]] = {
    reset
    compileClassSourceToClassSpecs(tag, source) match {
      case Failure(t) =>
        Failure(t)
      case Success(byteCodes) =>
        Success(instantiateSpecListToClassLoader(key, tag, byteCodes, source))
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // internal
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  def reset: this.type = {
    _reporter.reset()
    _target.reset()
    this
  }

  /**
   *
   * @param tag common tag across all classes in speclist
   * @param specList
   * @return a set of instantiated classes from spec list
   */
  private
  def instantiateSpecListToClassLoader(key: FeltArtifactKey, tag: FeltArtifactTag, specList: FeltClassSpecList, source: FeltCode): Array[FeltArtifactInstance] = {
    if (FeltCompileEngine.generatedAllBindings) {
      val url = jarFromByteCode(specList, source)
      log debug s"Instantiate class at $url"
    }
    specList.foreach {
      case (className, bytecode) =>
        FeltCompileEngine.addToClassLoader(key, tag, className, bytecode)
    }
    specList.map[FeltArtifactInstance] {
      case (className, _) =>
        val clazz = FeltCompileEngine._runtimeClassLoader.findClass(className)
        if (clazz.getConstructors.length != 1 || clazz.getConstructors.head.getParameterCount > 0) {
          null.asInstanceOf[FeltArtifactInstance]
        } else {
          clazz.getDeclaredConstructor().newInstance()
        }
    }.filter(_ != null)
  }

  private
  def addByteCodesToClassPath(key: FeltArtifactKey, tag: FeltArtifactTag, classSpecs: FeltClassSpecList, source: FeltCode): Unit = {
    reset
    // write class to tmp jar file
    val url = jarFromByteCode(classSpecs, source)
    if (FeltCompileEngine.generatedAllBindings) {
      log debug s"Instantiate class at $url"
    }
    // add jar file to classpath addUrlToClassPath(url)
    addUrlToClassPath(url)
    classSpecs.foreach {
      case (className, bytecode) =>
        FeltCompileEngine.addToClassLoader(key, tag, className, bytecode)
    }
    FeltCompileEngine._versionClock.incrementAndGet
  }

  /**
   * given a tag that covers all classes, and a source compilation unit, return
   * all compiled classes as an array of {className, bytecode} tuples
   *
   * @param tag
   * @param compilationUnit
   * @return
   */
  private
  def compileClassSourceToClassSpecs(tag: FeltArtifactTag, compilationUnit: FeltCode): Try[FeltClassSpecList] = {
    try {
      reset
      val sourceFiles = List(new BatchSourceFile("<internal>", compilationUnit))
      (new _global.Run).compileSources(sourceFiles)
      _reporter.throwIfError()
      val bytecode = _target.bytecode
      log debug s"compiled classes ${bytecode.map(_._1).mkString(",")}  tag=$tag"
      Success(bytecode)
    } catch safely {
      case t: Throwable =>
        log error s"Compile error (classpath=${_global.classPath.asClassPathString})"
        Failure(t)
    }
  }

  private
  def jarFromByteCode(specList: FeltClassSpecList, source: FeltCode): URL = {

    val bindingsJarFolder = FeltCompiler synchronized {
      val bindingsJarFolder = FeltCompileEngine.generatedBindingsJarFolder
      if (!bindingsJarFolder.toFile.exists)
        Files.createDirectory(bindingsJarFolder)
      bindingsJarFolder
    }

    val topClassName = s"${specList.head._1}"
    val jarName = s"$topClassName.jar"
    val jarFile = Paths.get(bindingsJarFolder.toString, jarName).toFile
    if (!FeltCompileEngine.generatedAllBindings) {
      jarFile.deleteOnExit()
    }

    val fout = new FileOutputStream(jarFile)

    val manifest = new java.util.jar.Manifest()
    manifest.getMainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0")

    val jarOut = new JarOutputStream(fout, manifest)
    specList.foreach {
      case (name, bytes) =>
        val className = s"$name.class"
        jarOut.putNextEntry(new ZipEntry(className))
        jarOut.write(bytes)
        jarOut.closeEntry()
    }

    {
      jarOut.putNextEntry(new ZipEntry(s"$topClassName.scala"))
      jarOut.write(source.getBytes)
      jarOut.closeEntry()
    }

    jarOut.close()
    jarFile.toURI.toURL
  }

  private
  def addUrlToClassPath(urls: URL*): Unit = {
    new _global.Run //  force some initialization TODO necessary??
    _global.extendCompilerClassPath(urls: _*) // Add jars to compile-time classpath
    FeltCompileEngine.jarFileUrls ++= urls
  }

  private
  def _scalaClassPath: List[String] = _scalaCompilerPath ::: _scalaLibraryPath

  private
  def _feltFilteredClassPath: List[String] = {
    val fcl = _feltClassPath.filter(p => FeltCompileEngine.includeFilter.exists(p.contains(_)))
    fcl
  }

  private final lazy val names = List(
    "org.burstsys.felt.compile.FeltCompiler",
    "org.burstsys.vitals.VitalsService",
    "org.burstsys.brio.model.BrioPathBuilder",
    "org.burstsys.ginsu.functions.coerce.GinsuCoerceFunctions",
    "org.burstsys.fabric.execution.model.runtime.FabricRuntime",
    "org.burstsys.tesla.director.TeslaDirector",
    "org.burstsys.zap.cube2.ZapCube2",
    "org.burstsys.hydra.HydraService",
    "org.burstsys.hydra.sweep.HydraRuntime",
    "org.joda.time.DateTime",
    "com.esotericsoftware.kryo.KryoSerializable"
  )

  private
  def _feltClassPath: List[String] = {
    val tcl: ClassLoader = Thread.currentThread().getContextClassLoader
    val effectiveClassPath: mutable.Set[String] = mutable.Set()
    tcl.getResources("org/burstsys").asScala.map(_.getPath).foreach(p => effectiveClassPath.add(p.replace("org/burstsys", "")))
    for (n <- names) {
      Try(tcl.loadClass(n)) match {
        case Success(c) =>
          effectiveClassPath.add(c.getProtectionDomain.getCodeSource.getLocation.getPath)
        case Failure(e) =>
          log warn s"didn't find classpath $n: $e"
      }
    }
    effectiveClassPath.toList
  }

  private
  def _scalaCompilerPath: List[String] = try {
    classPathOfClass(classOf[IMain])
  } catch safely {
    case e: Exception =>
      throw VitalsException(s" Unable to load scala interpreter from classpath (scala-compiler jar is missing?)", e)
  }

  private
  def _scalaLibraryPath: List[String] = try {
    classPathOfClass(classOf[List[_]])
  } catch safely {
    case e: Exception =>
      throw VitalsException(s"Unable to load scala base object from classpath (scala-library jar is missing?)", e)
  }

  private
  def classPathOfClass(clazz: Class[_]): List[String] = {
    val l = List(clazz.getProtectionDomain.getCodeSource.getLocation.getPath)
    l
  }
}
