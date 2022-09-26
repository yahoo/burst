/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client

import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.message.assess.FabricNetAssessReqMsg
import org.burstsys.fabric.net.message.cache._
import org.burstsys.fabric.net.message.wave.FabricNetParticleReqMsg

/**
 * handle events from a remote fabric protocol server-supervisor to the local fabric protocol client-worker
 * (this is on a fabric protocol [[TeslaRequestThreadPool]])
 */
trait FabricNetClientListener extends Any {

  /**
   * tell client that server has disconnected
   * (this is on a fabric protocol [[TeslaRequestThreadPool]])
   *
   * @param connection
   */
  def onNetClientServerDisconnect(connection: FabricNetClientConnection): Unit = {}

  /**
   * tell client that there is an incoming cache operation request from server
   * (this is on a fabric protocol [[TeslaRequestThreadPool]])
   *
   * @param connection
   * @param msg
   */
  def onNetClientCacheOperationReqMsg(connection: FabricNetClientConnection, msg: FabricNetCacheOperationReqMsg): Unit = {}

  /**
   * tell client that there is an incoming slice information request from server
   * (this is on a fabric protocol [[TeslaRequestThreadPool]])
   *
   * @param connection
   * @param msg
   */
  def onNetClientSliceFetchReqMsg(connection: FabricNetClientConnection, msg: FabricNetSliceFetchReqMsg): Unit = {}

  /**
   * tell client that there is an incoming assessment request from server
   * (this is on a fabric protocol [[TeslaRequestThreadPool]])
   *
   * @param connection
   * @param msg
   */
  def onNetClientAssessReqMsg(connection: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = {}

  /**
   * tell client that there is an incoming particle request from server
   * (this is on a fabric protocol [[TeslaRequestThreadPool]])
   *
   * @param connection
   * @param msg
   */
  def onNetClientParticleReqMsg(connection: FabricNetClientConnection, msg: FabricNetParticleReqMsg): Unit = {}

}
