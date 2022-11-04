/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client.connection

import io.netty.channel.Channel
import org.burstsys.fabric.container.FabricWorkerService
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.net.client.{FabricNetClient, FabricNetClientListener}
import org.burstsys.fabric.net.message.assess.FabricNetAssessReqMsg
import org.burstsys.fabric.net.message.{AccessParameters, FabricNetAssessReqMsgType, FabricNetMsg}
import org.burstsys.fabric.net.receiver.FabricNetReceiver
import org.burstsys.fabric.net.transmitter.FabricNetTransmitter
import org.burstsys.fabric.net.{FabricNetConnection, FabricNetLink, message}
import org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisorNode
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.fabric.topology.model.node.{FabricNode, UnknownFabricNodeId, UnknownFabricNodePort}
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
 * This is the client side representative of a [[FabricNetConnection]].
 */
trait FabricNetClientConnection extends FabricWorkerService with FabricNetConnection {

  def talksTo(listeners: FabricNetClientListener*): this.type

  def transmitControlMessage(msg: FabricNetMsg): Future[Unit]

  def transmitDataMessage(msg: FabricNetMsg): Future[Unit]

}

object FabricNetClientConnection {

  def apply(
             container: FabricWorkerContainer[_],
             channel: Channel,
             transmitter: FabricNetTransmitter,
             receiver: FabricNetReceiver,
             client: FabricNetClient
           ): FabricNetClientConnection =
    FabricNetClientConnectionContext(container, channel, transmitter, receiver, client )

}

protected final case
class FabricNetClientConnectionContext(
                                        container: FabricWorkerContainer[_],
                                        channel: Channel,
                                        transmitter: FabricNetTransmitter,
                                        receiver: FabricNetReceiver,
                                        client: FabricNetClient
                                      ) extends AnyRef
  with FabricNetClientConnection with FabricNetLink
  with FabricNetClientTetherHandler with FabricNetClientAssessHandler {

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

  override def onMessage(messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {
    messageId match {
      /////////////////// ASSESSMENT /////////////////
      case FabricNetAssessReqMsgType =>
        val msg = FabricNetAssessReqMsg(buffer)
        log debug s"FabricNetClientConnection.onNetClientAssessReqMsg $msg"
        serverKey.nodeId = msg.senderKey.nodeId
        _listenerSet foreach (_.onNetClientAssessReqMsg(this, msg))
        var parameters: AccessParameters = Map.empty
        _listenerSet foreach{l =>
          parameters = l.prepareAccessRespParameters(parameters)
        }
        assessRequest(msg, parameters)

      case _ =>
        _listenerSet foreach (_.onNetMessage(this, messageId, buffer))
    }
  }

  override def onDisconnect(): Unit = {
    _listenerSet.foreach(_.onDisconnect(this))
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

  override def transmitControlMessage(msg: FabricNetMsg): Future[Unit] = {
    transmitter.transmitControlMessage(msg)
  }

  override def transmitDataMessage(msg: FabricNetMsg): Future[Unit] = {
    transmitter.transmitDataMessage(msg)
  }
}
