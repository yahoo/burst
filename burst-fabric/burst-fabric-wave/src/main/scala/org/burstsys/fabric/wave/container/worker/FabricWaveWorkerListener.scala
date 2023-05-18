/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.container.worker

import org.burstsys.fabric.container.worker.FabricWorkerListener
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.wave.message.cache.{FabricNetCacheOperationReqMsg, FabricNetSliceFetchReqMsg}
import org.burstsys.fabric.wave.message.wave.FabricNetParticleReqMsg

/**
 * event handler for worker container events
 */
trait FabricWaveWorkerListener extends FabricWorkerListener {
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
   * tell client that there is an incoming particle request from server
   * (this is on a fabric protocol [[TeslaRequestThreadPool]])
   *
   * @param connection
   * @param msg
   */
  def onNetClientParticleReqMsg(connection: FabricNetClientConnection, msg: FabricNetParticleReqMsg): Unit = {}

}
