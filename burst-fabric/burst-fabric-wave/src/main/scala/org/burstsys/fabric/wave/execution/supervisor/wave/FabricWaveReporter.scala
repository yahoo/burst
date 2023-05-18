/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.supervisor.wave

import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.gather.data.FabricDataGather
import org.burstsys.tesla.scatter.slot.TeslaScatterSlot
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterFixedValueMetric, VitalsReporterFloatValueMetric, VitalsReporterUnitOpMetric}

import scala.annotation.unused

private[fabric]
object FabricWaveReporter extends VitalsReporter {

  final val dName: String = "fab-wave"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _waveSuccessMetric = VitalsReporterUnitOpMetric("wave_success")

  private[this]
  val _waveFailMetric = VitalsReporterUnitOpMetric("wave_fail")

  private[this]
  val _waveDataSizeMetric = VitalsReporterFixedValueMetric("wave_bytes")

  private[this]
  val _waveTimeSkewMetric = VitalsReporterFloatValueMetric("wave_time_skew")

  private[this]
  val _waveSizeSkewMetric = VitalsReporterFloatValueMetric("wave_size_skew")

  private[this]
  val _waveDataItemsMetric = VitalsReporterFixedValueMetric("wave_items")

  private[this]
  val _waveQueryMetric = VitalsReporterUnitOpMetric("wave", "query")

  private[this]
  val _waveRowsMetric = VitalsReporterUnitOpMetric("wave", "row")

  private[this]
  val _waveQueryFail = VitalsReporterUnitOpMetric("wave_query", "fail")

  private[this]
  val _waveQueryOverflow = VitalsReporterUnitOpMetric("wave_query", "overflow")

  private[this]
  val _waveQueryLimit = VitalsReporterUnitOpMetric("wave_query", "limit")

  private[this]
  val _waveConcurrencyMetric = VitalsReporterFixedValueMetric("wave_concurrency")

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def recordConcurrency(concurrency: Int): Unit = {
        _waveConcurrencyMetric.record(concurrency)
  }

  /**
   * record a successful analysis run
   */
  def successfulAnalysis(elapsedNs: Long, gather: FabricGather): Unit = {
        gather match {
      case gather: FabricDataGather =>
        if (gather.successCount == gather.queryCount) {
          _waveSuccessMetric.recordOpWithTime(elapsedNs)
          _waveQueryMetric.recordOps(gather.queryCount)
          _waveQueryLimit.recordOps(gather.limitCount)
          _waveQueryOverflow.recordOps(gather.overflowCount)
          _waveRowsMetric.recordOps(gather.rowCount)
          _waveDataSizeMetric.record(gather.gatherMetrics.generationMetrics.byteCount)
          _waveDataItemsMetric.record(gather.gatherMetrics.generationMetrics.itemCount)
          _waveSizeSkewMetric.record(gather.gatherMetrics.generationMetrics.sizeSkew)
          _waveTimeSkewMetric.record(gather.gatherMetrics.generationMetrics.timeSkew)
        } else {
          _waveFailMetric.recordOp()
          _waveQueryFail.recordOps(gather.queryCount - gather.successCount)
        }
      case _: FabricFaultGather =>
        _waveFailMetric.recordOp()
    }
  }

  /**
   * record a failed analysis run
   */
  def failedWave(@unused elapsedNs: Long, @unused failedSlots: Array[TeslaScatterSlot]): Unit = {
        _waveFailMetric.recordOp()
  }
}
