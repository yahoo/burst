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

  private[this]
  val _scanSuccessMetric = VitalsReporterUnitOpMetric("engine_scan", "success")

  private[this]
  val _scanFailureMetric = VitalsReporterUnitOpMetric("engine_scan", "fail")

  private[this]
  val _scanRowsMetric = VitalsReporterUnitOpMetric("engine_scan", "row")

  private[this]
  val _scanQueriesMetric = VitalsReporterUnitOpMetric("engine_scan", "query")

  private[this]
  val _scanOverflowMetric = VitalsReporterUnitOpMetric("engine_scan", "overflow")

  private[this]
  val _scanLimitMetric = VitalsReporterUnitOpMetric("engine_scan", "limit")


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def snapFetch(elapsedNs: Long): Unit = {
        _snapFetchMetric.recordOpWithTime(elapsedNs)
  }

  /**
   * record a successful analysis run
   */
  def successfulScan(elapsedNs: Long, gather: FabricGather): Unit = {
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

      case _: FabricFaultGather =>

    }
  }

  /**
   * record a failed analysis run
   *
   */
  def failedScan(): Unit = {
        _scanFailureMetric.recordOp()
  }
}
