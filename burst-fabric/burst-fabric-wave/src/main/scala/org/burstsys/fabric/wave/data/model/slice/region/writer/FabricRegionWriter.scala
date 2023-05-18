/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.region.writer

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.{NoSuchFileException, Path, StandardOpenOption}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong, LongAdder}
import org.burstsys.fabric.wave.data.model.slice.region.hose.FabricWriteMetrics
import org.burstsys.fabric.wave.data.model.slice.region.{FabricRegion, FabricRegionIdentity, FabricRegionReporter, FabricRegionTag, RegionMagic, RegionVersion}
import org.burstsys.fabric.wave.data.model.snap.{FabricSnap, FabricSnapComponent}
import org.burstsys.fabric.wave.data.worker.pump.{FabricCacheImpeller, FabricCacheIntake}
import org.burstsys.tesla
import org.burstsys.tesla.parcel.{TeslaEndMarkerParcel, TeslaParcel}
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.instrument.{prettyRateString, prettyTimeFromNanos}

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
                               ) extends FabricRegionWriter {

  lazy val parameters = s"guid=${snap.guid}, regionIndex=${region.regionIndex}, file=${filePath}"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private var _channel: AsynchronousFileChannel = _

  private var _writePtr: Long = _

  private final val _completed = new AtomicBoolean(false)

  private var _lastWriteFuture: java.util.concurrent.Future[Integer] = _

  private val _isRuntRegion = new AtomicBoolean(false)

  private val _isOpenForWrites = new AtomicBoolean(false)

  private val _isOnDisk = new AtomicBoolean(false)

  private val _start: Long = System.nanoTime

  private val _elapsedNs = new AtomicLong

  private val _parcelCount = new LongAdder

  private val _itemCount = new LongAdder

  private val _inflatedByteCount = new LongAdder

  private val _deflatedByteCount = new LongAdder

  @transient
  private val _parcelQueue: FabricRegionParcelWriteQueue = new FabricRegionParcelWriteQueue(regionParcelWriteQueueSize)


  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Api
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def wireSnap(s: FabricSnap): Unit = snap = s

  override
  def regionIsRunt: Boolean = _isRuntRegion.get

  override
  def isOpenForWrites: Boolean = _isOpenForWrites.get

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def openForWrites(): Unit = synchronized {
    lazy val tag = s"FabricRegionWriter.open($parameters)"
    synchronized {
      if (_isOpenForWrites.get) {
        throw VitalsException(s"REGION_WRITE_OPEN_ALREADY $tag")
      }
      try {
        log debug s"REGION_WRITE_OPEN_START $tag"
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
      _isOpenForWrites.set(true)
    }
  }

  override def queueParcelForWrite(parcel: TeslaParcel): Unit = {
    lazy val tag = s"FabricRegionParcelWriter.queueParcelForWrite($parameters)"
    if (!isOpenForWrites) {
      throw VitalsException(s"REGION_QUEUE_NOT_OPEN $tag")
    }
    try {
      // queue up a single additional buffer
      _parcelQueue put parcel
      // and forward the task to our assigned impeller to push through it's IO queue
      impeller impel this
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"REGION_QUEUE_FAIL $t $tag", t)
        throw t
    }
  }

  private val zeroBytes = 0L

  override def writeNextParcel: Long = {
    val tag = s"FabricRegionParcelWriter.writeNextParcel($parameters)"
    if (!isOpenForWrites) throw VitalsException(s"REGION_WRITE_NOT_OPEN $tag")
    try {
      val parcel: TeslaParcel = _parcelQueue.take
      parcel match {
        case TeslaEndMarkerParcel =>
          val elapsedNs = System.nanoTime - _start
          _elapsedNs.set(elapsedNs)
          log debug s"REGION_WRITE_COMPLETE parcelCount=${_parcelCount.sum}, elapsedNs=$elapsedNs (${prettyTimeFromNanos(elapsedNs)}), (${prettyRateString("parcel", _parcelCount.sum, elapsedNs)}) $tag"
          markCompleted()
          zeroBytes

        case _ =>
          val byteSize = parcel.inflatedSize
          _parcelCount.add(1)
          _itemCount.add(parcel.bufferCount)
          writeParcelToDisk(parcel) // this call releases buffer
          FabricRegionReporter.countParcelWrite(byteSize)
          byteSize
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"REGION_WRITE_FAIL $t $tag", t)
        throw t
    }
  }

  override def allParcelsQueuedForWrite(): Unit = {
    this queueParcelForWrite TeslaEndMarkerParcel
  }

  override def closeForWrites(): Unit = {
    lazy val tag = s"FabricRegionWriter.close($parameters)"
    synchronized {
      if (!_isOpenForWrites.get) {
        throw VitalsException(s"REGION_WRITE_CLOSE_NOT_OPEN! $tag ")
      }
      if (!_completed.get) {
        log.warn(s"$tag region not marked as complete")
      }

      if (_parcelCount.sum == 0) {
        _isRuntRegion.set(true)
        log debug s"RUNT_REGION (will delete later) $tag"
      }

      _channel.close()
      _channel = null

      _isOpenForWrites.set(false)
      _isOnDisk.set(true)

      FabricRegionReporter.countWriteClose()

    }
  }

  override def waitForWritesToComplete(): Unit = {
    lazy val tag = s"FabricRegionWriter.waitForWritesToComplete($parameters)"
    if (!isOpenForWrites) {
      throw VitalsException(s"REGION_WRITE_WAIT_NOT_OPEN! $tag ")
    }
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

  private def markCompleted(): Unit = {
    lazy val tag = s"FabricRegionWriter.markCompleted($parameters)"
    if (!isOpenForWrites) {
      throw VitalsException(s"REGION_MARK_COMPLETE_NOT_OPEN $tag")
    }
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

  private def writeParcelToDisk(parcel: TeslaParcel): Unit = {
    lazy val tag = s"FabricRegionParcelWriter.writeParcelToDisk($parameters)"
    if (!isOpenForWrites) {
      throw VitalsException(s"REGION_DISK_WRITE_NOT_OPEN $tag")
    }

    // the actual write to disk of a NIO buffer
    @inline def writeBuffer(buffer: ByteBuffer): Unit = {
      TeslaRequestCoupler { // give up the worker thread while we wait for ASYNC IO operation
        val oldName = Thread.currentThread().getName
        Thread.currentThread().setName(s"fab-disk-writer")
        try {
          while (buffer.hasRemaining) {
            _writePtr += _lastWriteFuture.get
            _lastWriteFuture = _channel.write(buffer, _writePtr)
          }
        } finally Thread.currentThread().setName(oldName)
      }
    }

    try {
      if (_channel.size >= Integer.MAX_VALUE) {
        val domainKey = snap.metadata.datasource.domain.domainKey
        val viewKey = snap.metadata.datasource.view.viewKey
        val msg = burstStdMsg(s"region $filePath over($domainKey, $viewKey) max size (${Integer.MAX_VALUE}) exceeded for region '$filePath' during write")
        log error msg
        throw VitalsException(msg)
      }
      if (parcel.isInflated) {
        _deflatedByteCount.add(parcel.deflatedSize)
        _inflatedByteCount.add(parcel.inflatedSize)
        writeBuffer(parcel.asByteBuffer)
      } else {
        val director = tesla.director.factory.grabDirector(parcel.inflatedSize)
        try {
          val inflateStart = System.nanoTime
          parcel.inflateTo(director.payloadPtr)
          _deflatedByteCount.add(parcel.deflatedSize)
          _inflatedByteCount.add(parcel.inflatedSize)
          FabricRegionReporter.recordParcelInflate(System.nanoTime - inflateStart, parcel.deflatedSize, parcel.inflatedSize)
          val buffer = director.directBuffer
          buffer limit parcel.inflatedSize
          writeBuffer(buffer)
        } finally tesla.director.factory releaseDirector director
      }

    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"REGION_DISK_WRITE_FAIL $t $tag", t)
        throw t
    } finally tesla.parcel.factory releaseParcel parcel
  }

  override def elapsedNs: Long = _elapsedNs.get

  override def parcelCount: Long = _parcelCount.sum

  override def itemCount: Long = _itemCount.sum

  override def inflatedByteCount: Long = _inflatedByteCount.sum

  override def deflatedByteCount: Long = _deflatedByteCount.sum

  override def syncWaitNs: Long = -1

  override def ioWaitNs: Long = -1
}
