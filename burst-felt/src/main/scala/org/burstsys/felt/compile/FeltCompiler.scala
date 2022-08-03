/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.compile

import org.burstsys.felt.compile.artifact.FeltArtifactKey
import org.burstsys.felt.compile.artifact.FeltArtifactTag
import org.burstsys.felt.model.tree.code.FeltCode
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._

import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.util.Failure
import scala.util.Success
import scala.util.Try

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
  val _global = new Global(_settings, _reporter)

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
        addByteCodesToClassPath(key, tag, classSpecs)
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
        Success(instantiateSpecListToClassLoader(key, tag, byteCodes))
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
  def instantiateSpecListToClassLoader(key: FeltArtifactKey, tag: FeltArtifactTag, specList: FeltClassSpecList): Array[FeltArtifactInstance] = {
    specList.foreach {
      case (className, bytecode) =>
        FeltCompileEngine.addToClassLoader(key, tag, className, bytecode)
    }
    specList.map {
      case (className, bytecode) =>
        val clazz = FeltCompileEngine._runtimeClassLoader.findClass(className)
        if (clazz.getConstructors.length != 1 || clazz.getConstructors.head.getParameterCount > 0) {
          null
        } else {
          clazz.newInstance
        }
    }.filter(_ != null)
  }

  private
  def addByteCodesToClassPath(key: FeltArtifactKey, tag: FeltArtifactTag, classSpecs: FeltClassSpecList): Unit = {
    reset
    // write class to tmp jar file
    val url = jarFromByteCode(classSpecs)
    // add jar file to classpath
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
        Failure(t)
    }
  }

  private
  def jarFromByteCode(specList: FeltClassSpecList): URL = {

    val bindingsJarFolder = FeltCompiler synchronized {
      val bindingsJarFolder = FeltCompileEngine.generatedBindingsJarFolder
      if (!bindingsJarFolder.toFile.exists)
        Files.createDirectory(bindingsJarFolder)
      bindingsJarFolder
    }

    val jarName = s"${specList.head._1}.jar"
    val jarFile = Paths.get(bindingsJarFolder.toString, jarName).toFile
    jarFile.deleteOnExit()

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
    jarOut.close()
    jarFile.toURI.toURL
  }

  private
  def addUrlToClassPath(urls: URL*): Unit = {
    new _global.Run //  force some initialization TODO necessary??
    _global.extendCompilerClassPath(urls: _*) // Add jars to compile-time classpath
    FeltCompileEngine.jarFileUrls ++= urls
  }

  @scala.annotation.tailrec
  private
  def getClassLoaderClassPath(cl: ClassLoader, acc: List[List[String]] = List.empty): List[List[String]] = {
    val cp = cl match {
      case urlClassLoader: URLClassLoader => urlClassLoader.getURLs.filter(_.getProtocol == "file").
        map(u => new File(u.toURI).getPath).toList
      case _ => Nil
    }
    cl.getParent match {
      case null => (cp :: acc).reverse
      case parent => getClassLoaderClassPath(parent, cp :: acc)
    }
  }

  private
  def _scalaClassPath: List[String] = _scalaCompilerPath ::: _scalaLibraryPath

  private
  def _feltFilteredClassPath: List[String] = {
    _feltClassPath.filter(p => FeltCompileEngine.includeFilter.exists(p.contains(_)))
  }

  private
  def _feltClassPath: List[String] = {

    val classPaths: List[List[String]] = getClassLoaderClassPath(Thread.currentThread.getContextClassLoader)
    val rtClassPath = classPaths.head

    // if there's just one thing in the classpath, and it's a jar, assume an executable jar.
    val bundledClassPathJars = if (rtClassPath.size > 1 || !rtClassPath.head.endsWith(".jar")) { Nil } else {
      val jarPath = rtClassPath.head
      val relativeRoot = new File(jarPath).getParentFile
      Option(new JarFile(jarPath).getManifest)
        .flatMap(m => Option(m.getMainAttributes.getValue("Class-Path")))
        .map(cp => cp.split(" ").map { f => new File(relativeRoot, f).getAbsolutePath }.toList)
        .getOrElse(Nil)
    }
    val effectiveClassPath = rtClassPath ::: bundledClassPathJars ::: classPaths.tail.flatten
    log debug s"FeltCompiler effective classpath=$effectiveClassPath"
    effectiveClassPath
  }

  private
  def _scalaCompilerPath: List[String] = try {
    classPathOfClassInJar(classOf[IMain].getName)
  } catch safely {
    case e: Exception =>
      throw VitalsException(s" Unable to load scala interpreter from classpath (scala-compiler jar is missing?)", e)
  }

  private
  def _scalaLibraryPath: List[String] = try {
    classPathOfClassInJar(classOf[Any].getName)
  } catch safely {
    case e: Exception =>
      throw VitalsException(s"Unable to load scala base object from classpath (scala-library jar is missing?)", e)
  }

  /**
   * For a given FQ classname, trick the resource finder into telling us the containing jar.
   */
  private
  def classPathOfClassInJar(className: String): List[String] = {
    val resource = className.split('.').mkString("/", "/", ".class")
    val path = getClass.getResource(resource).getPath
    if (path.indexOf("file:") >= 0) {
      val indexOfFile = path.indexOf("file:") + 5
      val indexOfSeparator = path.lastIndexOf('!')
      List(path.substring(indexOfFile, indexOfSeparator))
    } else {
      require(path.endsWith(resource))
      List(path.substring(0, path.length - resource.length + 1))
    }
  }

}
