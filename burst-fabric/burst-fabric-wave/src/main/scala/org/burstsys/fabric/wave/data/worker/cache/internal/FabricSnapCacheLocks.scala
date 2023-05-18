/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.cache.internal

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.logging._

import scala.language.postfixOps

/**
 * locking protocol for the snap cache functions
 */
trait FabricSnapCacheLocks extends AnyRef {

  final val debugGlobalLocks = false
  final val debugSnapTransitions = false

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final protected
  val _snapCacheGlobalLock = new ReentrantLock(true)

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /*
    /**
     * a synchronized transition from a snap's current state to another
     * @param snap
     * @param newState
     */
    final protected
    def transitionToState(snap: FabricSnap, newState: FabricSnapState): Unit = {
      lazy val tag = s"FabricSnapCacheLocker.transitionToState(newState=$newState, guid=${snap.guid}, ${snap.slice.identity})"
      if (debugSnapTransitions)
        log info burstStdMsg(s"CACHE_SNAP_ENTER $tag")
      snap synchronized {
        snap.state = newState
        snap.persist // update snap to persistent store (local file system)
        snap.notifyAll()
      }
      if (debugSnapTransitions)
        log info burstStdMsg(s"CACHE_SNAP_EXIT $tag")
    }

    /**
     * a synchronized wait while a snap undergoes some operation that moves it from one state to another
     * entrance has no locks, exit has global cache lock
     * @param snap
     */
    final protected
    def waitForStateTransitionThenGlobalLock(snap: FabricSnap): Unit = {
      lazy val tag = s"FabricSnapCacheLocker.waitForStateTransition(guid=${snap.guid}, ${snap.slice.identity})"
      if (debugSnapTransitions)
        log info s"CACHE_SNAP_WAIT_FOR_TRANSITION $tag"
      // wait for any transitions to finish
      snap synchronized {
        var wasInTransition = false
        while (snap.state == TransitionSnap) {
          if (!wasInTransition) log info s"CACHE_SNAP_IN_TRANSITION (wait for resolution...) $tag"
          wasInTransition = true
          snap.wait()
        }
        log info s"CACHE_SNAP_${if (wasInTransition) "" else "NOT_"}IN_TRANSITION (resolved state is '${snap.state}'...) $tag"
        acquireGlobalCacheLock()
      }
      if (debugSnapTransitions)
        log info burstStdMsg(s"CACHE_SNAP_EXIT $tag")
    }
  */

  final private def info: String = s"holds=${_snapCacheGlobalLock.getHoldCount} queue=${_snapCacheGlobalLock.getQueueLength}"

  /**
   * acquire the global grain snap cache lock. This is to be used very sparingly and for
   * very short periods as it blocks all other snap cache operations.
   */
  final protected
  def acquireGlobalCacheLock(): Unit = {
    lazy val tag = "FabricSnapCacheLocker.acquireGlobalCacheLock"
    if (debugGlobalLocks)
      log info s"CACHE_ENTER $tag $info"
    if (_snapCacheGlobalLock.tryLock(0, TimeUnit.SECONDS) ||
      _snapCacheGlobalLock.tryLock(cacheLockAcquireTimeout.toSeconds, TimeUnit.SECONDS)) {
      if (debugGlobalLocks)
        log info s"CACHE_EXIT $tag"
    } else {
      val msg = s"CACHE_TIMEOUT $tag $cacheLockAcquireTimeout [${_snapCacheGlobalLock}] $info"
      log error burstStdMsg(msg)
      throw VitalsException(msg)
    }
  }

  /**
   * release the global grain snap cache lock
   */
  final protected
  def releaseGlobalCacheLock(): Unit = {
    lazy val tag = "FabricSnapCacheLocker.releaseGlobalCacheLock"
    if (debugGlobalLocks)
      log info s"CACHE_ENTER $tag $info"
    try {
      _snapCacheGlobalLock.unlock()
    } catch safely {
      // just in case we fall through a hole and try to unlock twice...
      case imse: IllegalMonitorStateException =>
        log warn burstStdMsg(s"CACHE_COULD_NOT_UNLOCK $tag", imse)
    }
    if (debugGlobalLocks)
      log info s"CACHE_EXIT $tag $info"
  }

  /**
   * release the global grain snap cache lock only if its held
   */
  final protected
  def releaseGlobalCacheLockIfHeld(): Unit = {
    lazy val tag = "FabricSnapCacheLocker.releaseGlobalCacheLockIfHeld"
    if (debugGlobalLocks)
      log info s"CACHE_ENTER $tag $info"
    try {
      if (_snapCacheGlobalLock.isHeldByCurrentThread) _snapCacheGlobalLock.unlock()
    } catch safely {
      // just in case we fall through a hole and try to unlock twice...
      case imse: IllegalMonitorStateException =>
        log warn burstStdMsg(s"CACHE_COULD_NOT_UNLOCK $tag", imse)
    }
    if (debugGlobalLocks)
      log info s"CACHE_EXIT $tag $info"
  }

}
