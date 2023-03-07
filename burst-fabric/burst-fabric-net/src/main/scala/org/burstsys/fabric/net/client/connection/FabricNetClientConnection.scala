/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client.connection

import io.netty.channel.Channel
import org.burstsys.fabric.container.FabricWorkerService
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.net.client.{FabricNetClient, FabricNetClientListener}
import org.burstsys.fabric.net.message.assess.{FabricNetAssessReqMsg, FabricNetTetherMsg}
import org.burstsys.fabric.net.message.{AccessParameters, FabricNetAssessReqMsgType, FabricNetMsg}
import org.burstsys.fabric.net.receiver.FabricNetReceiver
import org.burstsys.fabric.net.transmitter.FabricNetTransmitter
import org.burstsys.fabric.net.{FabricNetConnection, FabricNetLink, message, newRequestId}
import org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisorNode
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.fabric.topology.model.node.{FabricNode, UnknownFabricNodeId, UnknownFabricNodePort}
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.git
import org.burstsys.vitals.healthcheck.{VitalsComponentHealth, VitalsHealthMarginal, VitalsHealthMonitoredComponent}
import org.burstsys.vitals.logging.burstStdMsg

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
 * This is the client side representative of a [[FabricNetConnection]].
 */
trait FabricNetClientConnection extends FabricWorkerService with FabricNetConnection with VitalsHealthMonitoredComponent {

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
  with FabricNetClientConnection with FabricNetLink with FabricNetClientAssessHandler {

  override val modality: VitalsServiceModality = VitalsPojo

  override def serviceName: String = s"fabric-net-client-connection(containerId=${container.containerIdGetOrThrow}, $link)"

  override def componentHealth: VitalsComponentHealth = {
    if (isConnected)
      VitalsComponentHealth(message = s"Connected to $serverKey")
    else
      VitalsComponentHealth(VitalsHealthMarginal, "Not connected")
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _listenerSet = ConcurrentHashMap.newKeySet[FabricNetClientListener]()

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override def talksTo(listeners: FabricNetClientListener*): this.type = {
    _listenerSet.addAll(listeners.asJava)
    this
  }

  override def transmitControlMessage(msg: FabricNetMsg): Future[Unit] = {
    transmitter.transmitControlMessage(msg)
  }

  override def transmitDataMessage(msg: FabricNetMsg): Future[Unit] = {
    transmitter.transmitDataMessage(msg)
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
    log info stoppingMessage
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
        log debug burstStdMsg(s"FabricNetClientConnection.onNetClientAssessReqMsg $this $msg")
        serverKey.nodeId = msg.senderKey.nodeId
        _listenerSet.stream.forEach(_.onNetClientAssessReqMsg(this, msg))

        var parameters: AccessParameters = Map.empty
        _listenerSet.stream.forEach{l =>
          parameters = l.prepareAccessRespParameters(parameters)
        }
        assessRequest(msg, parameters)

      case _ =>
        _listenerSet.stream.forEach(_.onNetMessage(this, messageId, buffer))
    }
  }

  override def onDisconnect(): Unit = {
    log trace burstStdMsg(s"disconnect $this")
    _listenerSet.stream.forEach(_.onDisconnect(this))
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Tethering
  ////////////////////////////////////////////////////////////////////////////////////

  lazy val tetherLineFunction = new VitalsBackgroundFunction("fab-client-tether", 100 milliseconds, 10 seconds, {
      val c = channel
      if (c != null && c.isActive) {
        val message = FabricNetTetherMsg(newRequestId, clientKey, serverKey, git.commitId)
        transmitter.transmitControlMessage(message)
      }
    }
  )

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
