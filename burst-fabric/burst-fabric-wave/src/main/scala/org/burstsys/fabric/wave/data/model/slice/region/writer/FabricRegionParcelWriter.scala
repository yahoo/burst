/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.region.writer

import java.nio.ByteBuffer

import org.burstsys.fabric.wave.data.model.slice.region.FabricRegionReporter
import org.burstsys.tesla
import org.burstsys.tesla.parcel.{TeslaEndMarkerParcel, TeslaParcel}
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.instrument.{prettyRateString, prettyTimeFromNanos}
import org.burstsys.vitals.logging._

/**
 * parcel write operations on a cache region
 */
trait FabricRegionParcelWriter extends FabricRegionWriter {

  self: FabricRegionWriterContext =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @transient private[this]
  val _parcelQueue: FabricRegionParcelWriteQueue = new FabricRegionParcelWriteQueue(regionParcelWriteQueueSize)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Api
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override def queueParcelForWrite(parcel: TeslaParcel): Unit = {
    lazy val tag = s"FabricRegionParcelWriter.queueParcelForWrite($parameters)"
    if (!isOpenForWrites) throw VitalsException(s"REGION_QUEUE_NOT_OPEN $tag")
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

  final override def writeNextParcel: Long = {
    val tag = s"FabricRegionParcelWriter.writeNextParcel($parameters)"
    if (!isOpenForWrites) throw VitalsException(s"REGION_WRITE_NOT_OPEN $tag")
    try {
      val parcel: TeslaParcel = _parcelQueue.take
      parcel match {
        case TeslaEndMarkerParcel =>
          val elapsedNs = System.nanoTime - _start
          _elapsedNs.set(elapsedNs)
          log info s"REGION_WRITE_COMPLETE parcelCount=${_parcelCount.sum}, elapsedNs=$elapsedNs (${prettyTimeFromNanos(elapsedNs)}), (${prettyRateString("parcel", _parcelCount.sum, elapsedNs)}) $tag"
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

  private def writeParcelToDisk(parcel: TeslaParcel): Unit = {
    lazy val tag = s"FabricRegionParcelWriter.writeParcelToDisk($parameters)"
    if (!isOpenForWrites) throw VitalsException(s"REGION_DISK_WRITE_NOT_OPEN $tag")

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
      checkFileSize()
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

  final override def allParcelsQueuedForWrite(): Unit = {
    this queueParcelForWrite TeslaEndMarkerParcel
  }
}
