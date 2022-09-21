/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.slice.region.writer

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.{NoSuchFileException, Path, StandardOpenOption}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong, LongAdder}

import org.burstsys.fabric.data.model.slice.region.hose.FabricWriteMetrics
import org.burstsys.fabric.data.model.slice.region.{FabricRegion, FabricRegionIdentity, FabricRegionReporter, FabricRegionTag, RegionMagic, RegionVersion}
import org.burstsys.fabric.data.model.snap.{FabricSnap, FabricSnapComponent}
import org.burstsys.fabric.data.worker.pump.{FabricCacheImpeller, FabricCacheIntake}
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

/**
 * write operations for a fabric region file
 */
trait FabricRegionWriter extends FabricWriteMetrics with FabricRegionIdentity with FabricSnapComponent {

  /**
   * Did the write to this region result in a run (empty) region?
   *
   * @return
   */
  def regionIsRunt: Boolean

  /**
   * The assigned impeller for this region writer
   *
   * @return
   */
  def impeller: FabricCacheImpeller

  /**
   * Write a single parcel off the queue to disk
   *
   * @return size of buffer written
   */
  def writeNextParcel: Long

  /**
   * queue a single parcel into the write vector
   *
   * @return
   */
  def queueParcelForWrite(parcel: TeslaParcel): Unit

  /**
   * open the region write subsystem
   */
  def openForWrites(): Unit

  /**
   * @return is this writer ready for writing
   */
  def isOpenForWrites: Boolean

  /**
   * Put the end of region marker on the queue
   */
  def allParcelsQueuedForWrite(): Unit


  /**
   * Wait for all the queued parcels to be written
   */
  def waitForWritesToComplete(): Unit

  /**
   * close the region write subsystem
   */
  def closeForWrites(): Unit

}

object FabricRegionWriter {
  def apply(
             snap: FabricSnap,
             region: FabricRegion,
             regionIndex: Int,
             regionTag: FabricRegionTag,
             filePath: Path
           ): FabricRegionWriter =
    FabricRegionWriterContext(
      snap: FabricSnap,
      region: FabricRegion,
      regionIndex: Int,
      regionTag: FabricRegionTag,
      filePath: Path,
      FabricCacheIntake.assignImpeller(regionTag)
    )
}

private[region] final case
class FabricRegionWriterContext(
                                 var snap: FabricSnap,
                                 region: FabricRegion,
                                 regionIndex: Int,
                                 regionTag: FabricRegionTag,
                                 filePath: Path,
                                 impeller: FabricCacheImpeller
                               ) extends AnyRef with FabricRegionWriter with FabricRegionParcelWriter {

  lazy val parameters = s"guid=${snap.guid}, regionIndex=${region.regionIndex}, file=${filePath}"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[writer] var _channel: AsynchronousFileChannel = _

  private[writer] var _writePtr: Long = _

  private[this] final val _completed = new AtomicBoolean(false)

  protected[this] var _lastWriteFuture: java.util.concurrent.Future[Integer] = _

  protected[this] var _isRuntRegion = false

  protected[this] var _isOpenForWrites = false

  protected[this] var _isOnDisk = false

  protected[this] val _start: Long = System.nanoTime

  protected[this] val _elapsedNs = new AtomicLong

  protected[this] val _parcelCount = new LongAdder

  protected[this] val _itemCount = new LongAdder

  protected[this] val _inflatedByteCount = new LongAdder

  protected[this] val _deflatedByteCount = new LongAdder

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Api
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def wireSnap(s: FabricSnap): Unit = snap = s

  override
  def regionIsRunt: Boolean = _isRuntRegion

  override
  def isOpenForWrites: Boolean = _isOpenForWrites

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def openForWrites(): Unit = synchronized {
    lazy val tag = s"FabricRegionWriter.open($parameters)"
    synchronized {
      if (_isOpenForWrites) throw VitalsException(s"REGION_WRITE_OPEN_ALREADY $tag")
      try {
        log info s"REGION_WRITE_OPEN_START $tag"
        _writePtr = 0
        _channel = AsynchronousFileChannel.open(filePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
      } catch safely {
        case nsfe: NoSuchFileException =>
          throw VitalsException(s"REGION_WRITE_OPEN_NO_PATH $tag", nsfe)
      }

      writeHeader()
      _completed.set(false)
      _parcelCount.reset()

      FabricRegionReporter.countWriteOpen()
      _isOpenForWrites = true
    }
  }


  override def closeForWrites(): Unit = {
    lazy val tag = s"FabricRegionWriter.close($parameters)"
    synchronized {
      if (!_isOpenForWrites) throw VitalsException(s"REGION_WRITE_CLOSE_NOT_OPEN! $tag ")
      if (!_completed.get) log.warn(s"$tag region not marked as complete")

      if (_parcelCount.sum == 0) {
        _isRuntRegion = true
        log info s"RUNT_REGION (will delete later) $tag"
      }

      _channel.close()
      _channel = null

      _isOpenForWrites = false
      _isOnDisk = true

      FabricRegionReporter.countWriteClose()

    }
  }

  override def waitForWritesToComplete(): Unit = {
    lazy val tag = s"FabricRegionWriter.waitForWritesToComplete($parameters)"
    if (!isOpenForWrites) throw VitalsException(s"REGION_WRITE_WAIT_NOT_OPEN! $tag ")
    try {
      _completed synchronized {
        while (!_completed.get) {
          _completed.wait()
        }
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"REGION_WRITE_WAIT_FAIL $t $tag", t)
        throw t
    }
  }


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected def markCompleted(): Unit = {
    lazy val tag = s"FabricRegionWriter.markCompleted($parameters)"
    if (!isOpenForWrites) throw VitalsException(s"REGION_MARK_COMPLETE_NOT_OPEN $tag")
    try {
      _completed synchronized {
        _completed.set(true)
        _completed.notifyAll()
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"REGION_MARK_COMPLETE_FAIL $t $tag", t)
        throw t
    }
  }

  private def writeHeader(): Unit = {
    lazy val tag = s"FabricRegionWriter.writeHeader($parameters)"
    try {
      _lastWriteFuture = _channel.write(ByteBuffer.wrap(Array(RegionMagic, RegionVersion)), _writePtr)
    } catch safely {
      case t: Throwable =>
        val msg = s"REGION_WRITE_HEADER_FAIL $t $tag"
        log error burstStdMsg(msg)
        throw VitalsException(msg, t)
    }
  }

  protected def logTooLargeMessage: String = {
    val msg = burstStdMsg(s"region $filePath over(${
      snap.metadata.datasource.domain.domainKey
    }, ${
      snap.metadata.datasource.view.viewKey
    }) max size (${Integer.MAX_VALUE}) exceeded for region '$filePath' during write")
    log error msg
    msg
  }

  /**
   * we can only mmap Integer.MAX_VALUE size regions so catch it here in the write even though
   * writes can be essentially any size...
   */
  protected
  def checkFileSize(): Unit = {
    if (_channel.size >= Integer.MAX_VALUE) throw VitalsException(logTooLargeMessage)
  }

  override def elapsedNs: Long = _elapsedNs.get

  override def parcelCount: Long = _parcelCount.sum

  override def itemCount: Long = _itemCount.sum

  override def inflatedByteCount: Long = _inflatedByteCount.sum

  override def deflatedByteCount: Long = _deflatedByteCount.sum

  override def syncWaitNs: Long = -1

  override def ioWaitNs: Long = -1
}
