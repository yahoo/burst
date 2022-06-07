/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.language.postfixOps

package object scatter extends VitalsLogger {

  final val debugScatters = false

  type TeslaScatterId = Int

  type TeslaScatterSlotId = Int

  /**
   * base class for all updates about the '''scatter'''
   * <p/>'''NOTE:'''  its important to keep updates related to a '''scatter''' and a '''scatter slot'''
   */
  abstract class TeslaScatterUpdate {

    private[this]
    var _msg: String = _

    def message: String = _msg

    private[scatter]
    def message_=(message: String): Unit = _msg = message

    def reset(): Unit = _msg = null

    final override def toString: String = this.getClass.getSimpleName.stripPrefix("Tesla").stripSuffix("$")

  }

  /**
   * '''scatter''' is started
   */
  final case class TeslaScatterBegin() extends TeslaScatterUpdate

  /**
   * '''scatter''' is completed - no more active slots
   */
  final case class TeslaScatterSucceed() extends TeslaScatterUpdate

  /**
   * '''scatter''' is not completed - but remaining slots were aborted
   */
  final case class TeslaScatterCancel() extends TeslaScatterUpdate

  /**
   * '''scatter''' is not completed - but no updates after timeout period
   */
  final case class TeslaScatterTimeout() extends TeslaScatterUpdate

  /**
   * '''scatter''' has an unrecoverable exception
   */
  final case class TeslaScatterFail() extends TeslaScatterUpdate {

    private[this]
    var _throwable: Throwable = _

    def throwable: Throwable = _throwable

    private[scatter]
    def throwable_=(t: Throwable): Unit = {
      message = printStack(t)
      _throwable = t
    }

    override def reset(): Unit = {
      super.reset()
      _throwable = null
    }
  }


  /**
   * the various known ''states'' of the overall '''scatter'''
   *
   * @param isRunning
   */
  sealed case
  class TeslaScatterState(isRunning: Boolean = false)

  object TeslaScatterIdleState extends TeslaScatterState()

  /**
   * the overall '''scatter''' has stopped. It may have outstanding zombie slots.
   */
  object TeslaScatterStopState extends TeslaScatterState()

  object TeslaScatterRunState extends TeslaScatterState(isRunning = true)

  object TeslaScatterFailState extends TeslaScatterState()

  object TeslaScatterSuccessState extends TeslaScatterState()


}
