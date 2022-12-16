/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.execution

import java.util.concurrent.ConcurrentHashMap
import java.util.function
import org.burstsys.supervisor.http.endpoints.ClientJsonObject
import ExecutionState.{ExecutionState, inProgress}
import org.burstsys.fabric.wave.execution.supervisor.wave.FabricWaveSeqNum
import org.burstsys.fabric.wave.execution.model.gather.metrics.FabricGatherMetrics
import org.burstsys.vitals.uid.VitalsUid

import scala.jdk.CollectionConverters._

object WaveState {
  private[execution] val accumulate: function.BiFunction[ExecutionState, Long, Long] = new function.BiFunction[ExecutionState, Long, Long] {
    override def apply(t: ExecutionState, value: Long): Long = value + 1
  }
}

final case
class WaveState(
                 seqNum: FabricWaveSeqNum,
                 guid: VitalsUid,
                 startMillis: Long = System.currentTimeMillis,
                 var state: ExecutionState = inProgress,
                 var status: String = "Wave started",
                 var endMillis: Long = 0,
                 var particleSkew: Option[Float] = None,
                 var metrics: Option[FabricGatherMetrics] = None
               ) extends ClientJsonObject {

  def computeParticleSkew(): Unit = {
    val durations = particles.values.asScala.map(p => p.endMillis - p.beginMillis)
    if (durations.nonEmpty) {
      val (max, min) = (durations.max.toFloat, durations.min.toFloat)
      particleSkew = Some((max - min) / min)
    }
  }

  def updateSummary(): Unit = {
    particleSummary.clear()
    particles.values.asScala.foreach(p => particleSummary.compute(p.state, WaveState.accumulate))
  }

  val particles: ConcurrentHashMap[VitalsUid, ParticleState] = new ConcurrentHashMap[VitalsUid, ParticleState]()

  val particleSummary: ConcurrentHashMap[ExecutionState, Long] = new ConcurrentHashMap[ExecutionState, Long]()
}
