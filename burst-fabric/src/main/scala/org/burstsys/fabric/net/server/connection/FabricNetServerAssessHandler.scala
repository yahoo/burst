/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server.connection

import java.util.concurrent.atomic.AtomicBoolean

import org.burstsys.fabric.net.message.assess.{FabricNetAssessReqMsg, FabricNetAssessRespMsg, FabricNetTetherMsg}
import org.burstsys.fabric.net.{FabricNetReporter, newRequestId}
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.background.VitalsBackgroundFunctions.BackgroundFunction

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * A background assessment handler
 */
trait FabricNetServerAssessHandler extends AnyRef {

  self: FabricNetServerConnectionContext =>

  ////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////

  /**
   * don't do assessments until ''after'' the client has '''tethered'''
   */
  private[this]
  val _clientTethered = new AtomicBoolean

  ////////////////////////////////////////////////////////////////////////////////////
  // background processing
  ////////////////////////////////////////////////////////////////////////////////////

  private
  val assessorFunction: BackgroundFunction = () => {
    if (_clientTethered.get) {
      val message = FabricNetAssessReqMsg(newRequestId, serverKey, clientKey)
      transmitter transmitControlMessage message
    }
  }

  // add our assessor thread to the list
  backgroundAssessor += assessorFunction

  ////////////////////////////////////////////////////////////////////////////////////
  // events
  ////////////////////////////////////////////////////////////////////////////////////

  final
  def assessorTether(msg: FabricNetTetherMsg): Unit = {
    _clientTethered.set(true)
  }

  final
  def assessResponse(msg: FabricNetAssessRespMsg): Unit = {
    lazy val hdr = s"FabricNetServerAssessHandler.assessResponse"
    FabricNetReporter.recordPing(msg.elapsedNanos)
    log debug s"$hdr $msg"
  }

}
