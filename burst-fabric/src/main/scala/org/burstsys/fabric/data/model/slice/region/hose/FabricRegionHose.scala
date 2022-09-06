/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.slice.region.hose

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousFileChannel, CompletionHandler}
import java.nio.file.{Files, NoSuchFileException, Path, StandardOpenOption}
import java.util.concurrent.atomic.{AtomicBoolean, LongAdder}
import java.util.concurrent.{ArrayBlockingQueue, TimeUnit}

import org.burstsys.fabric.data.model.slice.region._
import org.burstsys.fabric.data.model.snap.{FabricSnap, FabricSnapComponent}
import org.burstsys.tesla
import org.burstsys.tesla.parcel.{TeslaParcel, _}
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.burstsys.vitals.logging._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * write pipeline for a single region file
 */
trait FabricRegionHose extends Any with FabricWriteMetrics with FabricSnapComponent {

  /**
   * open a [[AsynchronousFileChannel]] for the write and start up pipeline processing thread.
   * Deletes a pre existing file of the same name.
   */
  def open(): Unit

  /**
   * queue an asynchronous parcel write, inflation if needed, of a parcel to a
   * file channel. Logs a WARN message if the put takes more time than
   * a predetermined SLOW duration.
   *
   * @param parcel
   * @return
   */
  def putParcel(parcel: TeslaParcel): Unit

  /**
   *
   * @return
   */
  def regionIsRunt: Boolean

  /**
   * puts a [[TeslaEndMarkerParcel]] on the queue and
   * then returns a future that completes when  all parcels are written to disk and hose resources released
   */
  def close: Future[Unit]

}

object FabricRegionHose {

  def apply(snap: FabricSnap, region: FabricRegion, regionIndex: Int, regionTag: FabricRegionTag, filePath: Path): FabricRegionHose =
    FabricRegionHoseContext(snap: FabricSnap, region: FabricRegion, regionIndex: Int, regionTag: FabricRegionTag, filePath: Path)

}

final case
class FabricRegionHoseContext(var snap: FabricSnap, region: FabricRegion, regionIndex: Int, regionTag: FabricRegionTag, regionPath: Path)
  extends FabricRegionHose {

  protected
  lazy val parameters = s"guid=${snap.guid}, ${snap.slice.identity}, regionIndex=$regionIndex, regionPath=$regionPath"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _writePtr: Int = _

  private[this]
  var _channel: AsynchronousFileChannel = _

  private[this]
  val _queue = new ArrayBlockingQueue[TeslaParcel](queuePutSize)

  private[this]
  val _isOpen = new AtomicBoolean(false)

  private[this]
  val _moreParcelsComing = new AtomicBoolean(false)

  private[this]
  val _parcelCount = new LongAdder

  private[this]
  val _itemCount = new LongAdder

  private[this]
  val _syncWait = new LongAdder

  private[this]
  val _ioWait = new LongAdder

  private[this]
  val _finalPromise = Promise[Unit]()

  private[this]
  var _start: Long = _

  private[this]
  var _finish: Long = _

  private[this]
  val _inflatedByteCount = new LongAdder

  private[this]
  val _deflatedByteCount = new LongAdder

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def wireSnap(s: FabricSnap): Unit = snap = s

  override def inflatedByteCount: Long = _inflatedByteCount.sum()

  override def deflatedByteCount: Long = _deflatedByteCount.sum()

  override def elapsedNs: Long = _finish - _start

  override def parcelCount: Long = _parcelCount.sum().toInt

  override def itemCount: Long = _itemCount.sum()

  override def syncWaitNs: Long = _syncWait.sum()

  override def ioWaitNs: Long = _ioWait.sum()

  override def regionIsRunt: Boolean = _itemCount.sum() == 0

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def open(): Unit = {
    lazy val tag = s"FabricHose.open($parameters)"
    try {
      if (debugHose) log info s"HOSE_OPEN $tag"
      if (Files.exists(regionPath)) {
        log info s"ALREADY_EXTENT_REGION_FILE (deleting...) $tag"
        Files.deleteIfExists(regionPath)
      }
      _channel = AsynchronousFileChannel.open(
        regionPath,
        StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW
      )
      // write the header synchronously
      _writePtr = _channel.write(ByteBuffer.wrap(Array(RegionMagic, RegionVersion)), 0).get()
      _isOpen.set(true)
      _moreParcelsComing.set(true)
      processor()
      _start = System.nanoTime()
    } catch safely {
      case t: NoSuchFileException => throw VitalsException(s"HOSE_PATH_DOES_NOT_EXIST $t $tag", t)
      case t: Throwable => throw VitalsException(s"HOSE_CANT_BE_CREATED $t $tag", t)
    }
  }

  override
  def putParcel(parcel: TeslaParcel): Unit = {
    val tag = s"FabricHose.putParcel($parameters)"
    if (debugHose) log info s"HOSE_PUT_PARCEL $tag"
    checkOpen()
    var continue = true
    var slowCount = 0
    while (continue) {
      if (_queue.offer(parcel, queuePutWait.toNanos, TimeUnit.NANOSECONDS)) {
        continue = false
      } else {
        slowCount += 1
        log warn burstStdMsg(s"HOSE_SLOW_PUT (slowCount=$slowCount after $queuePutWait...) $tag")
      }
    }
  }

  override
  def close: Future[Unit] = {
    val tag = s"FabricHose.close($parameters)"
    checkOpen()
    putParcel(TeslaEndMarkerParcel)
    _finalPromise.future onComplete {
      case Failure(t) =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        _channel.close()
        _isOpen.set(false)
      case Success(()) =>
        if (debugHose)
          log info
            s"""|
                |HOSE_CLOSE_SUCCESS
                |$sprayMetrics
                |   syncWait=${syncWaitNs} (${prettyTimeFromNanos(syncWaitNs)})
                |   ioWait=${ioWaitNs} (${prettyTimeFromNanos(ioWaitNs)})
                |$tag""".stripMargin
        _channel.close()
        _isOpen.set(false)
    }
    _finalPromise.future
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // implementation
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * this is a loop running on a request thread, that takes parcels off the queue
   * and pushes them through a possible inflation via a parallel inflation worker process, and then an
   * async NIO disk write. For each parcel write, we capture the asyn op's future and make sure that the
   * next parcel write does a block-wait on that future to make sure writes are serialized. If the write ops can keep up
   * with the speed of the incoming queue this wait approaches zero i.e. the queue wait overlaps the write IO.
   * If we receive a [[TeslaEndMarkerParcel]] as a special marker parcel, then that is our signal that no more
   * parcels are coming. We wait for the last async write to complete and exit the thread. Generally
   * the inflation pipeline can keep up with 2020 era server class single SSD disk subsystems.
   */
  private
  def processor(): Unit = {
    lazy val tag = s"FabricHose.processor($parameters)"
    log info s"START_HOSE $tag "
    TeslaRequestFuture {
      _moreParcelsComing.set(true)
      try { // first one is ready to go
        var lastWrite = Promise[Unit]().success((): Unit).future
        while (_moreParcelsComing.get()) {
          val parcel = _queue.take()
          if (parcel == TeslaEndMarkerParcel) {
            if (debugHose) log info s"END_HOSE $tag"
            _moreParcelsComing.set(false)
          } else {
            val syncStart = System.nanoTime()
            // wait for last async write to complete
            Await.ready(lastWrite, atMost = Duration.Inf)
            _syncWait add System.nanoTime() - syncStart
            lastWrite = writeToPipeline(parcel)
            _parcelCount.increment()
          }
        }
        // make sure last write is complete
        lastWrite onComplete {
          case Failure(t) =>
            _finish = System.nanoTime()
            _finalPromise.failure(t)
          case Success(()) =>
            _finish = System.nanoTime()
            _finalPromise.success((): Unit)
        }
        if (debugHose) log info s"$tag done"
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(s"FAIL $t $tag", t)
          _isOpen.set(false)
          _moreParcelsComing.set(false)
          _finalPromise.failure(t)
      }
    }
  }

  private
  def writeToPipeline(parcel: TeslaParcel): Future[Unit] = {
    lazy val tag = s"FabricHose.writeParcelToDisk($parameters)"
    if (debugHose) log info s"HOSE_WRITE $tag"
    checkOpen()
    val promise = Promise[Unit]()
    _inflatedByteCount add parcel.inflatedSize
    _deflatedByteCount add parcel.deflatedSize
    _itemCount add parcel.bufferCount
    try {
      checkFileSize()
      if (parcel.isInflated) {
        if (debugHose) log info s"WRITE_ALREADY_INFLATED_PARCEL $tag"
        writeBufferToDisk(parcel.asByteBuffer) onComplete {
          case Failure(t) =>
            log error burstStdMsg(s"FAIL $t $tag", t)
            promise.failure(t)
          case Success(r) => promise.success(r)
        }
      } else {
        if (debugHose) log info s"INFLATE_PARCEL $tag"
        inflate(parcel) onComplete {
          case Failure(t) => promise.failure(t) // director freed by inflate()
          case Success(director) =>
            val buffer = director.directBuffer
            buffer limit parcel.inflatedSize
            if (debugHose) log info s"WRITE_INFLATED_PARCEL $tag "
            writeBufferToDisk(buffer) onComplete {
              case Failure(t) =>
                tesla.director.factory releaseDirector director
                log error burstStdMsg(s"FAIL $t $tag", t)
                promise.failure(t)
              case Success(r) =>
                tesla.director.factory releaseDirector director
                promise.success(r)
            }
        }
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        promise.failure(t)
    } finally tesla.parcel.factory releaseParcel parcel
    promise.future
  }

  /**
   * async write to disk of a NIO buffer
   *
   * @param buffer
   * @return future for IO op completion
   */
  private
  def writeBufferToDisk(buffer: ByteBuffer): Future[Unit] = {
    lazy val tag = s"FabricHose.writeBufferToDisk($parameters)"
    if (debugHose)
      log info s"HOSE_WRITE_TO_DISK $tag"
    val promise = Promise[Unit]()
    checkOpen()
    // make sure our async disk IO is on a request thread
    val ioStart = System.nanoTime()
    _channel.write(buffer, _writePtr, buffer, new CompletionHandler[Integer, ByteBuffer] {
      override def completed(bytesWritten: Integer, buffer: ByteBuffer): Unit = {
        if (buffer.hasRemaining) throw VitalsException(s"BUFFER_NOT_EMPTY! $tag")
        _writePtr += bytesWritten
        val ioElapsed = System.nanoTime - ioStart
        FabricHoseReporter.sampleParcelWrite(ioElapsed, bytesWritten)
        _ioWait add ioElapsed
        promise.success((): Unit)
      }

      override def failed(t: Throwable, attachment: ByteBuffer): Unit = {
        log error burstStdMsg(s"FAIL $t $tag", t)
        promise.failure(t)
      }
    })
    promise.future
  }

  /**
   * we can only mmap Integer.MAX_VALUE size regions so catch it here in the write even though
   * writes can be essentially any size...
   */
  private
  def checkFileSize(): Unit =
    if (_channel.size >= Integer.MAX_VALUE) throw VitalsException(s"write file size cannot exceed ${Integer.MAX_VALUE} bytes")

  private
  def checkOpen(): Unit =
    if (!_isOpen.get())
      throw VitalsException(s"not open!!")

}
