/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.compile

import org.burstsys.felt.FeltService
import org.burstsys.felt.compile.artifact.{FeltArtifactKey, FeltArtifactTag}
import org.burstsys.felt.configuration._
import org.burstsys.felt.model.schema.traveler.FeltTraveler
import org.burstsys.felt.model.tree.code.FeltCode
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsSingleton}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.file.removePathFilesAndDirsRecursively
import org.burstsys.vitals.reporter.instrument.prettyTimeFromNanos

import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.{ConcurrentHashMap, LinkedBlockingQueue}
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Try}
import scala.jdk.CollectionConverters._

/**
 * Manage a queue of versioned scala 'compiler' instances. This allows us to perform concurrent code
 * generations in the face of all sorts of challenges.
 */
object FeltCompileEngine extends VitalsService with FeltCompiler {

  override def modality: VitalsServiceModality = VitalsSingleton

  override def serviceName: String = s"felt-compiler"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _tagMap = new ConcurrentHashMap[FeltArtifactTag, ArrayBuffer[FeltClassName]].asScala

  /**
   * every time we code generate to a a class we want to use in subsequent code generations e.g.
   * [[FeltTraveler]] instances, we bump the version so that we can get all new
   * compiler instances with the new classpath. Supposedly this can be done via the compiler itself but this is a simpler
   * and more deterministic way to go.
   */
  private[compile]
  val _versionClock = new AtomicInteger(1)

  private[compile]
  val generatedAllBindings: Boolean = {
    (System.getProperty("burst.compile.jar.tmpdir") != null) &&
      System.getProperty("burst.compile.jar.tmpdir").trim.nonEmpty
  }

  /**
   * The folder where we place code generated types (jars) that we want to import into subsequent code generations
   */
  private[compile]
  val generatedBindingsJarFolder = {
    if (generatedAllBindings) {
      Paths.get(System.getProperty("burst.compile.jar.tmpdir"), "burst-felt-code")
    } else {
      Paths.get(System.getProperty("java.io.tmpdir"), "burst-felt-code")
    }
  }

  /**
   * we store the set of code generations to classpath jar URLs here...
   */
  private[compile]
  val jarFileUrls = new mutable.HashSet[URL]

  /**
   * we grab the entire classpath we have been given and prune it down to necessary and sufficient dependencies
   * for code generation. This is a simplified pattern match for doing the filtering.
   */
  private[compile]
  val includeFilter = List(
    "jre/lib/rt/jar",
    "scala-library",
    "burst-vitals",
    "burst-brio",
    "burst-tesla",
    "burst-ginsu",
    "burst-felt",
    "burst-fabric-net",
    "burst-fabric-wave",
    "burst-zap",
    "burst-hydra",
    "kryo",
    "reflectasm",
    "minlog",
    "joda",
    "trove4j",
    "objenesis"
  )

  /////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Queuing
  /////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _compilerQueue = new LinkedBlockingQueue[FeltCompilerContext]

  private[compile]
  val _runtimeClassLoader = new FeltClassLoader(Thread.currentThread.getContextClassLoader)

  /////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    synchronized {
      ensureNotRunning
      log info startingMessage
      val start = System.nanoTime
      removePathFilesAndDirsRecursively(generatedBindingsJarFolder)
      for (i <- 0 until burstFeltCompileThreadsProperty.get) {
        _compilerQueue put FeltCompilerContext(_versionClock.get)
      }
      log debug s"$serviceName start up took ${prettyTimeFromNanos(System.nanoTime - start)} with ${burstFeltCompileThreadsProperty.get} worker(s)"
      markRunning
    }
    this
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      log info stoppingMessage
      _compilerQueue.clear()
      markNotRunning
    }
    this
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def classLoader: FeltClassLoader = _runtimeClassLoader

  def deleteFromClassLoaderByTag(key: FeltArtifactKey, tag: FeltArtifactTag):Unit = {
    log debug s"FELT_COMPILE_ENGINE_DELETE_FROM_CLASSLOADER(key='$key', tag='$tag'"
    _tagMap.getOrElseUpdate(tag, new ArrayBuffer[FeltClassName]) foreach {
      className =>
        classLoader.removeClassesByName(className)
        FeltService.onFeltDeleteFromClassLoader(key, tag, className)
    }
  }

  def addToClassLoader(key: FeltArtifactKey, tag: FeltArtifactTag, className: FeltClassName, bytecode: FeltByteCode):Unit = {
    log debug s"FELT_COMPILE_ENGINE_ADD_TO_CLASSLOADER(key='$key', tag='$tag', className='$className', bytesize=${bytecode.length}"
    classLoader.addClassByteCode(className, bytecode)
    FeltService.onFeltAddToClassLoader(key, tag, className, bytecode.length)
    _tagMap.getOrElseUpdate(tag, new ArrayBuffer[FeltClassName]) += className
  }

  override
  def generatedSourceToTravelerClassNames(key: FeltArtifactKey, tag: FeltArtifactTag, source: FeltCode): Try[Array[FeltClassName]] = {
    startIfNotAlreadyStarted
    try {
      val compiler = takeCompiler
      val oldLoader = Thread.currentThread.getContextClassLoader
      // TODO we need to allocate a new class loader every once in a while to allow for GC of classes as they LRU out
      Thread.currentThread.setContextClassLoader(FeltCompileEngine._runtimeClassLoader)
      try {
        compiler.generatedSourceToTravelerClassNames(key, tag, source)
      } finally {
        releaseCompiler(compiler)
        Thread.currentThread.setContextClassLoader(oldLoader)
      }
    } catch safely {
      case t: Throwable =>
        Failure(t)
    }
  }

  override
  def generatedSourceToSweepInstance(key: FeltArtifactKey, tag: FeltArtifactTag, source: FeltCode): Try[Array[Any]] = {
    startIfNotAlreadyStarted
    try {
      val compiler = takeCompiler
      val oldLoader = Thread.currentThread.getContextClassLoader
      // TODO we need to allocate a new class loader every once in a while to allow for GC of classes as they LRU out
      Thread.currentThread.setContextClassLoader(FeltCompileEngine._runtimeClassLoader)
      try {
        compiler.generatedSourceToSweepInstance(key, tag, source)
      } finally {
        releaseCompiler(compiler)
        Thread.currentThread.setContextClassLoader(oldLoader)
      }
    } catch safely {
      case t: Throwable =>
        Failure(t)
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////
  // internal
  /////////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  def takeCompiler: FeltCompilerContext = {
    ensureRunning
    val compiler = _compilerQueue.take
    if (compiler.version == _versionClock.get)
      compiler
    else {
      FeltCompilerContext(_versionClock.get)
    }
  }

  private
  def releaseCompiler(compiler: FeltCompilerContext): this.type = {
    ensureRunning
    _compilerQueue put compiler
    this
  }

}
