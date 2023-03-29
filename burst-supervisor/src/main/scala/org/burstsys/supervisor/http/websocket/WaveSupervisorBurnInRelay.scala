package org.burstsys.supervisor.http.websocket

import org.burstsys.fabric.container.http.{FabricWebSocket, FabricWebSocketGroup, FabricWebSocketListener, FabricWebSocketService}
import org.burstsys.supervisor.http.endpoints.ClientWebsocketMessage
import org.burstsys.supervisor.http.service.provider.{BurnInConfig, BurnInEvent, BurstWaveBurnInListener, BurstWaveSupervisorBurnInService}
import org.burstsys.supervisor.http.websocket.BurnInWebsocketMessages.{BurnInConfigMsg, BurnInEventsMsg, BurnInStatusMsg}

case class WaveSupervisorBurnInRelay(webSocketService: FabricWebSocketService, burnIn: BurstWaveSupervisorBurnInService)
  extends BurstWaveBurnInListener with FabricWebSocketListener {

  private val _socket = webSocketService.open("/burn-in", this)

  override def burnInStarted(config: BurnInConfig): Unit = {
    _socket.broadcastJson(getRunningMessage)
    _socket.broadcastJson(getConfigMessage)
  }

  override def burnInEvent(event: BurnInEvent): Unit = {
    _socket.broadcastJson(BurnInEventsMsg(Array(event)))
  }

  override def burnInStopped(): Unit = {
    _socket.broadcastJson(getRunningMessage)
  }

  override def onWebSocketOpen(group: FabricWebSocketGroup, socket: FabricWebSocket): Unit = {
    socket.sendJson(getRunningMessage)
    socket.sendJson(getConfigMessage)
    socket.sendJson(getEventsMessage)
  }

  private def getEventsMessage: BurnInEventsMsg = {
    val events = burnIn.getEvents
    BurnInEventsMsg(events)
  }

  private def getRunningMessage: BurnInStatusMsg = {
    val isRunning = burnIn.isRunning
    BurnInStatusMsg(isRunning)
  }

  private def getConfigMessage: BurnInConfigMsg = {
    val config = burnIn.getConfig
    BurnInConfigMsg(config)
  }

}

object BurnInWebsocketMessages {

  case class BurnInStatusMsg(
                              isRunning: Boolean,
                            ) extends ClientWebsocketMessage {
    override val msgType: String = "StatusMsg"
  }

  case class BurnInConfigMsg(
                              config: BurnInConfig,
                            ) extends ClientWebsocketMessage {
    override val msgType: String = "ConfigMsg"
  }

  case class BurnInEventsMsg(
                              events: Array[BurnInEvent],
                            ) extends ClientWebsocketMessage {
    override val msgType: String = "EventsMsg"
  }
}
