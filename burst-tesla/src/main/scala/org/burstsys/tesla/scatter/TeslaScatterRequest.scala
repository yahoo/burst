/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter

import org.burstsys.tesla.scatter.slot.TeslaScatterSlot
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.uid.VitalsUid

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
 * provided by scatter framework user to represent the work being performed for each slot of the scatter
 */
trait TeslaScatterRequest[R] extends Any {

  /**
   * unique identifier for the individual request within the scatter
   *
   * @return
   */
  def ruid: VitalsUid

  /**
   * host name of where this request is headed...
   *
   * @return
   */
  def destinationHostName: VitalsHostName

  /**
   * if no update is received after this time, the slot is considered tardy
   */
  def tardyAfter: Duration = Duration.Inf

  /**
   * the slot this request is being executed by
   */
  def slot: TeslaScatterSlot

  def slot_=(slot: TeslaScatterSlot): Unit

  /**
   * execute the body of this closure. Right now the returned futures are ignored
   * but we prolly want to change that.
   */
  def execute: Future[_]

  /**
   * cancel this request. This method must either call `super` or call `slot.slotCancelled`.
   * This method should also avoid excessive blocking to ensure the scatter event loop is processed
   * efficiently.
   */
  def cancel(): Unit = slot.slotCancelled()

  /**
   * close the request - all resources are released/finalized
   */
  def close(): Unit = slot = null

  /**
   * the '''scatter''' result
   *
   * @return
   */
  def result: R

}

abstract class TeslaScatterRequestContext[R] extends AnyRef with TeslaScatterRequest[R] {

  private final var _slot: TeslaScatterSlot = _

  final var result: R = _

  override final def slot: TeslaScatterSlot = _slot

  override final def slot_=(slot: TeslaScatterSlot): Unit = _slot = slot

}
