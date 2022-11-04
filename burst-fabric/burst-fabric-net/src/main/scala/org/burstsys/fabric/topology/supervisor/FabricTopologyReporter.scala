/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.supervisor

import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.model.node.FabricNodeId
import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterFixedValueMetric}

import java.util.concurrent.atomic.LongAdder

private[fabric]
object FabricTopologyReporter extends VitalsReporter with FabricTopologyListener {

  final val dName: String = "fab-topo"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _workerCountMetric = VitalsReporterFixedValueMetric("topo_worker_count")

  private[this]
  val _workerGainRateMetric = VitalsReporterByteOpMetric("topo_worker_gain_rate")

  private[this]
  val _workerLossRateMetric = VitalsReporterByteOpMetric("topo_worker_loss_rate")

  private[this]
  val _workerCurrentCount = new LongAdder()

  private[this]
  val _workerTotalGain = new LongAdder()

  private[this]
  val _workerTotalLoss = new LongAdder()

  this +=_workerCountMetric
  this +=_workerGainRateMetric
  this += _workerLossRateMetric

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: FabricNodeId): Unit = {
    _workerCountMetric.record(_workerCurrentCount.longValue)
    if(_workerCurrentCount.longValue > 0) newSample() // keep it going
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def onTopologyWorkerGain(worker: FabricTopologyWorker): Unit = {
    newSample()
    _workerGainRateMetric.recordOp()
    _workerCurrentCount.increment()
    _workerTotalGain.increment()
  }

  override
  def onTopologyWorkerLoss(worker: FabricTopologyWorker): Unit = {
    newSample()
    _workerLossRateMetric.recordOp()
    _workerCurrentCount.decrement()
    _workerTotalLoss.increment()
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def report: String = {
    if (nullData) return ""
    s"\ttopo_worker_current_count=${_workerCurrentCount.longValue}, topo_worker_total_loss=${_workerTotalLoss.longValue}, topo_worker_total_gain=${_workerTotalGain.longValue},\n${_workerGainRateMetric.report}${_workerLossRateMetric.report}"
  }

}
