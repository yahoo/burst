/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter.slot

import io.opentelemetry.api.trace.Span
import org.burstsys.tesla.scatter._
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.uid._

import java.util.concurrent.locks.ReentrantLock

/**
 * one of n slots (requests) in a scatter
 */
trait TeslaScatterSlot extends Any {

  /**
   * the current state of the slot
   */
  def slotState: TeslaScatterSlotState

  /**
   * the scatter this slot is part of
   */
  def scatter: TeslaScatter

  /**
   * the request for this slot
   */
  def request: TeslaScatterRequest[_]

  /**
   * The fixed ID for this slot
   */
  def slotId: TeslaScatterSlotId

  /**
   * per request/slot request UID
   */
  def ruid: VitalsUid

  /**
   * the hostname for the destination of this request
   *
   * @return
   */
  def destinationHostName: VitalsHostName

  /**
   * last update for this slot
   */
  def lastUpdateNanos: Long

  def isTardy: Boolean = request.tardyAfter.isFinite && System.nanoTime - lastUpdateNanos > request.tardyAfter.toNanos

  /**
   * elapsed time in ns from begin to success or failure end
   *
   * @return
   */
  def elapsedNanos: Long

  /**
   * open the reusable slot
   *
   * @param request the request to be executed on this slot
   */
  def open(request: TeslaScatterRequest[_]): TeslaScatterSlot

  /**
   * close the reusable slot
   */
  def close: TeslaScatterSlot

  /**
   * callback to indicate the request has started
   */
  def slotBegin(): Unit

  /**
   * callback to indicate the request has made progress
   *
   * @param message an indication of the request's progress
   */
  def slotProgress(message: String): Unit

  /**
   * callback to indicate the request is stalled. This is a sort of inverse progress message.
   * Tardy messages are delivered at the cadence of `request.tardyAfter`
   */
  def slotTardy(): Unit

  /**
   * callback to indicate the request was retried on another worker
   */
  def slotRetry(): Unit

  /**
   * callback to indicate the request experienced an exception (abnormal exception).
   */
  def slotFailed(throwable: Throwable): Unit

  /**
   * callback to indicate the request was cancelled
   */
  def slotCancelled(): Unit

  /**
   * callback to indicate the request succeeded
   */
  def slotSuccess(): Unit

  /**
   * possible failure exception
   *
   * @return
   */
  def failure: Throwable

  def span:  Span

  def setSpan(span: Span): Unit

}

object TeslaScatterSlot {

  def apply(scatter: TeslaScatterContext, slotId: TeslaScatterSlotId): TeslaScatterSlot =
    TeslaScatterSlotContext(scatter: TeslaScatterContext, slotId: TeslaScatterSlotId)
}

private[scatter] final case
class TeslaScatterSlotContext(scatter: TeslaScatterContext, slotId: TeslaScatterSlotId)
  extends AnyRef with TeslaScatterSlot {

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATE
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _slotState: TeslaScatterSlotState = TeslaScatterSlotIdle

  private[this]
  var _ruid: VitalsUid = _

  private[this]
  var _destinationHostName: VitalsHostName = "NO HOST"

  private[this]
  var _request: TeslaScatterRequest[_] = _

  private[this]
  var _lastUpdateNanos: Long = -1L

  private[this]
  var _beginNanos: Long = -1L

  private[this]
  var _endNanos: Long = -1L

  private[this]
  val _gate = new ReentrantLock()

  private[this]
  val _begin = TeslaScatterSlotBegin(this)

  private[this]
  val _progress = TeslaScatterSlotProgress(this)

  private[this]
  val _succeed = TeslaScatterSlotSucceed(this)

  private[this]
  val _fail = TeslaScatterSlotFail(this)

  private[this]
  val _retry = TeslaScatterSlotRetry(this)

  private[this]
  val _tardy = TeslaScatterSlotTardy(this)

  private[this]
  val _cancelled = TeslaScatterSlotCancel(this)

  private[this]
  var _span:Span = _

  override
  def toString: String = s"slotId=$slotId, ruid=$ruid, destinationHostName='$destinationHostName', state=${_slotState}"

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // exceptions
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def failure: Throwable = _fail.throwable

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // locking
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private def lockStatusMsg: String = {
    if (_gate.isLocked)
      if (_gate.isHeldByCurrentThread)
        s"here ${_gate.getHoldCount}"
      else {
        val lockStr = _gate.toString
        lockStr.substring(lockStr.indexOf("@"))
      }
    else s"Unlocked"
  }

  def lockSlot(from: String = ""): this.type = {
    if (log.isDebugEnabled) log debug s"Slot#$slotId $from lock ($lockStatusMsg)"

    _gate.lock()

    if (log.isDebugEnabled) log debug s"Slot#$slotId $from locked ($lockStatusMsg)"

    this
  }

  def unlockSlot(from: String): this.type = {
    _gate.unlock()

    if (log.isDebugEnabled) log debug s"Slot#$slotId $from unlocked ($lockStatusMsg)"

    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override def elapsedNanos: Long = _endNanos - _beginNanos

  override def ruid: VitalsUid = _ruid

  override def destinationHostName: VitalsHostName = _destinationHostName

  override def request: TeslaScatterRequest[_] = _request

  def request_=(r: TeslaScatterRequest[_]): TeslaScatterRequest[_] = {
    _request = r
    r
  }

  override def lastUpdateNanos: Long = _lastUpdateNanos

  override def slotState: TeslaScatterSlotState = _slotState

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def open(request: TeslaScatterRequest[_]): TeslaScatterSlot = {
    _ruid = request.ruid
    _destinationHostName = request.destinationHostName
    _request = request
    _request.slot = this
    update(TeslaScatterSlotIdle)
    this
  }

  override
  def close: TeslaScatterSlot = {
    _ruid = null
    _destinationHostName = "NO HOST"
    _request.close()
    _request = null
    _slotState = TeslaScatterSlotIdle
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // updates
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private def update(state: TeslaScatterSlotState = _slotState): Unit = {
    _lastUpdateNanos = System.nanoTime
    _slotState = state
  }

  override
  def slotBegin(): Unit = {
    lockSlot("begin")
    try {
      _beginNanos = System.nanoTime
      update(TeslaScatterSlotActive)
    } finally unlockSlot("begin")
    TeslaScatterReporter.scatterSlotOpen()
    scatter.slotBegin(slot = this, update = _begin)
  }

  override
  def slotProgress(message: String): Unit = {
    lockSlot("progress")
    try {
      update()
      _progress.message = message
    } finally unlockSlot("progress")
    scatter.slotProgress(slot = this, update = _progress)
  }

  override
  def slotFailed(t: Throwable): Unit = {
    lockSlot("failed")
    try {
      _endNanos = System.nanoTime
      update(TeslaScatterSlotFinished)
      _fail.throwable = t
    } finally unlockSlot("failed")
    TeslaScatterReporter.scatterSlotFail()
    scatter.slotFail(slot = this, update = _fail)
  }

  override
  def slotSuccess(): Unit = {
    lockSlot("success")
    try {
      _endNanos = System.nanoTime
      update(TeslaScatterSlotFinished)
    } finally unlockSlot("success")
    TeslaScatterReporter.scatterSlotSuccess()
    scatter.slotSuccess(slot = this, update = _succeed)
  }

  override
  def slotTardy(): Unit = {
    lockSlot("tardy")
    try {
      update()
    } finally unlockSlot("tardy")
    TeslaScatterReporter.scatterSlotTardy()
    scatter.slotTardy(slot = this, update = _tardy)
  }

  override
  def slotRetry(): Unit = {
    lockSlot("retry")
    try {
      update(TeslaScatterSlotActive)
    } finally unlockSlot("retry")

    scatter.slotRetry(slot = this, update = _retry)
  }

  override
  def slotCancelled(): Unit = {
    lockSlot("cancelled")
    try {
      update(TeslaScatterSlotZombie)
    } finally unlockSlot("cancelled")

    scatter.slotCancelled(slot = this, update = _cancelled)
  }

  override def span: Span = {
    _span
  }

  override def setSpan(span: Span): Unit = {
    _span = span
  }
}
