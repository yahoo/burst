/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.snap

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.locks.ReentrantReadWriteLock

import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

import scala.language.postfixOps

/**
 * all locking semantics associated with [[FabricSnap]]
 */
trait FabricSnapLocker extends AnyRef {

  self: FabricSnapContext =>

  final val debugSnapLocks = false

  ///////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////

  private[this]
  final val _snapReadWriteLock = new ReentrantReadWriteLock(true)

  private[this]
  final val _snapReadLock = _snapReadWriteLock.readLock()

  private[this]
  final val _snapWriteLock = _snapReadWriteLock.writeLock()

  private[this]
  final val _snapWriteLocked = new AtomicBoolean()

  private[this]
  final val _snapReadLocks = new AtomicInteger()

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  final
  def printReadLock: String = s"${_snapReadLock.toString}, snapReadLocks=${_snapReadLocks.get()}"

  final
  def printWriteLock: String = s"${_snapWriteLock.toString}, snapWriteLocked=${_snapWriteLocked.get()}"

  final override
  def trySnapReadLock: Boolean = {
    lazy val tag = s"FabricSnapLocker.trySnapReadLock($parameters)"
    if (debugSnapLocks) log info s"ENTER $tag"
    if (_snapReadLock.tryLock()) {
      if (debugSnapLocks) log info s"ACQUIRED $tag"
      _snapReadLocks.incrementAndGet()
      true
    } else {
      if (debugSnapLocks) log info s"UNAVAILABLE $tag"
      false
    }
  }
  final override
  def trySnapWriteLock: Boolean = {
    lazy val tag = s"FabricSnapLocker.trySnapWriteLock($parameters)"
    if (debugSnapLocks) log info s"ENTER $tag"
    if (_snapWriteLock.tryLock()) {
      if (debugSnapLocks) log info s"ACQUIRED $tag"
      _snapWriteLocked.set(true)
      true
    } else {
      if (debugSnapLocks) log info s"UNAVAILABLE $tag"
      false
    }
  }


  final override
  def releaseSnapWriteLock(): FabricSnap = {
    lazy val tag = s"FabricSnapLocker.releaseSnapWriteLock($parameters)"
    if (debugSnapLocks)
      log info s"ENTER $tag"
    try {
      _snapWriteLock.unlock()
    } catch safely {
      case t: IllegalMonitorStateException =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        throw t
    }
    if (debugSnapLocks)
      log info s"EXIT $tag"
    _snapWriteLocked.set(false)
    this
  }

  final override
  def releaseSnapReadLock(): FabricSnap = {
    lazy val tag = s"FabricSnapLocker.releaseSnapReadLock($parameters)"
    if (debugSnapLocks)
      log info s"ENTER $tag"
    try {
      _snapReadLock.unlock()
    } catch safely {
      case t: IllegalMonitorStateException =>
        log error t
        throw t
    }
    if (debugSnapLocks)
      log info s"EXIT $tag"
    _snapReadLocks.decrementAndGet()
    this
  }

}
