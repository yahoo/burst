/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterUnitOpMetric}

private[brio]
object BrioReporter extends VitalsReporter {

  final val dName: String = "brio"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _pressItemsMetric = VitalsReporterUnitOpMetric("brio_press", "items")
  this += _pressItemsMetric

  private[this]
  val _pressSlowMetric = VitalsReporterUnitOpMetric("brio_press_slow")
  this += _pressSlowMetric

  private[this]
  val _pressBytesMetric = VitalsReporterByteOpMetric("brio_press_item")
  this += _pressBytesMetric

  private[this]
  val _pressRejectMetric = VitalsReporterUnitOpMetric("brio_press", "rejects")
  this += _pressRejectMetric

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def onPressComplete(ns: Long, bytes: Long): Unit = {
    newSample()
    _pressItemsMetric.recordOpWithTime(ns)
    _pressBytesMetric.recordOpWithTimeAndSize(ns, bytes)
  }

  def onPressReject(): Unit = {
    newSample()
    _pressRejectMetric.recordOp()
  }

  def onPressSlow(): Unit = {
    newSample()
    _pressSlowMetric.recordOp()
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    s"${_pressItemsMetric.report}${_pressBytesMetric.report}${_pressRejectMetric.report}${_pressSlowMetric.report}"
  }

}
