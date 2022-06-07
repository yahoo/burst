/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter

import org.burstsys.vitals.logging._

package object slot extends VitalsLogger {

  /**
   * the questions that can be asked regarding a '''scatter slot''' state
   */
  trait TeslaScatterSlotStateFlags {

    /**
     * is this slot in a '''zombie''' state i.e. has the scatter stopped without
     * a confirmed return from the request. These are managed so that any cleanup
     * associated with zombies can still happen even after the scatter is no longer
     * active.
     */
    def isZombie: Boolean = false

    /**
     * is this slot in an '''active''' state i.e. the scatter is active and this
     * request has not completed, failed, or timed out.
     */
    def isActive: Boolean = false

    /**
     * is this slot in an '''idle''' state i.e. the scatter is active and this
     * request has completed, failed, or timed out.
     */
    def isIdle: Boolean = false

    /**
     * is this slot in an '''idle''' state i.e. the scatter is not active but its final
     * state has not yet been received / acknowledged
     */
    def isDone: Boolean = false

  }

  /**
   * the various known ''states'' of an individual '''scatter slot'''
   */
  sealed trait TeslaScatterSlotState extends TeslaScatterSlotStateFlags

  /**
   * this slot is currently not in use
   */
  case object TeslaScatterSlotIdle extends TeslaScatterSlotState {
    override def isIdle: Boolean = true
    override def toString: String = "Idle"
  }

  /**
   * this slot is currently in use and active
   */
  case object TeslaScatterSlotActive extends TeslaScatterSlotState {
    override def isActive: Boolean = true
    override def toString: String = "Active"
  }

  /**
   * this slot is not currently active but is not in use. Generally this means that
   * there was a state change that requires interaction
   */
  case object TeslaScatterSlotFinished extends TeslaScatterSlotState {
    override def isDone: Boolean = true
    override def toString: String = "Finished"
  }

  /**
   * this slot was still active when the scatter was stopped
   */
  case object TeslaScatterSlotZombie extends TeslaScatterSlotState {
    override def isActive: Boolean = true
    override def isZombie: Boolean = true
    override def toString: String = "Zombie"
  }

  /**
   * set of updates associated with a specific '''scatter slot'''
   */
  sealed trait TeslaScatterSlotUpdate extends AnyRef {
    def slot: TeslaScatterSlot
  }

  /**
   * '''scatter slot''' begin.
   */
  final case class TeslaScatterSlotBegin(slot: TeslaScatterSlot) extends TeslaScatterUpdate with TeslaScatterSlotUpdate

  /**
   * '''scatter slot''' progress has been achieved. The '''scatter slot''' stays active
   */
  final case class TeslaScatterSlotProgress(slot: TeslaScatterSlot) extends TeslaScatterUpdate with TeslaScatterSlotUpdate

  /**
   * '''scatter slot''' failure. '''scatter slot''' is now idle
   */
  final case class TeslaScatterSlotFail(slot: TeslaScatterSlot) extends TeslaScatterUpdate with TeslaScatterSlotUpdate {

    private[this]
    var _throwable: Throwable = _

    def throwable: Throwable = _throwable

    private[scatter]
    def throwable_=(t: Throwable): Unit = _throwable = t

    override def reset(): Unit = {
      super.reset()
      _throwable = null
    }

  }

  /**
   * '''scatter slot''' success. '''scatter slot''' is now idle
   */
  final case class TeslaScatterSlotSucceed(slot: TeslaScatterSlot) extends TeslaScatterUpdate with TeslaScatterSlotUpdate

  /**
   * '''scatter slot''' timeout. '''scatter slot''' is now idle
   */
  final case class TeslaScatterSlotTardy(slot: TeslaScatterSlot) extends TeslaScatterUpdate with TeslaScatterSlotUpdate

  final case class TeslaScatterSlotRetry(slot: TeslaScatterSlot) extends TeslaScatterUpdate with TeslaScatterSlotUpdate

  final case class TeslaScatterSlotCancel(slot: TeslaScatterSlot) extends TeslaScatterUpdate with TeslaScatterSlotUpdate

}
