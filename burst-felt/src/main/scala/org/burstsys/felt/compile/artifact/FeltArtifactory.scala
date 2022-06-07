/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.compile.artifact

import org.burstsys.felt.FeltReporter
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.errors.safely

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BiFunction
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.language.postfixOps

/**
 * a factory/cache for felt artifacts
 *
 * @tparam INPUT  what is needed to generate the artifact output
 * @tparam OUTPUT the generated artifact that is cached
 */
abstract
class FeltArtifactory[INPUT <: Any, OUTPUT <: FeltArtifact[INPUT]] extends VitalsService {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _cache = new ConcurrentHashMap[String, OUTPUT]

  private[this]
  val _currentCount = new AtomicInteger()

  /**
   * this is the 'LRU cleaner' -- responsible for cleaning up (removing) artifacts that have not been
   * touched for more than the 'stale' duration (LRU).
   */
  private[this]
  def _lruCleaner(): Unit = {
    log info s"FELT_ARTIFACTORY_CLEANER service=$serviceName starting cleaner thread"
    while (true) {
      try {
        log info s"FELT_ARTIFACTORY_CLEANER service=$serviceName cleanDuration=$cleanDuration"
        Thread.sleep(cleanDuration.toMillis)

        val excessCount = {
          val tally = _currentCount.get - maxCount
          if (tally < 0) 0 else tally
        }

        // check to see if there is something to do
        if (excessCount != 0) {
          FeltReporter.recordSweepClean()

          log info s"FELT_ARTIFACTORY_CLEANER service=$serviceName cycle... excessCount=$excessCount, currentCount=${_currentCount.get} / maxCount=$maxCount"

          // now get a right sized set of cache entries to resolve size problem
          val lruEntries = _cache.entrySet().asScala
            .map(e => (e.getKey, e.getValue)).toList
            .sortBy(_._2.lastTouch).take(excessCount) // take excessCount of lastTouch (LRU)

          lruEntries foreach {
            case (key, artifact) =>
              // try for a lock, if you don't get it, then presence of an extent read lock means LRU semantics are violated anyway...
              if (artifact.tryWriteLock) {
                log debug s"FELT_ARTIFACTORY_CLEANER_TRY_WRITE_LOCK_SUCCESS"
                try {
                  log info s"FELT_ARTIFACTORY_CLEANER service=$serviceName DELETE (key='${artifact.key}', tag='${artifact.tag}')"
                  artifact.delete(artifact.key, artifact.tag)
                  _currentCount.decrementAndGet
                  FeltReporter.recordSweepCountDecrement()
                  _cache.remove(key)
                } finally artifact.releaseWriteLock()
              } else {
                log debug s"FELT_ARTIFACTORY_CLEANER_TRY_WRITE_LOCK_FAIL"
              }
          }
        }
      } catch safely {
        case t: Throwable =>
          log error s"FELT_ARTIFACTORY_CLEANER ERROR $t"
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def startingMessage: String = s"${super.startingMessage} lruEnabled=$lruEnabled cleanInterval=$cleanDuration"

  final override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    if (lruEnabled)
      TeslaRequestFuture(_lruCleaner())
    markRunning
  }

  final override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    _cache.clear()
    markNotRunning
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * How long to wait between loops of the LRU cleaner
   *
   * @return the duration the cleaner will wait
   */
  def cleanDuration: Duration = Duration.Inf

  /**
   * if true, then the LRU background cleaner will scan periodically. Also turns on READ LOCKS.
   *
   * @return if the cleaner is running or not
   */
  def lruEnabled: Boolean

  protected
  def onCacheHit(): Unit

  protected
  def artifactName: String

  protected
  def maxCount: Int

  protected
  def generateContent(artifact: OUTPUT): Unit

  protected
  def createArtifact(key: FeltArtifactKey, tag: FeltArtifactTag, input: INPUT): OUTPUT

  /**
   * cache or generate the artifact
   *
   * @return the cached or generated artifact
   */
  final
  def fetchArtifact(key: FeltArtifactKey, tag: FeltArtifactTag, input: INPUT): OUTPUT = {
    // look to see if we have already generated this artifact
    val artifact = _cache.compute(key, (key: String, current: OUTPUT) => current match {
      case null =>
        log info s"ARTIFACT_NOT_IN_CACHE (will generate and cache) -- $artifactName($tag) $serviceName"
        val artifact: OUTPUT = createArtifact(key, tag, input)
        _currentCount.incrementAndGet()
        FeltReporter.recordSweepCountIncrement()
        artifact.acquireWriteLock

      case artifact =>
        onCacheHit()
        log debug s"$serviceName -- $artifactName($tag) found in cache"
        artifact.touch
    })

    if (artifact.isWriteLockedByCurrentThread) {
      try {
        generateContent(artifact)
      } catch safely {
        case t =>
          _cache.remove(key) // make the artifactory forget the failed artifact
          log error(s"ARTIFACT_FAILED_TO_GENERATE -- $artifactName($tag) $serviceName", t)
          throw t
      } finally artifact.releaseWriteLock()
    }

    if (lruEnabled) artifact.acquireReadLock
    artifact
  }

}
