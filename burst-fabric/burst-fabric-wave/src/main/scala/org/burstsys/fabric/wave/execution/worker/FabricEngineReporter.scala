/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.worker

import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.gather.data.FabricDataGather
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterUnitOpMetric

private[fabric]
object FabricEngineReporter extends VitalsReporter {

  final val dName: String = "fab-engine"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _snapFetchMetric = VitalsReporterUnitOpMetric("engine_snap", "fetch")
  this += _snapFetchMetric

  private[this]
  val _scanSuccessMetric = VitalsReporterUnitOpMetric("engine_scan", "success")
  this += _scanSuccessMetric

  private[this]
  val _scanFailureMetric = VitalsReporterUnitOpMetric("engine_scan", "fail")
  this += _scanFailureMetric

  private[this]
  val _scanRowsMetric = VitalsReporterUnitOpMetric("engine_scan", "row")
  this += _scanRowsMetric

  private[this]
  val _scanQueriesMetric = VitalsReporterUnitOpMetric("engine_scan", "query")
  this += _scanQueriesMetric

  private[this]
  val _scanOverflowMetric = VitalsReporterUnitOpMetric("engine_scan", "overflow")
  this += _scanOverflowMetric

  private[this]
  val _scanLimitMetric = VitalsReporterUnitOpMetric("engine_scan", "limit")
  this += _scanLimitMetric


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def snapFetch(elapsedNs: Long): Unit = {
    newSample()
    _snapFetchMetric.recordOpWithTime(elapsedNs)
  }

  /**
   * record a successful analysis run
   *
   * @param elapsedNs
   * @param gather
   */
  def successfulScan(elapsedNs: Long, gather: FabricGather): Unit = {
    newSample()
    gather match {
      case gather: FabricDataGather =>
        if (gather.successCount == gather.queryCount) {
          _scanSuccessMetric.recordOpWithTime(elapsedNs)
          _scanRowsMetric.recordOps(gather.rowCount)
          _scanQueriesMetric.recordOps(gather.queryCount)
          _scanOverflowMetric.recordOps(gather.overflowCount)
          _scanLimitMetric.recordOps(gather.limitCount)
        } else {
          _scanFailureMetric.recordOp()
        }

      case gather: FabricFaultGather =>

    }
  }

  /**
   * record a failed analysis run
   *
   */
  def failedScan(): Unit = {
    newSample()
    _scanFailureMetric.recordOp()
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def report: String = {
    if (nullData) return ""
    s"${_snapFetchMetric.report}${_scanSuccessMetric.report}${_scanFailureMetric.report}${_scanQueriesMetric.report}${_scanRowsMetric.report}${_scanOverflowMetric.report}${_scanLimitMetric.report}"
  }

}
