/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.websocket

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.agent.event.{AgentRequestFailed, AgentRequestStarted, AgentRequestSucceeded}
import org.burstsys.dash.application.websocket.{BurstDashWebSocketListener, BurstWebSocket, BurstWebSocketGroup, BurstWebSocketService}
import org.burstsys.dash.service.execution.{RequestState, requests}
import org.burstsys.fabric.execution.master.wave.{ParticleCancelled, ParticleDispatched, ParticleFailed, ParticleProgress, ParticleSucceeded, ParticleTardy, WaveBegan, WaveFailed, WaveSucceeded, WaveTimeout}
import org.burstsys.fabric.execution.model.pipeline.{FabricPipelineEvent, FabricPipelineEventListener}
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.uid.VitalsUid

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

final case class BurstRestExecutionRelay(webSocketService: BurstWebSocketService)
  extends FabricPipelineEventListener with BurstDashWebSocketListener {

  private val _socket = webSocketService.open("/execution", this)

  private val _updatedRequests = ConcurrentHashMap.newKeySet[VitalsUid]()
  private val _updatePusher = new VitalsBackgroundFunction("execution-pusher", 500 millis, 500 millis, {
    val itr = _updatedRequests.iterator()
    while (itr.hasNext) {
      val guid = itr.next()
      itr.remove()
      requests.get(guid).foreach(broadcastRequestUpdate)
    }
  })
  _updatePusher.start

  ///////////////////////////////////////////////////////////////////////
  // Listen for events
  ///////////////////////////////////////////////////////////////////////

  override def onEvent: PartialFunction[FabricPipelineEvent, Unit] = {
    val pf: PartialFunction[FabricPipelineEvent, VitalsUid] = {
      case e: AgentRequestStarted => requests.requestStarted(e.guid, e.source, e.over, e.call)
      case e: AgentRequestSucceeded => requests.requestSucceeded(e.guid)
      case e: AgentRequestFailed => requests.requestFailed(e.guid, e.status, e.message)
      case e: WaveBegan => requests.waveStarted(e.guid, e.seqNum)
      case e: WaveTimeout => requests.waveTimedout(e.guid, e.seqNum, e.message)
      case e: WaveFailed => requests.waveFailed(e.guid, e.seqNum, e.message)
      case e: WaveSucceeded => requests.waveSucceeded(e.guid, e.seqNum, e.results.gatherMetrics.toJson)
      case e: ParticleDispatched => requests.particleStarted(e.seqNum, e.ruid, e.host)
      case e: ParticleProgress => requests.particleProgress(e.seqNum, e.ruid, e.message)
      case e: ParticleTardy => requests.particleTardy(e.seqNum, e.ruid)
      case e: ParticleCancelled => requests.particleCancelled(e.seqNum, e.ruid)
      case e: ParticleSucceeded => requests.particleSucceeded(e.seqNum, e.ruid)
      case e: ParticleFailed => requests.particleFailed(e.seqNum, e.ruid, e.message)
      case other =>
        log info s"not handling event $other"
        "000000000000000000000000000000000_000000"
    }
    pf andThen _updatedRequests.add
  }

  ///////////////////////////////////////////////////////////////////////
  // Websocket
  ///////////////////////////////////////////////////////////////////////


  override
  def onWebSocketOpen(group: BurstWebSocketGroup, socket: BurstWebSocket): Unit = {
    socket.sendJson(ExecutionRelayMessage(requests.lastReset))
    requests.foreach(r => socket.sendJson(ExecutionRelayMessage(r.guid, r)))
  }

  override def onWebSocketAction(group: BurstWebSocketGroup, socket: BurstWebSocket, action: String, payload: Map[String, Any]): Unit = {
    action match {
      case "clear-executions" =>
        requests.clear()
        group.broadcastJson(ExecutionRelayMessage(requests.lastReset))

      case "get-request-details" =>
        payload.get("guid") match {
          case Some(guid: String) =>
            requests.get(guid) match {
              case Some(r) => socket.sendJson(ExecutionRelayMessage(guid, r, shallow = false))
              case None =>
                log info s"ExecutionRelay received unknown guid '$guid'"
                socket.sendJson(ExecutionRelayMessage(guid, null, shallow = false))
            }
          case other => log info s"ExecutionRelay received unknown guid '$other'"
        }

      case unknown => log info s"ExecutionRelay - unknown action '$unknown'"
    }
  }

  private[this]
  def broadcastRequestUpdate(request: RequestState): Unit = {
    _socket.broadcastJson(ExecutionRelayMessage(request.guid, request))
  }
}
