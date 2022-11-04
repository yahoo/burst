/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.supervisor

import org.burstsys.fabric.net.message.assess.{FabricNetAssessRespMsg, FabricNetTetherMsg}
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection

/**
  * handler for supervisor container events
  */
trait FabricSupervisorListener extends AnyRef {
  /**
   * connection is disconnecting
   */
  def onDisconnect(connection:FabricNetServerConnection): Unit ={}

  /**
   * tell a fabric server-supervisor that fabric client-worker has ''tethered'' (heartbeat after connection)
   */
  def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetTetherMsg): Unit = {}

  /**
   * tell a fabric server-supervisor that fabric client-worker cache assessment request is delivering a response
   */
  def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {}
}
