/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash

import org.burstsys.brio.types.BrioTypes
import org.burstsys.dash.endpoints.ClientJsonObject
import org.burstsys.dash.endpoints.ClientWebsocketMessage
import org.burstsys.dash.service.execution.ParticleState
import org.burstsys.dash.service.execution.RequestState
import org.burstsys.dash.service.execution.WaveState
import org.burstsys.dash.service.thrift.ThriftRequest
import org.burstsys.fabric.configuration.burstFabricTopologyHomogeneous
import org.burstsys.fabric.wave.execution.supervisor.wave.FabricWaveSeqNum
import org.burstsys.fabric.wave.execution.model.execute.parameters
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.gather.metrics.FabricGatherMetrics
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerProxy
import org.burstsys.fabric.topology.model.node.worker.JsonFabricWorker
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.uid.VitalsUid

import scala.jdk.CollectionConverters._


package object websocket extends VitalsLogger {

  //////////////////////////////////////////////////////////////////////////////
  // Worker topology
  //////////////////////////////////////////////////////////////////////////////

  object FabricTopologyMessage {

    def apply(workers: Array[JsonFabricWorker]): ClientWebsocketMessage =
      FabricWorkersMessageContext(workers = workers, homogeneous = burstFabricTopologyHomogeneous.getOrThrow)

  }

  private final
  case class FabricWorkersMessageContext(workers: Array[JsonFabricWorker], homogeneous: Boolean) extends ClientWebsocketMessage {
    val msgType = "update"
  }

  //////////////////////////////////////////////////////////////////////////////
  // Wave execution
  //////////////////////////////////////////////////////////////////////////////

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

  //////////////////////////////////////////////////////////////////////////////
  // Wave execution
  //////////////////////////////////////////////////////////////////////////////

  object ThriftRequests {
    final case class AllThriftRequests(requests: Array[ThriftRequest]) extends ClientWebsocketMessage {
      val msgType: String = "AllRequests"
    }

    final case class ThriftRequestReceived(ruid: VitalsUid) extends ClientWebsocketMessage {
      val msgType: String = "RequestReceived"
    }
    final case class ThriftRequestUpdate(req: ThriftRequest) extends ClientWebsocketMessage {
      val msgType: String = "RequestUpdate"
    }
    final case class ThriftRequestEncouteredException(ruid: VitalsUid, exception: Throwable) extends ClientWebsocketMessage {
      val msgType: String = "RequestEncouteredException"
    }
  }

}
