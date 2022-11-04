/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.worker

import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.message.AccessParameters
import org.burstsys.fabric.net.message.assess.FabricNetAssessReqMsg

/**
  * event handler for worker container events
  */
trait FabricWorkerListener extends AnyRef {

  /**
   * tell client that server has disconnected
   * (this is on a fabric protocol [[TeslaRequestThreadPool]])
   *
   * @param connection
   */
  def onDisconnect(connection: FabricNetClientConnection): Unit = {}

  /**
   * tell client that there is an incoming assessment request from server
   * (this is on a fabric protocol [[TeslaRequestThreadPool]])
   *
   * @param connection
   * @param msg
   */
  def onNetClientAssessReqMsg(connection: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = {}

  /**
   * Clients Listeners can add info to the access response
   */
  def prepareAccessRespParameters(parameters:  AccessParameters): AccessParameters = {
    parameters
  }
}
