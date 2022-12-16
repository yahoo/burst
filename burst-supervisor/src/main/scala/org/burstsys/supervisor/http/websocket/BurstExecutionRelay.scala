/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.websocket

import org.burstsys.agent.event.{AgentRequestFailed, AgentRequestStarted, AgentRequestSucceeded}
import org.burstsys.brio.types.BrioTypes
import org.burstsys.fabric.container.http.{FabricWebSocket, FabricWebSocketGroup, FabricWebSocketListener, FabricWebSocketService}
import org.burstsys.fabric.wave.execution.model.execute.parameters
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.gather.metrics.FabricGatherMetrics
import org.burstsys.fabric.wave.execution.model.pipeline.{FabricPipelineEvent, FabricPipelineEventListener}
import org.burstsys.fabric.wave.execution.supervisor.wave._
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.supervisor.http.endpoints.{ClientJsonObject, ClientWebsocketMessage}
import org.burstsys.supervisor.http.service.execution.{ParticleState, RequestState, WaveState, requests}
import org.burstsys.supervisor.http.websocket.ExecutionMessages.ExecutionRelayMessage
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.uid.VitalsUid

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

final case class BurstExecutionRelay(webSocketService: FabricWebSocketService)
  extends FabricPipelineEventListener with FabricWebSocketListener {

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

  override def onEvent: PartialFunction[FabricPipelineEvent, Boolean] = {
    val pf: PartialFunction[FabricPipelineEvent, VitalsUid] = {
      case e: AgentRequestStarted =>
        requests.requestStarted(e.guid, e.source, e.over, e.call)
      case e: AgentRequestSucceeded =>
        requests.requestSucceeded(e.guid)
      case e: AgentRequestFailed =>
        requests.requestFailed(e.guid, e.status, e.message)
      case e: WaveBegan =>
        requests.waveStarted(e.guid, e.seqNum)
      case e: WaveTimeout =>
        requests.waveTimedout(e.guid, e.seqNum, e.message)
      case e: WaveFailed =>
        requests.waveFailed(e.guid, e.seqNum, e.message)
      case e: WaveSucceeded =>
        requests.waveSucceeded(e.guid, e.seqNum, e.results.gatherMetrics.toJson)
      case e: ParticleDispatched =>
        requests.particleStarted(e.seqNum, e.ruid, e.host)
      case e: ParticleProgress =>
        requests.particleProgress(e.seqNum, e.ruid, e.message)
      case e: ParticleTardy =>
        requests.particleTardy(e.seqNum, e.ruid)
      case e: ParticleCancelled =>
        requests.particleCancelled(e.seqNum, e.ruid)
      case e: ParticleSucceeded =>
        requests.particleSucceeded(e.seqNum, e.ruid)
      case e: ParticleFailed =>
        requests.particleFailed(e.seqNum, e.ruid, e.message)
      case other =>
        log info s"not handling event $other"
        "000000000000000000000000000000000_000000"
    }
    pf.andThen(uid => _updatedRequests.add(uid))
  }

  ///////////////////////////////////////////////////////////////////////
  // Websocket
  ///////////////////////////////////////////////////////////////////////


  override
  def onWebSocketOpen(group: FabricWebSocketGroup, socket: FabricWebSocket): Unit = {
    socket.sendJson(ExecutionRelayMessage(requests.lastReset))
    requests.foreach(r => socket.sendJson(ExecutionRelayMessage(r.guid, r)))
  }

  override def onWebSocketAction(group: FabricWebSocketGroup, socket: FabricWebSocket, action: String, payload: Map[String, Any]): Unit = {
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

object ExecutionMessages {

  object ExecutionRelayMessage {
    def apply(guid: String, req: RequestState, shallow: Boolean = true): ClientWebsocketMessage = {
      val msgType = if (shallow) "request-update" else "request-details"
      if (req == null) ExecutionRelayMessageContext(msgType, guid, null)
      else ExecutionRelayMessageContext(msgType, guid, FabricRequestJson(req, shallow))
    }

    def apply(resetMillis: Long): ClientWebsocketMessage =
      ExecutionRelayStatusMessage("relay-status", resetMillis)
  }

  private final
  case class ExecutionRelayStatusMessage(msgType: String, since: Long)
    extends ClientWebsocketMessage

  private final
  case class ExecutionRelayMessageContext(msgType: String, guid: String, request: FabricRequestJson)
    extends ClientWebsocketMessage


  final
  case class FabricRequestJson(
                                guid: VitalsUid,
                                source: Array[String],
                                over: FabricOver,
                                parameters: Array[FabricParameterJson],
                                state: String,
                                status: String,
                                beginMillis: Long,
                                endMillis: Long,
                                wave: Option[FabricWaveJson]
                              ) extends ClientJsonObject

  object FabricRequestJson {
    def apply(req: RequestState, shallow: Boolean = true): FabricRequestJson = new FabricRequestJson(
      req.guid, req.source.toArray, req.over.toJson, asJson(req.call), req.state.name, req.status, req.startTime, req.endTime, req.wave.map(w => FabricWaveJson(w, shallow))
    )

    private def asJson(call: Option[FabricCall]): Array[FabricParameterJson] = call.map(c => {
      c.parameters.map(p => {
        val datum = p.form match {
          case parameters.FabricScalarForm => p.valueType match {
            case BrioTypes.BrioBooleanKey => p.asScalar[Boolean]
            case BrioTypes.BrioByteKey => p.asScalar[Byte]
            case BrioTypes.BrioShortKey => p.asScalar[Short]
            case BrioTypes.BrioIntegerKey => p.asScalar[Int]
            case BrioTypes.BrioLongKey => p.asScalar[Long]
            case BrioTypes.BrioDoubleKey => p.asScalar[Double]
            case BrioTypes.BrioStringKey => p.asScalar[String]
          }
          case parameters.FabricVectorForm => p.valueType match {
            case BrioTypes.BrioBooleanKey => p.asVector[Boolean]
            case BrioTypes.BrioByteKey => p.asVector[Byte]
            case BrioTypes.BrioShortKey => p.asVector[Short]
            case BrioTypes.BrioIntegerKey => p.asVector[Int]
            case BrioTypes.BrioLongKey => p.asVector[Long]
            case BrioTypes.BrioDoubleKey => p.asVector[Double]
            case BrioTypes.BrioStringKey => p.asVector[String]
          }
          case parameters.FabricMapForm => ???
          case _ => null
        }
        FabricParameterJson(p.name, p.valueType, datum, p.isNull)
      })
    }).getOrElse(Array.empty)
  }

  final
  case class FabricParameterJson(name: String, brioType: Int, value: Any, isNull: Boolean)

  final
  case class FabricWaveJson(
                             seqNum: FabricWaveSeqNum,
                             guid: VitalsUid,
                             state: String,
                             status: String,
                             beginMillis: Long,
                             endMillis: Long,
                             skew: Option[Float],
                             metrics: Option[FabricGatherMetrics],
                             particles: Array[FabricParticleJson],
                             particleSummary: Map[String, Long]
                           ) extends ClientJsonObject

  object FabricWaveJson {
    def apply(wave: WaveState, shallow: Boolean = true): FabricWaveJson = new FabricWaveJson(
      wave.seqNum, wave.guid, wave.state.msg, wave.status, wave.startMillis, wave.endMillis, wave.particleSkew, wave.metrics,
      if (shallow) Array.empty else wave.particles.values().asScala.map(FabricParticleJson(_)).toArray,
      wave.particleSummary.asScala.map(entry => (entry._1.msg, entry._2)).toMap
    )
  }

  final
  case class FabricParticleJson(
                                 ruid: VitalsUid,
                                 hostname: VitalsHostName,
                                 state: String,
                                 message: String,
                                 updates: Array[ParticleState.Update],
                                 beginMillis: Long,
                                 endMillis: Long
                               ) extends ClientJsonObject

  object FabricParticleJson {
    def apply(p: ParticleState): FabricParticleJson =
      new FabricParticleJson(p.ruid, p.host, p.state.msg, p.message, p.updates.toArray, p.beginMillis, p.endMillis)
  }
}
