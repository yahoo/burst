/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.tesla.TeslaTypes.TeslaMemoryPtr
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.VitalsByteQuantReporter

import scala.language.implicitConversions

package object parcel extends VitalsLogger {

  final val partName: String = "parcel"

  object TeslaParcelReporter extends VitalsByteQuantReporter("tesla", "parcel")

  implicit def longToParcel(data: Long): TeslaParcel = TeslaParcelAnyVal(data)

  implicit def parcelToLong(parcel: TeslaParcel): TeslaParcel = parcel.blockPtr

  /**
   * the status associated with this standard or faux parcel
   *
   * @param statusMarker the marker status 'ptr' - this must be a negative number (so it is not mistaken
   *                     for a real off heap memory pointer)
   */
  sealed case
  class TeslaParcelStatus(statusMarker: Int) {

    def statusName: String = "normal"

    def isMarker: Boolean = false

    def isError: Boolean = false

    def isEnd: Boolean = false

    def isHeartbeat: Boolean = false

    final override def toString: String = statusName
  }

  /**
   * Nothing wrong
   */
  object TeslaNormalStatus extends TeslaParcelStatus(0)

  /**
   * Nothing wrong
   */
  object TeslaEndStatus extends TeslaParcelStatus(-100) {
    override val statusName = "end-marker"
    override val isMarker = true
    override val isEnd = true
  }

  /**
   * Timeout
   */
  object TeslaTimeoutStatus extends TeslaParcelStatus(-200) {
    override val statusName = "timeout-marker"
    override val isMarker = true
    override val isError = true
  }

  /**
   * unknown runtime exception
   */
  object TeslaExceptionStatus extends TeslaParcelStatus(-300) {
    override val statusName = "exception-marker"
    override val isMarker = true
    override val isError = true
  }

  /**
   * No Data
   */
  object TeslaNoDataStatus extends TeslaParcelStatus(-400) {
    override val statusName = "nodata-marker"
    override val isMarker = true
    override val isError = false
  }

  /**
   * Mock
   */
  object TeslaMockStatus extends TeslaParcelStatus(-500) {
    override val statusName = "mock-parcel"
    override val isMarker = false
    override val isError = false
  }

  /**
   * abort propogation
   */
  object TeslaAbortStatus extends TeslaParcelStatus(-600) {
    override val statusName = "abort-marker"
    override val isMarker = true
    override val isError = true
  }

  /**
   * Heartbeat
   */
  object TeslaHeartbeatStatus extends TeslaParcelStatus(-700) {
    override val statusName = "heartbeat-marker"
    override val isMarker = true
    override val isError = false
    override val isHeartbeat = true
  }

  // TODO add other error conditions here...

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // prevent long -> parcel object conversions
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * parcels are value classes with a memory ptr to off heap memory. If that ptr is negative,
   * it is a '''statusMarker''' which this routine converts to a status code. This allows us to pass
   * parcels around that have special meaning to the pipeline
   *
   * @param statusMarker a real memory ptr or a possible status marker ptr
   * @return a status [[TeslaNormalStatus]] if this is a real memory ptr or a suitable
   *         [[TeslaParcelStatus]] if not
   */
  implicit def ptrToParcelStatus(statusMarker: TeslaMemoryPtr): TeslaParcelStatus = {
    if (statusMarker > 0) TeslaNormalStatus
    else
      statusMarker match {
        case TeslaAbortStatus.statusMarker => TeslaAbortStatus
        case TeslaEndStatus.statusMarker => TeslaEndStatus
        case TeslaHeartbeatStatus.statusMarker => TeslaHeartbeatStatus
        case TeslaMockStatus.statusMarker => TeslaMockStatus
        case TeslaNoDataStatus.statusMarker => TeslaNoDataStatus
        case TeslaTimeoutStatus.statusMarker => TeslaTimeoutStatus
        case TeslaExceptionStatus.statusMarker => TeslaExceptionStatus
        case _ =>
          throw VitalsException(s"ptrToParcelStatus($statusMarker) unknown marker ptr")
      }
  }

  /**
   * parcels are value classes with a memory ptr to off heap memory. If that ptr is negative,
   * it is a '''statusMarker''' which this routine converts to a status code. This allows us to pass
   * parcels around that have special meaning to the pipeline
   *
   * @param statusMarker a real memory ptr or a possible status marker ptr
   * @return if statusMarker is a real memory pointer then throw an exception, otherwise
   *         return a suitable marker parcel
   */
  def ptrToMarkerParcel(statusMarker: TeslaMemoryPtr): TeslaParcel = {
    if (statusMarker > 0)
      throw VitalsException(s"ptrToMarkerParcel($statusMarker) should not use for non marker parcels")
    else
      statusMarker match {
        case TeslaAbortStatus.statusMarker => TeslaAbortMarkerParcel
        case TeslaEndStatus.statusMarker => TeslaEndMarkerParcel
        case TeslaHeartbeatStatus.statusMarker => TeslaHeartbeatMarkerParcel
        case TeslaMockStatus.statusMarker => TeslaMockMarkerParcel
        case TeslaNoDataStatus.statusMarker => TeslaNoDataMarkerParcel
        case TeslaTimeoutStatus.statusMarker => TeslaTimeoutMarkerParcel
        case TeslaExceptionStatus.statusMarker => TeslaExceptionMarkerParcel
        case _ =>
          throw VitalsException(s"ptrToMarkerParcel($statusMarker) unknown marker ptr")
      }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // marker parcels that trigger special handling
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * This indicates normal end of a successful stream of data over pipe.
   * Most pipe transfers end by placing one of these on the end of the queue
   */
  final val TeslaEndMarkerParcel:TeslaParcel = TeslaParcelAnyVal(TeslaEndStatus.statusMarker)

  /**
   * used for unit tests
   */
  final val TeslaMockMarkerParcel:TeslaParcel = TeslaParcelAnyVal(TeslaMockStatus.statusMarker)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // marker parcels that represent an error
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * this indicates a stream that got started but in the end had no data
   * associated with it. This can only be sent if __no other__ data has been
   * sent. It is a fatal error to send this after other data has been sent.
   * Do not send a [[TeslaEndMarkerParcel]] after this...
   */
  final val TeslaNoDataMarkerParcel:TeslaParcel = TeslaParcelAnyVal(TeslaNoDataStatus.statusMarker)

  /**
   * This indicates that there was a timeout while waiting for the producer
   * to send data across
   */
  final val TeslaTimeoutMarkerParcel:TeslaParcel = TeslaParcelAnyVal(TeslaTimeoutStatus.statusMarker)

  /**
   * This indicates that there was a runtime exception
   */
  final val TeslaExceptionMarkerParcel:TeslaParcel = TeslaParcelAnyVal(TeslaExceptionStatus.statusMarker)

  /**
   * abort propogation
   */
  final val TeslaAbortMarkerParcel:TeslaParcel = TeslaParcelAnyVal(TeslaAbortStatus.statusMarker)

  /**
   * heartbeat marker
   */
  final val TeslaHeartbeatMarkerParcel:TeslaParcel = TeslaParcelAnyVal(TeslaHeartbeatStatus.statusMarker)

  /**
   * support parcel status management
   */
  trait TeslaParcelStatusMarker extends Any {

    /**
     * we get this from the parcel
     *
     * @return
     */
    def blockPtr: TeslaMemoryPtr

    /**
     * return the status code associated with this parcel. Positive numbers
     * are memory ptrs, negative numbers are special faux marker parcels
     *
     * @return
     */
    def status: TeslaParcelStatus = ptrToParcelStatus(blockPtr)

  }

}
