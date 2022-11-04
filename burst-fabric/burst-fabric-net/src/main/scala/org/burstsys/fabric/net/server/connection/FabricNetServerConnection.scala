/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server.connection

import io.netty.channel.Channel
import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.net.message.assess.{FabricNetAssessRespMsg, FabricNetTetherMsg}
import org.burstsys.fabric.net.message.{FabricNetAssessRespMsgType, FabricNetMsg, FabricNetTetherMsgType}
import org.burstsys.fabric.net.receiver.FabricNetReceiver
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.net.transmitter.FabricNetTransmitter
import org.burstsys.fabric.net.{FabricNetConnection, FabricNetLink, FabricNetReporter, message}
import org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisorNode
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.fabric.topology.model.node.{FabricNode, UnknownFabricNodeId, UnknownFabricNodePort}
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

/**
 * This is the server side representative of a connection to a single client [[FabricNetConnection]].
 */
trait FabricNetServerConnection extends FabricSupervisorService with FabricNetConnection {

  /**
   * optional listener for the protocol
   */
  def talksTo(listener: FabricNetServerListener*): this.type

  def transmitControlMessage(msg: FabricNetMsg): Future[Unit]

  def transmitDataMessage(msg: FabricNetMsg): Future[Unit]

}

object FabricNetServerConnection {
  def apply(container: FabricSupervisorContainer[_], channel: Channel, transmitter: FabricNetTransmitter, receiver: FabricNetReceiver): FabricNetServerConnection =
    FabricNetServerConnectionContext(container, channel, transmitter, receiver)
}

protected final case
class FabricNetServerConnectionContext(
                                        container: FabricSupervisorContainer[_],
                                        channel: Channel,
                                        transmitter: FabricNetTransmitter,
                                        receiver: FabricNetReceiver
                                      ) extends AnyRef
  with FabricNetServerConnection with FabricNetLink with FabricNetServerAssessHandler
  // with FabricNetServerCacheHandler with FabricNetServerParticleHandler
  {

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

  override def onMessage(messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {
    messageId match {
      /////////////////// TETHERING /////////////////
      case FabricNetTetherMsgType =>
        val msg = FabricNetTetherMsg(buffer)
        log debug s"FabricNetServerConnection.onNetServerTetherMsg $this $msg"
        clientKey.nodeId = msg.senderKey.nodeId
        _listenerSet foreach (_.onNetServerTetherMsg(this, msg))
        assessorTether(msg)

      /////////////////// ASSESSMENT /////////////////
      case FabricNetAssessRespMsgType =>
        val msg = FabricNetAssessRespMsg(buffer)
        log debug s"FabricNetServerConnection.onNetServerAssessRespMsg $this $msg"
        _listenerSet foreach (_.onNetServerAssessRespMsg(this, msg))
        assessResponse(msg)

      case _ =>
        _listenerSet foreach (_.onNetMessage(this, messageId, buffer))
    }
  }

  /**
   * The callback for handling channel disconnect messages
   */
  override def onDisconnect(): Unit = {
    _listenerSet.foreach(_.onDisconnect(this))
    stopIfNotAlreadyStopped
    FabricNetReporter.recordConnectClose()
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Keys
  ////////////////////////////////////////////////////////////////////////////////////

  override
  lazy val clientKey: FabricNode = FabricWorkerNode(
    workerId = UnknownFabricNodeId, workerNodeAddress = remoteAddress
  )

  override
  lazy val serverKey: FabricNode = FabricSupervisorNode(supervisorId = container.containerIdGetOrThrow, supervisorNodeAddress = localAddress, supervisorPort = UnknownFabricNodePort)

  override def transmitControlMessage(msg: FabricNetMsg): Future[Unit] = {
    transmitter.transmitControlMessage(msg)
  }

  override def transmitDataMessage(msg: FabricNetMsg): Future[Unit] = {
    transmitter.transmitDataMessage(msg)
  }
}
