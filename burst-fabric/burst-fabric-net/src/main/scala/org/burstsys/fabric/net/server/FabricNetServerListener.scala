/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server

import org.burstsys.fabric.net.message
import org.burstsys.fabric.net.message.assess.{FabricNetAssessRespMsg, FabricNetHeartbeatMsg}
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection

/**
  * handle events from a remote fabric protocol client-worker to the local fabric protocol server-supervisor
  * (this is on a fabric protocol [[org.burstsys.tesla.thread.request.TeslaRequestThreadPool]])
  */
trait FabricNetServerListener extends Any {
  /**
   * messages that are not handled by the server
   */
  def onNetMessage(connection:FabricNetServerConnection, messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {}

  /**
   * connection is disconnecting
   */
  def onDisconnect(connection:FabricNetServerConnection): Unit ={}

  /**
    * tell a fabric server-supervisor that fabric client-worker has ''tethered'' (heartbeat after connection)
    */
  def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetHeartbeatMsg): Unit = {}

  /**
   * tell a fabric server-supervisor that fabric client-worker cache assessment request is delivering a response
   */
  def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {}
}
