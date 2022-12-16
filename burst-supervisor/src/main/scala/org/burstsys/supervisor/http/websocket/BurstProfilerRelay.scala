/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.websocket

import org.burstsys.fabric.container.http.FabricWebSocket
import org.burstsys.fabric.container.http.FabricWebSocketGroup
import org.burstsys.fabric.container.http.FabricWebSocketListener
import org.burstsys.fabric.container.http.FabricWebSocketService
import org.burstsys.supervisor.http.endpoints.ClientWebsocketMessage
import org.burstsys.supervisor.http.service.provider.BurstWaveProfilerListener
import org.burstsys.supervisor.http.service.provider.BurstWaveSupervisorProfilerService
import org.burstsys.supervisor.http.service.provider.ProfilerConfig
import org.burstsys.supervisor.http.service.provider.ProfilerEvent
import org.burstsys.supervisor.http.websocket.ProfilerMessages.ProfilerRelayMessage

final case class BurstProfilerRelay(profiler: BurstWaveSupervisorProfilerService, webSocketService: FabricWebSocketService)
  extends FabricWebSocketListener with BurstWaveProfilerListener {

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Private State
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  private val _socketGroup = webSocketService.open("/profiler", this)

  override def onWebSocketOpen(group: FabricWebSocketGroup, socket: FabricWebSocket): Unit = {
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

object ProfilerMessages {

  object ProfilerRelayMessage {
    def apply(config: ProfilerConfig, events: Array[ProfilerEvent]): ClientWebsocketMessage =
      ProfilerAttachMessage("profiler-attach", config, events)

    def apply(event: ProfilerEvent): ClientWebsocketMessage =
      ProfilerEventMessage("profiler-event", event)

    private case class ProfilerAttachMessage(msgType: String, config: ProfilerConfig, events: Array[ProfilerEvent])
      extends ClientWebsocketMessage

    private case class ProfilerEventMessage(msgType: String, event: ProfilerEvent)
      extends ClientWebsocketMessage
  }

}
