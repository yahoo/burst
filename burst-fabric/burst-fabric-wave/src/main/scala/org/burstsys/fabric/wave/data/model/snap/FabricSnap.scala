/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.snap

import java.io.FileInputStream
import java.nio.file.{Path, Paths}

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import org.burstsys.fabric.wave.data.model.slice.FabricSlice
import org.burstsys.fabric.wave.data.model.slice.data.FabricSliceData
import org.burstsys.fabric.wave.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.vitals.errors
import org.burstsys.vitals.errors._
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.time.printTimeInPast
import org.burstsys.vitals.uid.{VitalsUid, newBurstUid}

/**
 * == Snaps (worker side cache dataset descriptor) ==
 * this is a one per dataset slice data structure that captures all cache persistent metadata associated
 * with a '''snap''' dataset slice. The snap file is written to disk after all the region files have been successfully
 * written in one atomic operation. All cache consistency depends on this snap data structure and its understanding
 * of the associated region files.
 * === Slice Identity ===
 * The Snap file contains enough information to identity a slice generation with 100% confidence
 * === Slice Regeneration ===
 * The Snap file contains enough information to ''regenerate'' a slice if necessary (i.e. if a region file read/write operation fails or is lost)
 * === Atomic Operations ===
 * [[https://docs.oracle.com/javase/tutorial/essential/io/move.html]]
 */
trait FabricSnap extends AnyRef {

  /**
   * @return unique UID for the snap
   */
  def guid: VitalsUid

  /**
   * @return the slice associated with this snap
   */
  def slice: FabricSlice

  def slice_=(slice: FabricSlice): Unit

  /**
   * @return access the underlying data
   */
  def data: FabricSliceData

  /**
   * @return access the underlying metadata
   */
  def metadata: FabricSliceMetadata

  /**
   * used for cache cleaning logic
   *
   * @return the last epoch time (ms) this snap was loaded or scanned.
   */
  def lastAccessTime: Long

  /**
   * @return value obtained from [[org.burstsys.fabric.wave.metadata.ViewCacheEvictTtlMsProperty]] or
   * [[org.burstsys.fabric.wavei.configuration.burstViewCacheEvictTtlMsPropertyDefault]]
   */
  def evictTtlMs: Long

  /**
   * @return value obtained from [[org.burstsys.fabric.wave.metadata.ViewCacheFlushTtlMsProperty]] or
   * [[org.burstsys.fabric.wave.configuration.burstViewCacheFlushTtlMsPropertyDefault]]
   */
  def flushTtlMs: Long

  /**
   * @return value obtained from [[org.burstsys.fabric.wave.metadata.ViewCacheEraseTtlMsProperty]] or
   * [[org.burstsys.fabric.wave.configuration.burstViewCacheEraseTtlMsPropertyDefault]]
   */
  def eraseTtlMs: Long

  /**
   * @return this snap's last access time is longer in the past than the evict TTL
   */
  def evictTtlExpired: Boolean

  /**
   * @return this snap's last access time is longer in the past than the flush TTL
   */
  def flushTtlExpired: Boolean

  /**
   * @return this snap's last access time is longer in the past than the erase TTL
   */
  def eraseTtlExpired: Boolean

  /**
   * used for cache cleaning logic
   *
   * @return the total number of times this snap was loaded or scanned
   */
  def totalAccessCount: Long

  /**
   * record a load or scan (lastAccessTime/totalAccessCount), used for cache cleaning logic
   */
  def recordAccess: FabricSnap

  /**
   * @return release the current read lock
   */
  def releaseSnapReadLock(): FabricSnap

  /**
   * @return release the write lock
   */
  def releaseSnapWriteLock(): FabricSnap

  /**
   * @return try to get a write lock returning true if successful
   */
  def trySnapWriteLock: Boolean

  /**
   * @return try to get a read lock returning true if successful
   */
  def trySnapReadLock: Boolean

  /**
   * @return the state of the snap
   */
  def state: FabricSnapState

  def state_=(s: FabricSnapState): Unit

  /**
   * wait for a state change because we could not get a lock
   * this should happen rarely and in the case that it happens AND it takes
   * a long time or get a state change we make a short timeout and live with the extra loops
   */
  def waitState(ms: Long): Unit

  /**
   * @return persist current state of snap to disk backing store
   */
  def persist: FabricSnap

  /**
   * delete underlying file storage for this snap
   * @return
   */
  def delete : FabricSnap

  /**
   * @return the snap disk backing store path
   */
  def snapFile: Path

  /**
   * @return if the underlying fault has healed
   */
  def isHealed: Boolean = failCount == 0

  /**
   * @return number of failed attempts to do a load...
   */
  def failCount: Int

  /**
   * record most recent failure. This gets cleared by `resetLastFail()`
   */
  def lastFail_=(t: Throwable): Unit

  /**
   * @return the last failure.
   */
  def lastFail: Option[Throwable]

  /**
   * reset failure but ''not'' failure count. We allow a certain number of
   * failures but do allow it to succeed if a retry helps
   */
  def resetLastFail(): Unit
}

object FabricSnap {

  def apply(path: Path): FabricSnap = {
    try {
      val filePath = Paths.get(path.toString + snapSuffix)
      val kryo = org.burstsys.vitals.kryo.acquireKryo
      val input = new Input(new FileInputStream(filePath.toString))
      try {
        kryo.readClassAndObject(input).asInstanceOf[FabricSnapContext]
      } finally input.close()
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }

  def apply(path: Path, slice: FabricSlice): FabricSnap = {
    val snap = FabricSnapContext(slice)
    snap.open(path)
  }

}

private[fabric] final
case class FabricSnapContext(var slice: FabricSlice) extends FabricSnap with FabricSnapImage with FabricSnapLocker {
  def this() = this(null)

  def parameters: String = s"guid=$guid, state=$state, path=$path, readLock=$printReadLock, writeLock=$printWriteLock, totalAccessCount=$totalAccessCount, lastAccessTime=$lastAccessTime (${printTimeInPast(lastAccessTime)} ago), failCount=$failCount, lastFailure=${if (lastFail.nonEmpty) s"\n${errors.printStack(lastFail.get)}\n" else "NONE"}"

  override
  def toString: String =
    s"""FabricSnap(guid=$guid,  state=$state
        |   slice=${slice}
        |   metadata=${metadata}
        |   data=${data}
        |   readLock=$printReadLock
        |   writeLock=$printWriteLock
        |   totalAccessCount=$totalAccessCount, lastAccessTime=$lastAccessTime (${printTimeInPast(lastAccessTime)} ago)
        |   evictTtlMs=$evictTtlMs (${prettyTimeFromMillis(evictTtlMs)}), evictTtlExpired=$evictTtlExpired,
        |   flushTtlMs=$flushTtlMs (${prettyTimeFromMillis(flushTtlMs)}), flushTtlExpired=$flushTtlExpired,
        |   eraseTtlMs=$eraseTtlMs (${prettyTimeFromMillis(eraseTtlMs)}), eraseTtlExpired=$eraseTtlExpired,
        |   failCount=$failCount, lastFailure=${if (lastFail.nonEmpty) s"\n${errors.printStack(lastFail.get)}\n" else "NONE"})""".stripMargin

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override def evictTtlExpired: Boolean = (System.currentTimeMillis - lastAccessTime) > evictTtlMs

  override def flushTtlExpired: Boolean = (System.currentTimeMillis - lastAccessTime) > flushTtlMs

  override def eraseTtlExpired: Boolean = (System.currentTimeMillis - lastAccessTime) > eraseTtlMs

}
