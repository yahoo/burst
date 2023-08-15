/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server.connection

import io.netty.channel.Channel
import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.net._
import org.burstsys.fabric.net.message.assess.{FabricNetAssessReqMsg, FabricNetAssessRespMsg, FabricNetHeartbeatMsg, FabricNetShutdownMsg}
import org.burstsys.fabric.net.message.{FabricNetAssessRespMsgType, FabricNetHeartbeatMsgType, FabricNetMsg}
import org.burstsys.fabric.net.receiver.FabricNetReceiver
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.net.transmitter.FabricNetTransmitter
import org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisorNode
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.fabric.topology.model.node.{FabricNode, UnknownFabricNodeId, UnknownFabricNodePort}
import org.burstsys.fabric.trek.{FabricNetAssess, FabricNetHeartbeatRcv}
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.background.VitalsBackgroundFunctions.BackgroundFunction
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.trek.TrekStage

import java.util.concurrent.ConcurrentHashMap
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

  /**
   * Notify the connected client that we are going away
   */
  def shutdown(): Unit

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
  with FabricNetServerConnection with FabricNetLink {

  override def serviceName: String = s"fabric-net-server-connection(containerId=${container.containerIdGetOrThrow}, $link)"

  override val modality: VitalsServiceModality = VitalsPojo

  override def toString: String = serviceName

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _listenerSet = ConcurrentHashMap.newKeySet[FabricNetServerListener]()

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
    backgroundAssessor -= assessorFunction
    markNotRunning
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override def talksTo(listeners: FabricNetServerListener*): this.type = {
    _listenerSet.addAll(listeners.asJava)
    this
  }

  override def shutdown(): Unit = {
    transmitControlMessage(FabricNetShutdownMsg())
  }

  override def onMessage(messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {
    messageId match {
      /////////////////// TETHERING /////////////////
      case FabricNetHeartbeatMsgType =>
        val stage = FabricNetHeartbeatRcv.beginSync()
        val msg = FabricNetHeartbeatMsg(buffer)
        log trace burstStdMsg(s"FabricNetServerConnection.onNetServerTetherMsg $this $msg")
        clientKey.nodeId = msg.senderKey.nodeId

        _listenerSet.stream.forEach (_.onNetServerTetherMsg(this, msg))
        backgroundAssessor += assessorFunction
        FabricNetHeartbeatRcv.end(stage)

      /////////////////// ASSESSMENT /////////////////
      case FabricNetAssessRespMsgType =>
        val msg = FabricNetAssessRespMsg(buffer)
        log trace burstStdMsg(s"$this $msg")
        _listenerSet.stream.forEach(_.onNetServerAssessRespMsg(this, msg))
        assessResponse(msg)

      case _ =>
        _listenerSet.stream.forEach(_.onNetMessage(this, messageId, buffer))
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Assessment
  ////////////////////////////////////////////////////////////////////////////////////
  var stage: Option[TrekStage] = None

  private val assessorFunction: BackgroundFunction = () => {
    stage = Option(FabricNetAssess.begin() { stage => stage })
    transmitter transmitControlMessage FabricNetAssessReqMsg(newRequestId, serverKey, clientKey)
  }

  private def assessResponse(msg: FabricNetAssessRespMsg): Unit = {
    FabricNetReporter.recordPing(msg.elapsedNanos)
    log trace burstStdMsg(s"$this $msg")
    stage.foreach(FabricNetAssess.end)
    stage = None
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // events
  ////////////////////////////////////////////////////////////////////////////////////

  /**
   * The callback for handling channel disconnect messages
   */
  override def onDisconnect(): Unit = {
    log debug burstStdMsg(s"connection disconnect $this")
    _listenerSet.stream.forEach(_.onDisconnect(this))
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
