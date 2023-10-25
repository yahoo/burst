/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.pipeline

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterUnitOpMetric

private[pipeline]
object PressPipelineReporter extends VitalsReporter {

  final val dName: String = "brio"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _pressItemsMetric = VitalsReporterUnitOpMetric("press", "items")

  private[this]
  val _pressSlowMetric = VitalsReporterUnitOpMetric("press_slow")

  private[this]
  val _pressBytesMetric = VitalsReporterUnitOpMetric("press_item")

  private[this]
  val _pressRejectMetric = VitalsReporterUnitOpMetric("press", "rejects")

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def onPressComplete(ns: Long, bytes: Long): Unit = {
    _pressItemsMetric.recordOpWithTime(ns)
    _pressBytesMetric.recordOpWithTimeAndSize(ns, bytes)
  }

  def onPressReject(): Unit = {
    _pressRejectMetric.recordOp()
  }

  def onPressSlow(): Unit = {
    _pressSlowMetric.recordOp()
  }
}
