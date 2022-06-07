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
 * == Resource Cleanup ==
 * For producers, parcel pipes are responsible for free'ing of memory once put on the
 * queue. For clients, the consumer is responsible for free'ing of memory
 * once taken off the queue.
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

  override
  def put(parcel: TeslaParcel): Unit = {
    lazy val tag = s"TeslaParcelPipe.put name=$pipeName"
    try {
      ensureRunning
      if (parcel.status.isMarker || logNonStatusParcel(_parcels.size)) log info s"$tag action=PUT status=${parcel.status.statusName} $ids ${_parcels.size}"
      _parcels.put(parcel.blockPtr)
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        throw t
    }
  }

  override
  def take: TeslaParcel = {
    lazy val tag = s"TeslaParcelPipe.take name=$pipeName"
    ensureRunning
    try {
      val ptr = _parcels.poll(timeoutDuration.toNanos, TimeUnit.NANOSECONDS)
      val parcel: TeslaParcel = if (ptr == null) TeslaTimeoutMarkerParcel else TeslaParcelAnyVal(ptr)
      if (parcel.status.isMarker || logNonStatusParcel(_parcels.size)) log info s"$tag action=TAKE status=${parcel.status.statusName} $ids ${_parcels.size}"
      parcel
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        throw t
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    synchronized {
      ensureNotRunning
      markRunning
    }
    this
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      markNotRunning
    }
    this
  }

}
