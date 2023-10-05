/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.worker

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterUnitOpMetric

private[samplestore]
object ScanningStoreReporter extends VitalsReporter {

  final val dName: String = "ScanningStore"

  private[this]
  val _readItemsMetric = VitalsReporterUnitOpMetric("read", "items")

  private[this]
  val _readBytesMetric = VitalsReporterUnitOpMetric("read", "bytes")

  private[this]
  val _readRejectMetric = VitalsReporterUnitOpMetric("read", "rejects")

  private[this]
  val _readSkippedMetric = VitalsReporterUnitOpMetric("read", "skipped")

  def onReadComplete(stats: FeedControl, ns: Long, bytes: Long): Unit = {
    _readItemsMetric.recordOpWithTime(ns)
    _readBytesMetric.recordOpWithTimeAndSize(ns, bytes)
    stats.processedItemsCounter.incrementAndGet()
  }

  def onReadReject(stats: FeedControl): Unit = {
    _readRejectMetric.recordOp()
    stats.rejectedItemsCounter.incrementAndGet()
  }

  def onReadSkipped(stats: FeedControl): Unit = {
    _readSkippedMetric.recordOp()
    stats.skipped.set(true)
  }
}
