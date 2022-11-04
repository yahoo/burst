/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.supervisor.wave

import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.gather.data.{FabricDataGather, FabricEmptyGather}
import org.burstsys.tesla.scatter.slot.TeslaScatterSlot
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterFixedValueMetric, VitalsReporterFloatValueMetric, VitalsReporterUnitOpMetric}

private[fabric]
object FabricWaveReporter extends VitalsReporter {

  final val dName: String = "fab-wave"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _waveSuccessMetric = VitalsReporterUnitOpMetric("wave_success")
  this += _waveSuccessMetric

  private[this]
  val _waveFailMetric = VitalsReporterUnitOpMetric("wave_fail")
  this += _waveFailMetric

  private[this]
  val _waveDataSizeMetric = VitalsReporterFixedValueMetric("wave_bytes")
  this += _waveDataSizeMetric

  private[this]
  val _waveTimeSkewMetric = VitalsReporterFloatValueMetric("wave_time_skew")
  this += _waveTimeSkewMetric

  private[this]
  val _waveSizeSkewMetric = VitalsReporterFloatValueMetric("wave_size_skew")
  this += _waveSizeSkewMetric

  private[this]
  val _waveDataItemsMetric = VitalsReporterFixedValueMetric("wave_items")
  this += _waveDataItemsMetric

  private[this]
  val _waveQueryMetric = VitalsReporterUnitOpMetric("wave", "query")
  this += _waveQueryMetric

  private[this]
  val _waveRowsMetric = VitalsReporterUnitOpMetric("wave", "row")
  this += _waveRowsMetric

  private[this]
  val _waveQueryFail = VitalsReporterUnitOpMetric("wave_query", "fail")
  this += _waveQueryFail

  private[this]
  val _waveQueryOverflow = VitalsReporterUnitOpMetric("wave_query", "overflow")
  this += _waveQueryOverflow

  private[this]
  val _waveQueryLimit = VitalsReporterUnitOpMetric("wave_query", "limit")
  this += _waveQueryLimit

  private[this]
  val _waveConcurrencyMetric = VitalsReporterFixedValueMetric("wave_concurrency")
  this += _waveConcurrencyMetric

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: FabricWaveSeqNum): Unit = {
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def recordConcurrency(concurrency: Int): Unit = {
    newSample()
    _waveConcurrencyMetric.record(concurrency)
  }

  /**
   * record a successful analysis run
   *
   * @param elapsedNs
   * @param gather
   */
  def successfulAnalysis(elapsedNs: Long, gather: FabricGather): Unit = {
    newSample()
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
      case gather: FabricFaultGather =>
        _waveFailMetric.recordOp()
    }
  }

  /**
   * record a failed analysis run
   *
   * @param elapsedNs
   * @param failedSlots
   */
  def failedWave(elapsedNs: Long, failedSlots: Array[TeslaScatterSlot]): Unit = {
    newSample()
    _waveFailMetric.recordOp()
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def report: String = {
    if (nullData) return ""
    val concurrency = s"${_waveConcurrencyMetric.report}"
    val topline = s"${_waveSuccessMetric.report}${_waveFailMetric.report}"
    val queries = s"${_waveQueryMetric.report}${_waveQueryFail.report}${_waveQueryOverflow.report}${_waveQueryLimit.report}"
    val size = s"${_waveDataSizeMetric.report}${_waveDataItemsMetric.report}"
    val skew = s"${_waveSizeSkewMetric.report}${_waveTimeSkewMetric.report}"
    s"$concurrency$topline$queries$size$skew"
  }

}
