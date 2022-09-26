/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.websocket

import org.burstsys.dash.application.websocket.{BurstDashWebSocketListener, BurstWebSocket, BurstWebSocketGroup, BurstWebSocketService}
import org.burstsys.dash.provider.profiler._

final case class BurstRestProfilerRelay(profiler: BurstDashProfilerService, webSocketService: BurstWebSocketService)
  extends BurstDashWebSocketListener with BurstDashProfilerListener {

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Private State
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  private val _socketGroup = webSocketService.open("/profiler", this)

  override def onWebSocketOpen(group: BurstWebSocketGroup, socket: BurstWebSocket): Unit = {
    socket.sendJson(ProfilerRelayMessage(profiler.config, profiler.getEvents))
  }

  override def profilerStarted(config: ProfilerConfig): Unit = {
    _socketGroup.broadcastJson(ProfilerRelayMessage(profiler.config, profiler.getEvents))
  }

  override def profilerEvent(event: ProfilerEvent): Unit = {
    _socketGroup.broadcastJson(ProfilerRelayMessage(event))
  }

  override def profilerStopped(): Unit = {}
}
