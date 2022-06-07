/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.compile.artifact

import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * locking behavior for artifacts
 */
trait FeltArtifactLocker extends AnyRef {

  self: FeltArtifact[_] =>

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  final val _readWriteLock = new ReentrantReadWriteLock(true)

  private[this]
  final val _readLock = _readWriteLock.readLock()

  private[this]
  final val _writeLock = _readWriteLock.writeLock()

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def tryWriteLock: Boolean = {
    lazy val tag = s"FeltArtifact('${self.name}').tryWriteLock"
    if (debugLocking)
      log info s"$tag BEGIN"
    try {
      _writeLock.tryLock()
    } finally
      if (debugLocking)
        log info s"$tag END"
  }

  final
  def acquireWriteLock: this.type = {
    lazy val tag = s"FeltArtifact('${self.name}').acquireWriteLock"
    try {
      if (debugLocking)
        log info s"$tag BEGIN"
      _writeLock.lock()
      if (debugLocking)
        log info s"$tag END"
    } catch {
      case t: Throwable =>
        log error s"$t $tag"
        throw t
    }
    this
  }

  final def isWriteLockedByCurrentThread: Boolean = _readWriteLock.isWriteLockedByCurrentThread

  final def releaseWriteLock(): this.type = {
    lazy val tag = s"FeltArtifact('${self.name}').releaseWriteLock"
    try {
      if (debugLocking)
        log info s"$tag BEGIN"
      _writeLock.unlock()
      if (debugLocking)
        log info s"$tag END"
    } catch {
      case t: Throwable =>
        log error s"$t $tag"
        throw t
    }
    this
  }

  final
  def acquireReadLock: this.type = {
    lazy val tag = s"FeltArtifact('${self.name}').acquireReadLock"
    try {
      if (debugLocking)
        log info s"$tag BEGIN"
      _readLock.lock()
      if (debugLocking)
        log info s"$tag END"
    } catch {
      case t: Throwable =>
        log error s"$t $tag"
    }
    this
  }

  final
  def releaseReadLock: this.type = {
    lazy val tag = s"FeltArtifact('${self.name}').releaseReadLock"
    try {
      if (debugLocking)
        log info s"$tag BEGIN"
      _readLock.unlock()
      if (debugLocking)
        log info s"$tag END"
    } catch {
      case t: Throwable =>
        log error s"$t $tag"
        throw t
    }
    this
  }

}
