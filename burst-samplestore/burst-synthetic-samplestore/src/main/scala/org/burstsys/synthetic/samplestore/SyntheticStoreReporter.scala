/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterUnitOpMetric

private[samplestore]
object SyntheticStoreReporter extends VitalsReporter {

  final val dName: String = "SyntheticStore"

  private[this]
  val _readItemsMetric = VitalsReporterUnitOpMetric("read", "items")

  private[this]
  val _readBytesMetric = VitalsReporterUnitOpMetric("read", "bytes")

  private[this]
  val _readRejectMetric = VitalsReporterUnitOpMetric("read", "rejects")

  private[this]
  val _readSkippedMetric = VitalsReporterUnitOpMetric("read", "skipped")

  def onReadComplete(ns: Long, bytes: Long): Unit = {
    _readItemsMetric.recordOpWithTime(ns)
    _readBytesMetric.recordOpWithTimeAndSize(ns, bytes)
  }

  def onReadReject(): Unit = {
    _readRejectMetric.recordOp()
  }

  def onReadSkipped(): Unit = {
    _readSkippedMetric.recordOp()
  }
}
