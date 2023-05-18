/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.container.supervisor

import org.burstsys.fabric.container.supervisor.FabricSupervisorListener
import org.burstsys.fabric.wave.message.cache.{FabricNetCacheOperationRespMsg, FabricNetSliceFetchRespMsg}
import org.burstsys.fabric.wave.message.scatter.FabricNetProgressMsg
import org.burstsys.fabric.wave.message.wave.FabricNetParticleRespMsg
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection

/**
 * handler for supervisor container events
 */
trait FabricWaveSupervisorListener extends FabricSupervisorListener {
  /**
   * tell a fabric server-supervisor that fabric client-worker cache operation request is delivering a response
   */
  def onNetServerCacheOperationRespMsg(connection: FabricNetServerConnection, msg: FabricNetCacheOperationRespMsg): Unit = {}

  /**
   * tell a fabric server-supervisor that fabric client-worker slice information request is delivering a response
   */
  def onNetServerSliceFetchRespMsg(connection: FabricNetServerConnection, msg: FabricNetSliceFetchRespMsg): Unit = {}

  /**
   * tell a fabric server-supervisor that fabric client-worker cache particle request is delivering a response
   */
  def onNetServerParticleProgressMsg(connection: FabricNetServerConnection, msg: FabricNetProgressMsg): Unit = {}

  /**
   * tell a fabric server-supervisor that fabric client-worker cache particle request is delivering a response
   */
  def onNetServerParticleRespMsg(connection: FabricNetServerConnection, msg: FabricNetParticleRespMsg): Unit = {}
}
