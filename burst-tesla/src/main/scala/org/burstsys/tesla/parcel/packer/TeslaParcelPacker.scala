/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.packer

import java.util.concurrent.ArrayBlockingQueue

import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.{TeslaMutableBuffer, endMarkerMutableBuffer}
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.instrument._
import org.burstsys.vitals.uid._

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * ==Description:==
 * A parallel runtime machine that packs [[org.burstsys.tesla.buffer.TeslaBuffer]] instances into
 * [[org.burstsys.tesla.parcel.TeslaParcel]] instances and feeds them into a
 * [[org.burstsys.tesla.parcel.pipe.TeslaParcelPipe]].
 * Note that no new objects are created other than the parcels
 * ==Usage:==
 * {{{
 *     val packer = tesla.parcel.packer.grabPacker(pipe)
 *     try {
 *       while(more_buffers)
 *         packer.put(a_buffer)
 *     } finally tesla.parcel.packer.releasePacker(packer)
 * }}}
 */
trait TeslaParcelPacker extends Any {

  /**
   * put a buffer into a queue for packing into a parcel and feeding to the pipe
   * The buffer is freed internally
   *
   * @param buffer
   */
  def put(buffer: TeslaMutableBuffer): Unit

}

private[packer] final case
class TeslaParcelPackerContext() extends AnyRef with TeslaParcelPacker {

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _guid: VitalsUid = _

  private[this]
  var _isActive: Boolean = false

  private[this]
  var _pipe: TeslaParcelPipe = _

  private[this]
  val _queue = new ArrayBlockingQueue[TeslaMutableBuffer](1e6.toInt)

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // background
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  lazy val future: Future[Unit] = TeslaRequestFuture {
    Thread.currentThread().setName(f"tesla-parcel-packer-${packerId.getAndIncrement()}%02d")

    var buffersInParcelTally = 0
    var bufferTally = 0
    var parcelTally = 0
    var startNanos = 0L
    var byteTally = 0L

    // get initial parcel
    var parcel = TeslaWorkerCoupler(tesla.parcel.factory grabParcel parcelMaxSize)

    // push parcel out to pipe and reset for more
    def pushParcel(): Unit = {
      buffersInParcelTally = 0
      parcelTally += 1
      _pipe put parcel
      parcel = tesla.parcel.factory grabParcel parcelMaxSize
    }

    while (true) {
      // grab the next reader result (wait on request thread)
      val buffer = _queue.take

      // do actual parcel packing on a worker thread
      TeslaWorkerCoupler {
        if (buffer == endMarkerMutableBuffer) {
          if (startNanos == 0L) startNanos = System.nanoTime

          // handle last runt parcel or possible empty parcel
          if (buffersInParcelTally > 0) pushParcel()
          val elapsedNanos = System.nanoTime - startNanos
          val bytesPerBuffer = if (bufferTally == 0) 0 else byteTally / bufferTally
          val bytesPerParcel = if (parcelTally == 0) 0 else byteTally / parcelTally
          log info
            s"""
               |TeslaParcelPacker FINISHED ,
               |  guid=${_guid} ,
               |  elapsedNanos=$elapsedNanos (${prettyTimeFromNanos(elapsedNanos)}) ,
               |  buffers=$bufferTally (${prettyRateString("buffer", bufferTally, elapsedNanos)}) ,
               |  parcels=$parcelTally (${prettyRateString("parcel", parcelTally, elapsedNanos)}) ,
               |  bytes=$byteTally (${prettyByteSizeString(byteTally)}) (${prettyRateString("byte", byteTally, elapsedNanos)}) ,
               |  bytesPerBuffer=$bytesPerBuffer (${prettyByteSizeString(bytesPerBuffer)}) (${prettyByteSizeString(bytesPerBuffer)}) ,
               |  bytesPerParcel=$bytesPerParcel (${prettyByteSizeString(bytesPerParcel)}) (${prettyByteSizeString(bytesPerParcel)}) ,
             """.stripMargin
          bufferTally = 0
          parcelTally = 0
          startNanos = 0L
          byteTally = 0L
          synchronized {
            _isActive = false
            notifyAll()
          }
        } else {

          // update metrics
          if (startNanos == 0L) startNanos = System.nanoTime
          byteTally += buffer.currentUsedMemory
          bufferTally += 1
          buffersInParcelTally += 1

          // write the buffer
          if (parcel.writeNextBuffer(buffer) == -1) {
            pushParcel()
            parcel.startWrites()
            if (parcel.writeNextBuffer(buffer) == -1)
              throw VitalsException(s"TeslaParcelPacker could not fit buffer in parcel byteTally=$byteTally")
          }
          tesla.buffer.factory releaseBuffer buffer
        }
      } // worker thread

    } // while loop
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def put(buffer: TeslaMutableBuffer): Unit = {
    _queue add buffer
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  def open(guid: VitalsUid, pipe: TeslaParcelPipe): this.type = {
    _pipe = pipe
    _guid = guid
    _isActive = true
    future
    this
  }

  def close: this.type = {
    _queue add endMarkerMutableBuffer
    // wait for this run to finish
    synchronized {
      while (_isActive)
        wait(10) // MS
    }
    this
  }

}
