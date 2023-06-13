/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.packer

import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.{TeslaMutableBuffer, endMarkerMutableBuffer}
import org.burstsys.tesla.parcel.packer
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.uid._

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future, TimeoutException}
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

  def packerId: Int

  /** @return whether the packer is currently packing parcels */
  def running: Boolean

  /**
   * Put a buffer into a queue for packing into a parcel and feeding to the pipe.
   * The buffer is freed internally
   */
  def put(buffer: TeslaMutableBuffer): Unit

  def finishWrites(): Future[Unit]

}

private[packer] final case
class TeslaParcelPackerContext() extends AnyRef with TeslaParcelPacker {

  val packerId: Int = packerIdSource.getAndIncrement()

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private val packerThreadName = f"tesla-parcel-packer-$packerId%02d"

  private var _guid: VitalsUid = _

  private val _moreToPack = new AtomicBoolean(false)

  private var _pipe: TeslaParcelPipe = _

  private val _queue = new ArrayBlockingQueue[TeslaMutableBuffer](1e6.toInt)

  private var _backgroundProcess: Future[Unit] = _

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // background
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private def startProcessing(): Unit = {
    _moreToPack.set(true)
    _queue.clear()
    _backgroundProcess = TeslaRequestFuture {
      val name = Thread.currentThread.getName
      Thread.currentThread().setName(packerThreadName)

      var buffersInParcelTally = 0
      var bufferTally = 0
      var parcelTally = 0
      var startNanos = 0L
      var byteTally = 0L

      // get initial parcel
      var parcel = TeslaWorkerCoupler(tesla.parcel.factory.grabParcel(parcelMaxSize))

      // push parcel out to pipe and reset for more
      def pushParcel(): Unit = {
        buffersInParcelTally = 0
        parcelTally += 1
        if (packer.log.isDebugEnabled)
          packer.log.debug(s"TeslaParcelPacker pushed parcel=$parcel guid=${_guid} parcelTally=$parcelTally")
        _pipe put parcel
        parcel = tesla.parcel.factory.grabParcel(parcelMaxSize)
      }

      while (_moreToPack.get) {
        // grab the next reader result (wait on request thread)
        val buffer = _queue.take

        // do actual parcel packing on a worker thread
        TeslaWorkerCoupler {
          if (buffer == endMarkerMutableBuffer) {
            if (startNanos == 0L) {
              startNanos = System.nanoTime
            }

            // handle last runt parcel or possible empty parcel
            if (buffersInParcelTally > 0) {
              pushParcel()
            }

            val elapsedNanos = System.nanoTime - startNanos
            val bytesPerBuffer = if (bufferTally == 0) 0 else byteTally / bufferTally
            val bytesPerParcel = if (parcelTally == 0) 0 else byteTally / parcelTally
            if (packer.log.isDebugEnabled)
              packer.log debug
                s"""TeslaParcelPacker FINISHED ,
                   |  guid=${_guid} ,
                   |  elapsedNanos=$elapsedNanos (${prettyTimeFromNanos(elapsedNanos)}) ,
                   |  buffers=$bufferTally (${prettyRateString("buffer", bufferTally, elapsedNanos)}) ,
                   |  parcels=$parcelTally (${prettyRateString("parcel", parcelTally, elapsedNanos)}) ,
                   |  bytes=$byteTally (${prettyByteSizeString(byteTally)}) (${prettyRateString("byte", byteTally, elapsedNanos)}) ,
                   |  bytesPerBuffer=$bytesPerBuffer (${prettyByteSizeString(bytesPerBuffer)}) (${prettyByteSizeString(bytesPerBuffer)}) ,
                   |  bytesPerParcel=$bytesPerParcel (${prettyByteSizeString(bytesPerParcel)}) (${prettyByteSizeString(bytesPerParcel)}) ,
     """.stripMargin
            _moreToPack.set(false)

          } else {

            // update metrics
            if (startNanos == 0L) {
              startNanos = System.nanoTime
            }
            byteTally += buffer.currentUsedMemory
            bufferTally += 1
            buffersInParcelTally += 1

            // write the buffer
            if (parcel.writeNextBuffer(buffer) == -1) {
              pushParcel()
              parcel.startWrites()
              if (parcel.writeNextBuffer(buffer) == -1) {
                throw VitalsException(s"TeslaParcelPacker could not fit buffer in parcel byteTally=$byteTally")
              }
            }
            tesla.buffer.factory releaseBuffer buffer
          }
        } // worker thread
      } // while(moreToPack)
      Thread.currentThread.setName(name)
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override def running: Boolean = _backgroundProcess != null && !_backgroundProcess.isCompleted

  override def put(buffer: TeslaMutableBuffer): Unit = {
    if (!running) {
      packer.log warn s"PACKER_NOT_RUNNING TeslaParcelPacker.put called on instance that is not running id=$packerId guid=${_guid} pipe=${_pipe} worker=${_backgroundProcess}"
    }
    _queue add buffer
  }

  override def finishWrites(): Future[Unit] = {
    _queue add endMarkerMutableBuffer
    Await.ready(_backgroundProcess, Duration.Inf)
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  def open(guid: VitalsUid, pipe: TeslaParcelPipe): this.type = {
    _pipe = pipe
    _guid = guid
    startProcessing()
    if (packer.log.isDebugEnabled)
      packer.log.debug(s"TeslaParcelPacker OPEN guid=${_guid} pipe=${_pipe} worker=${_backgroundProcess}")
    this
  }

  def close: this.type = {
    _queue add endMarkerMutableBuffer
    if (packer.log.isDebugEnabled)
      packer.log.debug(s"TeslaParcelPacker CLOSING guid=${_guid} pipe=${_pipe} worker=${_backgroundProcess}")

    // wait for this run to finish
    while (_moreToPack.get) {
      try {
        Await.ready(_backgroundProcess, 10.milliseconds)
      } catch safely {
        case _: TimeoutException => // ignore this and wait for the press to complete
      }
    }
    if (packer.log.isDebugEnabled)
      packer.log.debug(s"TeslaParcelPacker CLOSED guid=${_guid} pipe=${_pipe} worker=${_backgroundProcess}")
    this
  }

}
