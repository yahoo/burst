/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client

import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.message
import org.burstsys.fabric.net.message.AccessParameters
import org.burstsys.fabric.net.message.assess.FabricNetAssessReqMsg

/**
 * handle events from a remote fabric protocol server-supervisor to the local fabric protocol client-worker
 * (this is on a fabric protocol [[TeslaRequestThreadPool]])
 */
trait FabricNetClientListener extends Any {
  /**
   * messages that are not handled by the server
   */
  def onNetMessage(connection:FabricNetClientConnection, messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {}

  /**
   * connection is disconnecting
   */
  def onDisconnect(connection:FabricNetClientConnection): Unit ={}

  /**
   * tell client that there is an incoming assessment request from server
   * (this is on a fabric protocol [[TeslaRequestThreadPool]])
   */
  def onNetClientAssessReqMsg(connection: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = {}

  /**
   * Clients Listeners can add info to the access response
   */
  def prepareAccessParameters(parameters:  AccessParameters): AccessParameters = {
    parameters
  }
}
