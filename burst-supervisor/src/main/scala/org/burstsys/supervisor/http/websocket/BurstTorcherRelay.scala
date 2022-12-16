/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.websocket

import org.burstsys.fabric.container.http.FabricWebSocket
import org.burstsys.fabric.container.http.FabricWebSocketGroup
import org.burstsys.fabric.container.http.FabricWebSocketListener
import org.burstsys.fabric.container.http.FabricWebSocketService
import org.burstsys.supervisor.http.endpoints.ClientJsonObject
import org.burstsys.supervisor.http.service.provider.BurstWaveSupervisorTorcherService
import org.burstsys.supervisor.http.service.provider.TorcherEventListener
import org.burstsys.supervisor.http.service.provider.TorcherLogMessage
import org.burstsys.supervisor.http.service.provider.TorcherStatus
import org.burstsys.supervisor.http.websocket.TorcherMessages.TorcherMessage
import org.burstsys.vitals.background.VitalsBackgroundFunction

import scala.concurrent.duration._
import scala.language.postfixOps

final case class BurstTorcherRelay(torcher: BurstWaveSupervisorTorcherService, websocketService: FabricWebSocketService)
  extends FabricWebSocketListener with TorcherEventListener {

  private val socketGroup: FabricWebSocketGroup = websocketService.open("/torcher", this)

  private val backgroundUpdater = new VitalsBackgroundFunction("torcher-sleeper", 1 second, 1 second, {
    socketGroup.broadcastJson(TorcherMessage(torcher.status))
  })

  override def onWebSocketOpen(group: FabricWebSocketGroup, socket: FabricWebSocket): Unit = {
    socket.sendJson(TorcherMessage(torcher.config.getOrElse("")))
    socket.sendJson(TorcherMessage(torcher.status))
    torcher.messages.foreach { msg => socket.sendJson(TorcherMessage(msg)) }
  }

  override def torcherStarted(source: String): Unit = {
    backgroundUpdater.startIfNotAlreadyStarted
    socketGroup.broadcastJson(TorcherMessage(torcher.config.getOrElse("")))
    socketGroup.broadcastJson(TorcherMessage(torcher.status))
  }

  override def torcherMessage(message: TorcherLogMessage): Unit = {
    socketGroup.broadcastJson(TorcherMessage(message))
  }

  override def torcherStopped(): Unit = {
    socketGroup.broadcastJson(TorcherMessage(torcher.status))
    backgroundUpdater.stopIfNotAlreadyStopped
  }

}

object TorcherMessages {
  object TorcherMessage {
    def apply(source: String): ClientJsonObject = TorcherConfigWsMessage(source)

    def apply(status: TorcherStatus): ClientJsonObject = TorcherStatusWsMessage(status)

    def apply(message: TorcherLogMessage): ClientJsonObject = TorcherLogWsMessage(message.level.toString, message.message)
  }

  private abstract class TorcherWsMessage(val op: String) extends ClientJsonObject

  private case class TorcherLogWsMessage(level: String, data: String)
    extends TorcherWsMessage("TORCHER_MESSAGE")

  private case class TorcherStatusWsMessage(status: TorcherStatus)
    extends TorcherWsMessage("TORCHER_STATUS")

  private case class TorcherConfigWsMessage(source: String)
    extends TorcherWsMessage("TORCHER_SOURCE")


}
