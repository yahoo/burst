/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server.connection

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.fabric.container.FabricMasterService
import org.burstsys.fabric.container.master.FabricMasterContainer
import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.ops.FabricCacheManageOp
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.execution.model.gather.FabricGather
import org.burstsys.fabric.execution.model.wave.FabricParticle
import org.burstsys.fabric.net.message.assess.{FabricNetAssessRespMsg, FabricNetTetherMsg}
import org.burstsys.fabric.net.message.cache.{FabricNetCacheOperationRespMsg, FabricNetSliceFetchRespMsg}
import org.burstsys.fabric.net.message.scatter.FabricNetProgressMsg
import org.burstsys.fabric.net.message.wave.FabricNetParticleRespMsg
import org.burstsys.fabric.net.receiver.FabricNetReceiver
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.net.transmitter.FabricNetTransmitter
import org.burstsys.fabric.net.{FabricNetConnection, FabricNetLink, FabricNetReporter}
import org.burstsys.fabric.topology.model.node.master.FabricMasterNode
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.fabric.topology.model.node.{FabricNode, UnknownFabricNodeId, UnknownFabricNodePort}
import org.burstsys.tesla.scatter.slot.TeslaScatterSlot
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.uid._
import io.netty.channel.Channel

import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.concurrent.Future

/**
 * This is the server side representative of a connection to a single client [[FabricNetConnection]].
 */
trait FabricNetServerConnection extends FabricMasterService with FabricNetConnection with FabricNetServerListener {

  /**
   * optional listener for the protocol
   */
  def talksTo(listener: FabricNetServerListener*): this.type

  /**
   * execute one particle of a wave. Make sure this call is async i.e. do not take a long time
   * or wait around for IO operations to succeed. The Future idiom should take care of this
   */
  def executeParticle(slot: TeslaScatterSlot, particle: FabricParticle): Future[FabricGather]

  /**
   * manage caches on the workers
   */
  def cacheManageOperation(guid: VitalsUid, ruid: VitalsUid, operation: FabricCacheManageOp, generationKey: FabricGenerationKey): Future[Array[FabricGeneration]]

  /**
   * fetch slice metadata from the caches of the workers
   */
  def cacheSliceFetchOperation(guid: VitalsUid, ruid: VitalsUid, generationKey: FabricGenerationKey): Future[Array[FabricSliceMetadata]]
}

object FabricNetServerConnection {
  def apply(container: FabricMasterContainer, channel: Channel, transmitter: FabricNetTransmitter, receiver: FabricNetReceiver): FabricNetServerConnection =
    FabricNetServerConnectionContext(container: FabricMasterContainer, channel: Channel, transmitter: FabricNetTransmitter, receiver: FabricNetReceiver)
}

protected final case
class FabricNetServerConnectionContext(
                                        container: FabricMasterContainer,
                                        channel: Channel,
                                        transmitter: FabricNetTransmitter,
                                        receiver: FabricNetReceiver
                                      ) extends AnyRef
  with FabricNetServerConnection with FabricNetServerParticleHandler with FabricNetLink
  with FabricNetServerAssessHandler with FabricNetServerCacheHandler {

  override def serviceName: String = s"fabric-net-server-connection(containerId=${container.containerIdGetOrThrow}, $link)"

  override val modality: VitalsServiceModality = VitalsPojo

  override def toString: String = serviceName

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _listenerSet: mutable.Set[FabricNetServerListener] = ConcurrentHashMap.newKeySet[FabricNetServerListener].asScala

  ////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    receiver connectedTo this
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    markNotRunning
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def talksTo(listeners: FabricNetServerListener*): this.type = {
    _listenerSet ++= listeners
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // EVENTS FROM RECEIVER
  ////////////////////////////////////////////////////////////////////////////////////

  /**
   * The callback for handling channel disconnect messages
   */
  override def onNetServerDisconnect(connection: FabricNetServerConnection): Unit = {
    _listenerSet.foreach(_.onNetServerDisconnect(connection))
    stopIfNotAlreadyStopped
    FabricNetReporter.recordConnectClose()
  }

  override
  def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetTetherMsg): Unit = {
    log debug s"FabricNetServerConnection.onNetServerTetherMsg $connection $msg"
    clientKey.nodeId = msg.senderKey.nodeId
    _listenerSet foreach (_.onNetServerTetherMsg(connection, msg))
    assessorTether(msg)
  }

  override
  def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {
    log debug s"FabricNetServerConnection.onNetServerAssessRespMsg $connection $msg"
    _listenerSet foreach (_.onNetServerAssessRespMsg(connection, msg))
    assessResponse(msg)
  }

  override
  def onNetServerParticleProgressMsg(connection: FabricNetServerConnection, msg: FabricNetProgressMsg): Unit = {
    _listenerSet foreach (_.onNetServerParticleProgressMsg(connection, msg))
    particleExecutionProgress(msg)
  }

  override
  def onNetServerParticleRespMsg(connection: FabricNetServerConnection, msg: FabricNetParticleRespMsg): Unit = {
    log debug s"FabricNetServerConnection.onNetServerParticleRespMsg $connection $msg"
    _listenerSet foreach (_.onNetServerParticleRespMsg(connection, msg))
    particleExecutionResp(msg)
  }

  override
  def onNetServerCacheOperationRespMsg(connection: FabricNetServerConnection, msg: FabricNetCacheOperationRespMsg): Unit = {
    _listenerSet.foreach(_.onNetServerCacheOperationRespMsg(this, msg))
    cacheManageOperationResp(msg)
  }

  override
  def onNetServerSliceFetchRespMsg(connection: FabricNetServerConnection, msg: FabricNetSliceFetchRespMsg): Unit = {
    _listenerSet.foreach(_.onNetServerSliceFetchRespMsg(this, msg))
    cacheSliceFetchOperationResp(msg)
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Keys
  ////////////////////////////////////////////////////////////////////////////////////

  override
  lazy val clientKey: FabricNode = FabricWorkerNode(
    workerId = UnknownFabricNodeId, workerNodeAddress = remoteAddress
  )

  override
  lazy val serverKey: FabricNode = FabricMasterNode(
    masterId = container.containerIdGetOrThrow, masterNodeAddress = localAddress, masterPort = UnknownFabricNodePort
  )

}
