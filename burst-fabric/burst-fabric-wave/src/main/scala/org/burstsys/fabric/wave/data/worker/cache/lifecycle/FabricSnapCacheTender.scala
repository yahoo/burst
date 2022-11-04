/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.cache.lifecycle

import org.burstsys.fabric.wave.data.model.limits.FabricSnapCacheLimits
import org.burstsys.fabric.wave.data.worker.cache.{FabricSnapCache, FabricSnapCacheContext}
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

/**
 * [[FabricSnapCache]] periodic lifecycle management of resources i.e. memory (off heap/native/direct) through eviction
 * and persistent file system (disk) through flushing of datasets where needed
 * and appropriate.
 * upper and lower resource bounds found in the `limits` [[FabricSnapCacheLimits]] object.
 * 'stale' snaps are those who's TTL is exceeded
 */
trait FabricSnapCacheTender extends AnyRef {

  self: FabricSnapCacheContext =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final protected
  def startTender(): Unit = {
    limits.validateResourceMinimums()
    backgroundSnapTender = new TenderBackground().start
  }

  final protected
  def stopTender(): Unit = {
    backgroundSnapTender.stop
    backgroundSnapTender = null
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final protected class TenderBackground extends VitalsBackgroundFunction(
    "fab-snap-tender", limits.tendStartWait, limits.tendPeriod, {
      // we do not assert global cache lock in tending...
      // make sure we keep this background processing out of the way of scans
      Thread.currentThread setPriority (Thread.NORM_PRIORITY - 2)
      log info s"CACHE_TEND_START hotSnapCount=$hotSnapCount, warmSnapCount=$warmSnapCount, coldSnapCount=$coldSnapCount"
      talk(_.onSnapCacheTend(cache))
      try {
        if (limits.memoryUsageAboveHighWater)
          evictConstrainedSnaps()
        if (limits.diskUsageAboveHighWater)
          flushConstrainedSnaps()
        evictStaleSnaps()
        flushStaleSnaps()
        eraseStaleSnaps()
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(s"CACHE_TEND_FAIL $t", t)
      }
      log info s"CACHE_TEND_END  hotSnapCount=$hotSnapCount, warmSnapCount=$warmSnapCount, coldSnapCount=$coldSnapCount"
    }
  )

  /**
   * background thread that periodically manages cache evict and flushes based on available memory and disk resources.
   */
  final private
  var backgroundSnapTender: TenderBackground = _

  private final
  def evictStaleSnaps(): Unit = {
    val eSnaps = evictTtlExpiredSnaps
    eSnaps.foreach(evictSnap(_, "stale"))
  }

  private final
  def flushStaleSnaps(): Unit = flushTtlExpiredSnaps.foreach(flushSnap(_, "stale"))

  private final
  def eraseStaleSnaps(): Unit = eraseTtlExpiredSnaps.foreach(eraseSnap(_, "stale"))

  private final
  def evictConstrainedSnaps(): Unit = {
    evictCandidateSnaps.foreach {
      s =>
        evictSnap(s, "low disk")
        if (limits.memoryUsageBelowLowWater) return
    }
  }

  private final
  def flushConstrainedSnaps(): Unit = {
    flushCandidateSnaps.foreach {
      s =>
        flushSnap(s, "low disk")
        if (limits.diskUsageBelowLowWater) return
    }
  }

}
