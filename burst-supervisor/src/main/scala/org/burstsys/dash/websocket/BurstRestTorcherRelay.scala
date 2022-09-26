/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.websocket

import org.burstsys.dash.application.websocket.{BurstDashWebSocketListener, BurstWebSocket, BurstWebSocketGroup, BurstWebSocketService}
import org.burstsys.dash.provider.torcher.{BurstDashTorcherService, TorcherEventListener, TorcherLogMessage, TorcherMessage}
import org.burstsys.vitals.background.VitalsBackgroundFunction

import scala.concurrent.duration._
import scala.language.postfixOps

final case class BurstRestTorcherRelay(torcher: BurstDashTorcherService, websocketService: BurstWebSocketService)
  extends BurstDashWebSocketListener with TorcherEventListener {

  private val socketGroup: BurstWebSocketGroup = websocketService.open("/torcher", this)

  private val backgroundUpdater = new VitalsBackgroundFunction("torcher-sleeper", 1 second, 1 second, {
    socketGroup.broadcastJson(TorcherMessage(torcher.status))
  })

  override def onWebSocketOpen(group: BurstWebSocketGroup, socket: BurstWebSocket): Unit = {
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
