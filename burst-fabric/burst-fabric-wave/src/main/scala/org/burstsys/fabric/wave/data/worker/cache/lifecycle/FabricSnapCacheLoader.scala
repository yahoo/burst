/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.cache.lifecycle

import org.burstsys.fabric.wave.data.model.slice.FabricSlice
import org.burstsys.fabric.wave.data.model.slice.state._
import org.burstsys.fabric.wave.data.model.snap.{FabricSnap, _}
import org.burstsys.fabric.wave.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.wave.data.worker.cache.{FabricSnapCache, FabricSnapCacheContext}
import org.burstsys.fabric.wave.data.worker.store.getWorkerStore
import org.burstsys.fabric.wave.metadata.model.datasource
import org.burstsys.vitals.errors._
import org.burstsys.vitals.reporter.instrument.prettyTimeFromNanos
import org.burstsys.vitals.logging._

import scala.language.postfixOps

/**
 * [[FabricSnapCache]] dataset generation load  lifecycle functions
 * ===locking protocol===
 * This loader follows an informal loop/wait locking protocol. '''Snap-locks''' are always
 * ''tried'' never blocked on, and thus we go around a loop in a quasi ''busy-wait''
 * mode. We ''wait'' proportionally much more than we ''loop'' so its not really that busy but
 * it does give us a simpler way to synchronize. The '''global cache lock''' is used only to
 * synchronize around the cache snap hash map.
 * ===state sequencing===
 * The state sequence is ``{cold, warm, hot}`` and is managed ''forward'' to get a datasets into
 * scannable memory and ''backwards'' via `flush`, `evict` (either to recoup resources or to
 * perform re-slicing). A given thread is only allowed to move forward or backwards one
 * state at a time (per loop).
 * ===resource tending===
 * its important to also look at [[FabricSnapCacheTender]] which shared this overall locking
 * strategy. Its ''possible'' to have load/re-slice sequencing interleave with resource limits
 * management.
 */
trait FabricSnapCacheLoader extends AnyRef {

  self: FabricSnapCacheContext =>

  final override
  def loadSnapWithReadLock(slice: FabricSlice): FabricSnap = {
    lazy val tag = s"FabricSnapCacheLoader.loadSnapWithReadLock(guid=${slice.guid}, ${slice.identity})"
    val startNanos = System.nanoTime()
    acquireGlobalCacheLock()
    try {
      get(slice) match {
        case None => // snap brand new so its always cold ...
          val snap = FabricSnap(getSnapFile(slice), slice)
          this += snap
          log info s"FAB_CACHE_SNAP_NOT_FOUND (adding to cache map...) $tag "
          releaseGlobalCacheLock()
          processSnap(startNanos, snap, slice)
          snap.lastFail.foreach(t => throw VitalsException(s"FAB_CACHE_LOAD_FAIL $tag", t))
          snap.persist

        case Some(snap) => // snap already in map so we have to deal with multiple states...
          log info s"FAB_CACHE_SNAP_FOUND (taken from cache map...) $tag "
          releaseGlobalCacheLock()
          processSnap(startNanos, snap, slice)
          snap.lastFail.foreach(t => throw VitalsException(s"FAB_CACHE_LOAD_FAIL $tag", t))
          snap.recordAccess
      }
    } catch safely {
      case t: Throwable =>
        releaseGlobalCacheLockIfHeld()
        throw t
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // internals
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * we have a state sequence of {cold, warm, hot} and we are either moving forwards (normal processing) or
   * backwards (resource management and reslice processing) always one state at a time.
   * <p/>
   * '''Note:''' this routine shares state machine sync protocols with [[FabricSnapCacheCleaner]] routines so be
   * aware of what is going on there...
   *
   * @param snap
   * @return either a [[HotSnap]], [[NoDataSnap]], or a [[FailedSnap]]
   */
  private
  def processSnap(startNanos: Long, snap: FabricSnap, newSlice: FabricSlice): FabricSnap = {
    var loops = 0
    var reqGuid = newSlice.guid
    def tag = s"FabricSnapCacheLoader.processSnap(guid=$reqGuid slice=${snap.slice.guid}, ${snap.slice.identity}, loops=$loops)"
    while (loops < loadLoopTuner.maxLoops) {
      val resliceNeeded = newSlice.isResliceOf(snap.slice)
      snap.state match {
        case ColdSnap =>
          if (snap.trySnapWriteLock) {
            try {
              if (resliceNeeded) {
                log info s"FAB_CACHE_SNAP_COLD (reslice mode - needs reset/load...) $tag"
                snap.slice = newSlice // if reslicing needed then this is where to reset.
                snap.persist
                talk(_.onSnapReslice(snap)) // if successful then this is considered a 'reslice'
              } else log info s"FAB_CACHE_SNAP_COLD (normal mode - needs load...) $tag"
              if (doLoad(snap)) // moves from cold to hot
                talk(_.onSnapColdLoad(snap, System.nanoTime - startNanos, snap.metadata.generationMetrics.byteCount))
              else
                talk(_.onSnapFailLoad(snap))
            } finally snap.releaseSnapWriteLock()
          } else snap.waitState(loadLoopTuner.waitQuantumMs)

        case WarmSnap =>
          if (snap.trySnapWriteLock) {
            try {
              if (resliceNeeded) {
                log info s"FAB_CACHE_SNAP_WARM (reslice mode - needs flush...) $tag"
                doFlush(snap) // moves from warm to cold
              } else {
                log info s"FAB_CACHE_SNAP_WARM (normal mode - needs warming...) $tag"
                if (doLoad(snap)) // moves from warm to hot
                  talk(_.onSnapWarmLoad(snap, System.nanoTime - startNanos, snap.metadata.generationMetrics.byteCount))
                else
                  talk(_.onSnapFailLoad(snap))
              }
            } finally snap.releaseSnapWriteLock()
          } else snap.waitState(loadLoopTuner.waitQuantumMs)

        case HotSnap =>
          if (resliceNeeded && snap.trySnapWriteLock) {
            try {
              log info s"FAB_CACHE_SNAP_HOT (reslice mode - needs evict...) $tag"
              doEvict(snap) // moves from hot to warm
            } finally snap.releaseSnapWriteLock()
          } else if (!resliceNeeded && snap.trySnapReadLock) {
            log info s"FAB_CACHE_SNAP_HOT (normal mode - scan ready...) $tag"
            talk(_.onSnapHotLoad(snap, System.nanoTime - startNanos, snap.metadata.generationMetrics.byteCount))
            return snap
          } else snap.waitState(loadLoopTuner.waitQuantumMs)

        case NoDataSnap =>
          if (resliceNeeded && snap.trySnapWriteLock) {
            try {
              log info s"FAB_CACHE_SNAP_EMPTY (reslice mode - needs fake flush ...) $tag"
              snap.state = ColdSnap
              talk(_.onSnapReslice(snap))
            } finally snap.releaseSnapWriteLock()
          } else if (!resliceNeeded && snap.trySnapReadLock) {
            log info s"FAB_CACHE_SNAP_EMPTY (normal mode - scan ready...) $tag"
            talk(_.onSnapHotLoad(snap, System.nanoTime - startNanos, snap.metadata.generationMetrics.byteCount))
            return snap
          } else snap.waitState(loadLoopTuner.waitQuantumMs)

        case FailedSnap =>
          if ((resliceNeeded || snap.hasHealed) && snap.trySnapWriteLock) {
            log info s"FAB_CACHE_SNAP_FAILED (failCount=${snap.failCount}, maxFails=${loadLoopTuner.maxFails} - reset failure...) $tag"
            snap.state = ColdSnap // start the state sequence again
            snap.resetLastFail() // set fail count to 0, if it's not already
            snap.releaseSnapWriteLock()
          } else if (snap.failCount < loadLoopTuner.maxFails && snap.trySnapWriteLock) {
            log info s"FAB_CACHE_SNAP_FAILED (failCount=${snap.failCount}, maxFails=${loadLoopTuner.maxFails} - try another cold load...) $tag"
            snap.state = ColdSnap // start the state sequence again
            snap.releaseSnapWriteLock()
          } else if (snap.failCount >= loadLoopTuner.maxFails && snap.trySnapWriteLock) {
            snap.releaseSnapWriteLock()
            val msg = s"FAB_CACHE_SNAP_FAILED (failCount=${snap.failCount}, maxFails=${loadLoopTuner.maxFails} - give up...) $tag"
            log error burstStdMsg(msg)
            return snap
          } else snap.waitState(loadLoopTuner.waitQuantumMs)

        case s =>
          val msg = s"FAB_CACHE_SNAP_BAD (bad state $s...) $tag"
          log error burstStdMsg(msg)
          val t = VitalsException(msg).fillInStackTrace()
          snap.lastFail = t
          throw t
      }

      loops += 1
    }
    // if this happens we have tuned it wrong
    val msg = s"FAB_CACHE_PROCESS_TIMEOUT elapsed=${prettyTimeFromNanos(System.nanoTime - startNanos)}, loops=$loops $tag"
    log error burstStdMsg(msg)
    val t = VitalsException(msg).fillInStackTrace()
    snap.lastFail = t
    throw t
  }

  /**
   * do a load from a cold or warm state.
   *
   * @param snap
   * @return true if success, false if not
   */
  private
  def doLoad(snap: FabricSnap): Boolean = {
    lazy val tag = s"FabricSnapCacheLoader.doLoad(guid=${snap.guid}, ${snap.slice.identity}, store=${snap.slice.datasource.view.storeProperties.get(FabricStoreNameProperty)})"
    try {
      val store = getWorkerStore(snap.slice.datasource)
      log info s"$tag"
      store.loadSliceFromCacheOrInitialize(snap)
      snap.metadata.state match {
        case FabricDataNoData =>
          snap.state = NoDataSnap
          true
        case FabricDataHot =>
          snap.state = HotSnap
          true
        case FabricDataFailed =>
          snap.state = FailedSnap
          false
        case FabricDataCold | FabricDataWarm | FabricDataMixed =>
          throw VitalsException(s"FAB_CACHE_LOAD_BAD_OUTCOME state=${snap.metadata.state}!! $tag")
        case s => throw VitalsException(s"unknown state $s!! $tag")
      }
    } catch safely {
      case t: Throwable =>
        snap.lastFail = t
        val msg = s"FAB_CACHE_LOAD_FAIL $t $tag"
        log error burstStdMsg(msg, t)
        false
    }
  }

}
