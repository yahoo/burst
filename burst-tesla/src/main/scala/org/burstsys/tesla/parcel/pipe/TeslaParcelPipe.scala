/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.pipe

import java.util.concurrent.{ArrayBlockingQueue, TimeUnit}

import org.burstsys.tesla.parcel.{TeslaParcel, _}
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid._

import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps

/**
 * TeslaParcelPipe is essentially a named queue for TeslaParcels.
 *
 * == Resource Cleanup ==
 * Parcel pipes are responsible for free'ing of memory for parcels put on the pipe.
 * Consumers are responsible for free'ing of memory for parcels taken from the pipe.
 */
trait TeslaParcelPipe extends VitalsService {

  /**
   *
   * @return
   */
  def pipeName: String

  /**
   * The fixed size of the internal queue
   *
   * @return
   */
  def depth: Int

  /**
   * The identity of this global request this pipe is part of
   *
   * @return
   */
  def guid: VitalsUid

  /**
   * The identity of this stream this pipe is part of
   *
   * @return
   */
  def suid: VitalsUid

  /**
   * put a parcel on the queue.
   *
   * @param chunk the parcel
   */
  def put(chunk: TeslaParcel): Unit

  /**
   * take a parcel off of the queue.
   *
   * @return
   */
  def take: TeslaParcel

  /**
   * the timeout for pipe 'takes'
   *
   * @return
   */
  def timeoutDuration: Duration

}

object TeslaParcelPipe {

  def apply(name: String, guid: VitalsUid, suid: VitalsUid, depth: Int = 100, timeout: Duration = 2 minutes): TeslaParcelPipe =
    TeslaParcelPipeContext(name, guid, suid, depth, timeout)

}

/**
 *
 */
private final case
class TeslaParcelPipeContext(pipeName: String, guid: VitalsUid, suid: VitalsUid, depth: Int, timeoutDuration: Duration)
  extends TeslaParcelPipe {

  private val ids = s"snap=$guid, suid=$suid"

  override def toString: VitalsUid = s"TeslaParcelPipe(name=$pipeName, $ids)"

  override def modality: VitalsServiceModality = VitalsPojo

  override def serviceName: String = s"TeslaParcelPipe(name=$pipeName, $ids)"

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // internal state
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  // TODO https://github.com/RobAustin/low-latency-primitive-concurrent-queues
  // TODO ConcurrentBlockingLongQueue might be great here but it needs an instance per thread pair - need to think...

  private[this] val _parcels = new ArrayBlockingQueue[java.lang.Long](depth)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PUT/GET API
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def put(parcel: TeslaParcel): Unit = {
    lazy val tag = s"TeslaParcelPipe.put name=$pipeName"
    try {
      ensureRunning
      if (parcel.status.isMarker) {
        log debug s"$tag action=PUT status=${parcel.status.statusName} $ids parcelQueueSize=${_parcels.size}"
      } else if (logNonStatusParcel(_parcels.size)) {
        log debug s"$tag action=PUT status=${parcel.status.statusName} $ids parcelQueueSize=${_parcels.size} buffers=${parcel.bufferCount}"
      }
      _parcels.put(parcel.blockPtr)
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"PARCEL_PIPE_FAIL $tag", t)
        throw t
    }
  }

  override def take: TeslaParcel = {
    lazy val tag = s"TeslaParcelPipe.take name=$pipeName"
    ensureRunning
    try {
      val ptr = _parcels.poll(timeoutDuration.toNanos, TimeUnit.NANOSECONDS)
      val parcel: TeslaParcel = if (ptr == null) TeslaTimeoutMarkerParcel else TeslaParcelAnyVal(ptr)
      if (parcel.status.isMarker) {
        log debug s"$tag action=TAKE status=${parcel.status.statusName} $ids parcelQueueSize=${_parcels.size}"
      } else if (logNonStatusParcel(_parcels.size)) {
        log debug s"$tag action=TAKE status=${parcel.status.statusName} $ids parcelQueueSize=${_parcels.size} buffers=${parcel.bufferCount}"
      }
      parcel
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"PARCEL_PIPE_FAIL $t $tag", t)
        throw t
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def start: this.type = {
    synchronized {
      ensureNotRunning
      markRunning
    }
    this
  }

  override def stop: this.type = {
    synchronized {
      ensureRunning
      markNotRunning
    }
    this
  }

}
