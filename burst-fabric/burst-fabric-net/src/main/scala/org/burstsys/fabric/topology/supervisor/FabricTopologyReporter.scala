/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.supervisor

import io.opentelemetry.api.metrics.LongUpDownCounter
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.vitals.reporter.metric.VitalsReporterUnitOpMetric
import org.burstsys.vitals.reporter.{VitalsReporter, metric}

private[fabric]
object FabricTopologyReporter extends VitalsReporter with FabricTopologyListener {

  final val dName: String = "fab-topo"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _workerGainRateMetric = VitalsReporterUnitOpMetric("topo_worker_gain_rate")

  private[this]
  val _workerLossRateMetric = VitalsReporterUnitOpMetric("topo_worker_loss_rate")

  private[this]
  val _workerCurrentCounter: LongUpDownCounter = metric.upDownCounter(s"topo_current_worker_counter")
    .setDescription(s"current active workers")
    .setUnit("worker")
    .build()

  private[this]
  val _workerTotalGain = metric.counter(s"topo_worker_gain")
    .setDescription("total workers gained")
    .setUnit("worker")
    .build()

  private[this]
  val _workerTotalLoss = metric.counter(s"topo_worker_gain")
    .setDescription("total workers gained")
    .setUnit("worker")
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def onTopologyWorkerGain(worker: FabricTopologyWorker): Unit = {
    _workerGainRateMetric.recordOp()
    _workerCurrentCounter.add(1)
    _workerTotalGain.add(1)
  }

  override
  def onTopologyWorkerLoss(worker: FabricTopologyWorker): Unit = {
    _workerLossRateMetric.recordOp()
    _workerCurrentCounter.add(-1)
    _workerTotalLoss.add(1)
  }
}
