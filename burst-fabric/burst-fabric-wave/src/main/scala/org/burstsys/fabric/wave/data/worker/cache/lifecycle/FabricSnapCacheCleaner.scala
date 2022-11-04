/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.cache.lifecycle

import org.burstsys.fabric.wave.data.model.snap.{ColdSnap, FabricSnap, FailedSnap, HotSnap, NoDataSnap, WarmSnap}
import org.burstsys.fabric.wave.data.worker.cache.FabricSnapCacheContext
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.instrument.{prettyTimeFromMillis, prettyTimeFromNanos}
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.time.printTimeInPast

/**
 * OPS to clean resources out of memory and disk
 * {{{
 *   evict: remove slice from memory
 *   flush: remove slices from disk
 * }}}
 * NOTE: under peak loading it is possible for cleaning to take up significant system BW. It is important
 * to set high/low memory/disk watermarks to ensure that tending is a measured background process and does
 * not cause intermittent lack of responsiveness. We may need to add a BW throttle to slow down flushing
 * so as to not sporadically slow down incoming load/scans.
 * <p/>
 * '''Note:''' this routine shares state machine sync protocols with [[FabricSnapCacheCleaner]] routines so be
 * aware of what is going on there...
 */
trait FabricSnapCacheCleaner extends AnyRef {

  self: FabricSnapCacheContext =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////


  /**
   * '''evict''' (remove from memory) a snap - you do not need (nor should you have)
   * a global or snap grained lock before entering
   * This will only work if the snap (after a possible transition)  is in [[HotSnap]] state. If not this is a NOOP.
   *
   * @param snap
   * @return true if success, false otherwise
   */
  final protected
  def evictSnap(snap: FabricSnap, cause:String): Boolean = {
    lazy val tag = s"FabricSnapCacheCleaner.evictSnap(guid=${snap.guid}, ${snap.slice.identity})"
    val evictStart = System.nanoTime
    try {
      var loops = 0
      while (loops < tenderLoopTuner.maxLoops) {
        snap.state match {
          /**
           * Only a HotSnap can be evicted. A NoDataSnap is ignored  since it is really hot AND warm
           * and does NOT need eviction. It will be handled in the flush ops.
           */
          case HotSnap =>
            if (snap.trySnapWriteLock) {
              try {
                log info
                  s"""|
                      |CACHE_SNAP_EVICT cause=$cause state=${snap.state}, ${snap.slice.identity},
                      | evictTtlMs=${snap.evictTtlMs} (${prettyTimeFromMillis(snap.evictTtlMs)})
                      | lastAccessTime=${snap.lastAccessTime}  (${printTimeInPast(snap.lastAccessTime)} ago)
                      | $tag""".stripMargin
                return doEvict(snap)
              } finally
                snap.releaseSnapWriteLock()
            } else
              snap.waitState(tenderLoopTuner.waitQuantumMs) // if someone else has a lock then we wait for them - but not too long...
          case _ => return false
        }
        loops += 1
      }
      log info burstStdMsg(s"FAB_CACHE_EVICT_DELAY elapsed=${prettyTimeFromNanos(System.nanoTime - evictStart)} loops=$loops")
      false
    } catch safely {
      case t: Throwable =>
        snap.lastFail = t
        log error burstStdMsg(s"FAB_CACHE_EVICT_FAIL $t $tag", t)
        throw t
    }
  }

  /**
   * '''flush''' (remove from persistent storage) a snap - you do not need (nor should you have)
   * a global or snap grained lock before entering.
   * This will only work if the snap (after a possible transition) is in [[WarmSnap]] (it will do an evict first)
   * or [[HotSnap]]  state. If not this is a NOOP.
   *
   * @param snap
   * @return true if success, false otherwise
   */
  final protected
  def flushSnap(snap: FabricSnap, cause:String): Boolean = {
    lazy val tag = s"FabricSnapCacheCleaner.flushSnap(guid=${snap.guid}, ${snap.slice.identity})"
    val flushStart = System.nanoTime
    try {
      var loops = 0
      while (loops < tenderLoopTuner.maxLoops) {
        snap.state match {

          /**
           * WarmSnap AND NoDataSnap handled here...
           * NoDataSnap is handled here since it is really hot AND warm
           * NoDataSnap DOES need flushing to become cold -- it does NOT need eviction before hand
           */
          case WarmSnap | NoDataSnap => //
            if (snap.trySnapWriteLock) {
              try {
                log info
                  s"""|
                      |CACHE_SNAP_FLUSH cause=$cause state=${snap.state}, ${snap.slice.identity},
                      | flushTtlMs=${snap.flushTtlMs} (${prettyTimeFromMillis(snap.flushTtlMs)})
                      | lastAccessTime=${snap.lastAccessTime}  (${printTimeInPast(snap.lastAccessTime)} ago)
                      | $tag""".stripMargin
                return doFlush(snap)
              } finally snap.releaseSnapWriteLock()
            } else snap.waitState(tenderLoopTuner.waitQuantumMs) // if someone else has a lock then we wait for them - but not too long...

          /**
           * HotSnap needs to be evicted BEFORE it can be flushed
           */
          case HotSnap =>
            if (snap.trySnapWriteLock) {
              try {
                log info
                  s"""|
                      |CACHE_SNAP_FLUSH (WITH EVICT) cause=stale state=${snap.state}, ${snap.slice.identity},
                      | flushTtlMs=${snap.flushTtlMs} (${prettyTimeFromMillis(snap.flushTtlMs)})
                      | lastAccessTime=${snap.lastAccessTime}  (${printTimeInPast(snap.lastAccessTime)} ago)
                      | $tag""".stripMargin
                return doEvict(snap) && doFlush(snap)
              } finally snap.releaseSnapWriteLock()
            } else snap.waitState(tenderLoopTuner.waitQuantumMs) // if someone else has a lock then we wait for them - but not too long...

          case _ => return false
        }
        loops += 1
      }
      log info burstStdMsg(s"FAB_CACHE_FLUSH_DELAY elapsed=${prettyTimeFromNanos(System.nanoTime - flushStart)} loops=$loops")
      false
    } catch safely {
      case t: Throwable =>
        snap.lastFail = t
        log error burstStdMsg(s"FAB_CACHE_FLUSH_FAIL $t $tag", t)
        throw t
    }
  }

  /**
   * '''erase''' (remove from cache management) a Cold snap - you do not need (nor should you have)
   * a global or snap grained lock before entering.
   * This will only work if the snap (after a possible transition) is in [[ColdSnap]] If not this is a NOOP.
   *
   * @param snap
   * @return true if success, false otherwise
   */
  final protected
  def eraseSnap(snap: FabricSnap, cause:String): Boolean = {
    lazy val tag = s"FabricSnapCacheCleaner.eraseSnap(guid=${snap.guid}, ${snap.slice.identity})"
    val start = System.nanoTime
    try {
      var loops = 0
      while (loops < tenderLoopTuner.maxLoops) {
        snap.state match {

          /**
           * ONLY ColdSnap can be erased. There is no such thing as a NoDataFlush at this point
           * since it would have to have been flushed earlier and thus in a cold state.
           */
          case ColdSnap =>
            if (snap.trySnapWriteLock) {
              try {
                log info
                  s"""|
                      |CACHE_SNAP_ERASE cause=$cause state=${snap.state}, ${snap.slice.identity},
                      | eraseTtlMs=${snap.eraseTtlMs} (${prettyTimeFromMillis(snap.eraseTtlMs)})
                      | lastAccessTime=${snap.lastAccessTime}  (${printTimeInPast(snap.lastAccessTime)} ago)
                      | $tag""".stripMargin
                return doErase(snap)
              } finally snap.releaseSnapWriteLock()
            } else snap.waitState(tenderLoopTuner.waitQuantumMs) // if someone else has a lock then we wait for them - but not too long...

          case _ => return false
        }
        loops += 1
      }
      log info burstStdMsg(s"FAB_CACHE_ERASE_DELAY elapsed=${prettyTimeFromNanos(System.nanoTime - start)} loops=$loops")
      false
    } catch safely {
      case t: Throwable =>
        snap.lastFail = t
        log error burstStdMsg(s"FAB_CACHE_ERASE_FAIL $t $tag", t)
        throw t
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * do the actual evict of the snap and its regions.
   *
   * @param snap
   * @return true if success, false otherwise
   */
  private[cache]
  def doEvict(snap: FabricSnap): Boolean = {
    lazy val tag = s"FabricSnapCacheCleaner.doEvict(guid=${snap.guid}, ${snap.slice.identity})"
    val evictStart = System.nanoTime
    try {
      snap.state match {
        case ColdSnap =>
          log warn s"FAB_CACHE_EVICT_COLD_SNAP $tag "
        case WarmSnap =>
          log warn s"FAB_CACHE_EVICT_WARM_SNAP $tag "
        case HotSnap =>
          log info s"FAB_CACHE_EVICT_HOT_SNAP $tag "
          snap.data.evictSliceFromMemory()
          snap.state = WarmSnap
          snap.recordAccess
          talk(_.onSnapEvict(snap, System.nanoTime - evictStart, snap.metadata.generationMetrics.byteCount))
          return true
        case NoDataSnap =>
          log warn s"FAB_CACHE_EVICT_NO_DATA_SNAP $tag "
        case FailedSnap =>
          log warn s"FAB_CACHE_EVICT_FAILED_SNAP $tag "
        case s =>
          log warn s"FAB_CACHE_EVICT_UNKNOWN_SNAP state=$s $tag "
      }
      false
    } catch safely {
      case throwable: Throwable =>
        log error burstStdMsg(s"FAB_CACHE_EVICT_FAIL $throwable $tag", throwable)
        snap.lastFail = throwable
        false
    }
  }

  /**
   * do the actual flush of the snap and its regions.
   *
   * @param snap
   * @return true if success, false otherwise
   */
  private[cache]
  def doFlush(snap: FabricSnap): Boolean = {
    lazy val tag = s"FabricSnapCacheCleaner.doFlush(guid=${snap.guid}, ${snap.slice.identity})"
    val flushStart = System.nanoTime
    try {
      snap.state match {
        case ColdSnap =>
          log warn s"FAB_CACHE_FLUSH_COLD_SNAP $tag "
        case WarmSnap =>
          log info s"FAB_CACHE_FLUSH_WARM_SNAP $tag "
          snap.data.flushSliceFromDisk()
          snap.state = ColdSnap
          snap.recordAccess
          talk(_.onSnapFlush(snap, System.nanoTime - flushStart, snap.metadata.generationMetrics.byteCount))
          return true
        case HotSnap =>
          log warn s"FAB_CACHE_FLUSH_HOT_SNAP $tag "
        case NoDataSnap =>
          log info s"FAB_CACHE_FLUSH_NO_DATA_SNAP $tag "
          snap.state = ColdSnap
          snap.recordAccess
          talk(_.onSnapFlush(snap, System.nanoTime - flushStart, snap.metadata.generationMetrics.byteCount))
          return true
        case FailedSnap =>
          log warn s"FAB_CACHE_FLUSH_FAILED_SNAP $tag "
        case s =>
          log warn s"FAB_CACHE_FLUSH_UNKNOWN_SNAP state=$s $tag "
      }
      false
    } catch safely {
      case t: Throwable =>
        snap.lastFail = t
        log error burstStdMsg(s"FAB_CACHE_FLUSH_FAIL $t $tag", t)
        false
    }
  }

  /**
   * do an erase of a (Cold) snap.
   *
   * @param snap
   * @return true if success, false otherwise
   */
  private[cache]
  def doErase(snap: FabricSnap): Boolean = {
    lazy val tag = s"FabricSnapCacheCleaner.doErase(guid=${snap.guid}, ${snap.slice.identity})"
    try {
      snap.state match {
        case WarmSnap => log warn s"FAB_CACHE_ERASE_COLD_SNAP $tag "
        case HotSnap => log warn s"FAB_CACHE_ERASE_HOT_SNAP $tag "
        case FailedSnap => log warn s"FAB_CACHE_ERASE_FAILED_SNAP $tag "
        case NoDataSnap => log warn s"FAB_CACHE_ERASE_NO_DATA_SNAP $tag "
        case ColdSnap =>
          acquireGlobalCacheLock()
          try {
            log info s"FAB_CACHE_ERASE_COLD_SNAP $tag "
            this -= snap.delete // remove from cache tracking and file system
            talk(_.onSnapErase(snap))
            return true
          } finally releaseGlobalCacheLock()
        case s => log warn s"FAB_CACHE_ERASE_UNKNOWN_SNAP state=$s $tag "
      }
      false
    } catch safely {
      case t: Throwable =>
        snap.lastFail = t
        log error burstStdMsg(s"FAB_CACHE_ERASE_FAIL $t $tag", t)
        false
    }
  }

}
