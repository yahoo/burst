/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client.connection

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.fabric.container.FabricWorkerService
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.execution.model.pipeline.addPipelineSubscriber
import org.burstsys.fabric.net.client.{FabricNetClient, FabricNetClientListener}
import org.burstsys.fabric.net.message.assess.FabricNetAssessReqMsg
import org.burstsys.fabric.net.message.cache._
import org.burstsys.fabric.net.message.wave.FabricNetParticleReqMsg
import org.burstsys.fabric.net.receiver.FabricNetReceiver
import org.burstsys.fabric.net.transmitter.FabricNetTransmitter
import org.burstsys.fabric.net.{FabricNetConnection, FabricNetLink}
import org.burstsys.fabric.topology.model.node
import org.burstsys.fabric.topology.model.node.{FabricNode, UnknownFabricNodeId, UnknownFabricNodePort}
import org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisorNode
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import io.netty.channel.Channel

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
 * This is the client side representative of a [[FabricNetConnection]].
 */
trait FabricNetClientConnection extends FabricWorkerService with FabricNetConnection with FabricNetClientListener {

  /**
   * optional listener for the protocol
   */
  def talksTo(listeners: FabricNetClientListener*): this.type

  /**
   * the associated client
   */
  def client: FabricNetClient

}

object FabricNetClientConnection {

  def apply(
             container: FabricWorkerContainer,
             channel: Channel,
             transmitter: FabricNetTransmitter,
             receiver: FabricNetReceiver,
             client: FabricNetClient
           ): FabricNetClientConnection =
    FabricNetClientConnectionContext(
      container: FabricWorkerContainer, channel: Channel, transmitter: FabricNetTransmitter,
      receiver: FabricNetReceiver, client: FabricNetClient
    )

}

protected final case
class FabricNetClientConnectionContext(
                                        container: FabricWorkerContainer,
                                        channel: Channel,
                                        transmitter: FabricNetTransmitter,
                                        receiver: FabricNetReceiver,
                                        client: FabricNetClient
                                      ) extends AnyRef
  with FabricNetClientConnection with FabricNetClientParticleHandler with FabricNetLink
  with FabricNetClientTetherHandler with FabricNetClientAssessHandler with FabricNetClientCacheHandler {

  override val modality: VitalsServiceModality = VitalsPojo

  override def serviceName: String = s"fabric-net-client-connection(containerId=${container.containerIdGetOrThrow}, $link)"

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _listenerSet = ConcurrentHashMap.newKeySet[FabricNetClientListener]().asScala

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def talksTo(listeners: FabricNetClientListener*): this.type = {
    _listenerSet ++= listeners
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    receiver connectedTo this
    tetherLineFunction.start
    assessorBackgroundFunction.start
    addPipelineSubscriber(this)
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info startingMessage
    tetherLineFunction.stop
    assessorBackgroundFunction.stop
    markNotRunning
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // EVENTS FROM RECEIVER
  ////////////////////////////////////////////////////////////////////////////////////

  override def onNetClientServerDisconnect(connection: FabricNetClientConnection): Unit = {
    _listenerSet.foreach(_.onNetClientServerDisconnect(connection))
  }

  override
  def onNetClientAssessReqMsg(connection: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = {
    // (this is on a fabric protocol [[TeslaRequestThreadPool]])
    assert(connection == this)
    log debug s"FabricNetClientConnection.onNetClientAssessReqMsg $msg"
    serverKey.nodeId = msg.senderKey.nodeId
    _listenerSet foreach (_.onNetClientAssessReqMsg(connection, msg))
    assessRequest(msg)
  }

  override
  def onNetClientParticleReqMsg(connection: FabricNetClientConnection, msg: FabricNetParticleReqMsg): Unit = {
    // (this is on a fabric protocol [[TeslaRequestThreadPool]])
    assert(connection == this)
    log debug s"FabricNetClientConnection.onNetClientParticleReqMsg $msg"
    _listenerSet foreach (_.onNetClientParticleReqMsg(connection, msg))
    executeParticle(msg)
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // CACHE REQUESTS
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def onNetClientCacheOperationReqMsg(connection: FabricNetClientConnection, msg: FabricNetCacheOperationReqMsg): Unit = {
    // (this is on a fabric protocol [[TeslaRequestThreadPool]])
    _listenerSet.foreach(_.onNetClientCacheOperationReqMsg(connection, msg))
    cacheManageOperation(msg)
  }

  override
  def onNetClientSliceFetchReqMsg(connection: FabricNetClientConnection, msg: FabricNetSliceFetchReqMsg): Unit = {
    // (this is on a fabric protocol [[TeslaRequestThreadPool]])
    _listenerSet.foreach(_.onNetClientSliceFetchReqMsg(connection, msg))
    cacheSliceFetch(msg)
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Keys
  ////////////////////////////////////////////////////////////////////////////////////

  override
  lazy val clientKey: FabricNode = FabricWorkerNode(
    workerId = container.containerIdGetOrThrow, workerNodeAddress = localAddress
  )

  override
  lazy val serverKey: FabricNode = FabricSupervisorNode(supervisorId = UnknownFabricNodeId, supervisorNodeAddress = remoteAddress, supervisorPort = UnknownFabricNodePort) // unknown at first
}
