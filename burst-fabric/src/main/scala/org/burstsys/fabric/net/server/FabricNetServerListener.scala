/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server

import org.burstsys.fabric.net.message.assess.{FabricNetAssessRespMsg, FabricNetTetherMsg}
import org.burstsys.fabric.net.message.cache.{FabricNetCacheOperationRespMsg, FabricNetSliceFetchRespMsg}
import org.burstsys.fabric.net.message.scatter.FabricNetProgressMsg
import org.burstsys.fabric.net.message.wave.FabricNetParticleRespMsg
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection

/**
  * handle events from a remote fabric protocol client-worker to the local fabric protocol server-supervisor
  * (this is on a fabric protocol [[org.burstsys.tesla.thread.request.TeslaRequestThreadPool]])
  */
trait FabricNetServerListener extends Any {

  /**
    * tell a fabric server-supervisor that fabric client-worker has ''tethered'' (heartbeat after connection)
    * (this is on a fabric protocol [[TeslaRequestThreadPool]])
    */
  def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetTetherMsg): Unit = {}

  /**
    * tell a fabric server-supervisor that fabric client-worker has ''disconnected'' (presumably died or partitioned)
    * (this is on a fabric protocol [[TeslaRequestThreadPool]])
    */
  def onNetServerDisconnect(connection: FabricNetServerConnection): Unit = {}

  /**
    * tell a fabric server-supervisor that fabric client-worker cache operation request is delivering a response
    * (this is on a fabric protocol [[TeslaRequestThreadPool]])
    */
  def onNetServerCacheOperationRespMsg(connection: FabricNetServerConnection, msg: FabricNetCacheOperationRespMsg): Unit = {}

  /**
    * tell a fabric server-supervisor that fabric client-worker slice information request is delivering a response
    * (this is on a fabric protocol [[TeslaRequestThreadPool]])
    */
  def onNetServerSliceFetchRespMsg(connection: FabricNetServerConnection, msg: FabricNetSliceFetchRespMsg): Unit = {}

  /**
    * tell a fabric server-supervisor that fabric client-worker cache assessment request is delivering a response
    * (this is on a fabric protocol [[TeslaRequestThreadPool]])
    */
  def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {}

  /**
   * tell a fabric server-supervisor that fabric client-worker cache particle request is delivering a response
   * (this is on a fabric protocol [[TeslaRequestThreadPool]])
   */
  def onNetServerParticleProgressMsg(connection: FabricNetServerConnection, msg: FabricNetProgressMsg): Unit = {}

  /**
    * tell a fabric server-supervisor that fabric client-worker cache particle request is delivering a response
    * (this is on a fabric protocol [[TeslaRequestThreadPool]])
    */
  def onNetServerParticleRespMsg(connection: FabricNetServerConnection, msg: FabricNetParticleRespMsg): Unit = {}

}
