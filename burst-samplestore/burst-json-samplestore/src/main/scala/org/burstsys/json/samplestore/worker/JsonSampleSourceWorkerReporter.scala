/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore.worker

import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.instrument.prettySizeString
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterFixedValueMetric}

import java.util.concurrent.atomic.LongAdder

private[worker]
object JsonSampleSourceWorkerReporter extends VitalsReporter {


  final val dName: String = "alloy-worker"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _succeedMetric = VitalsReporterByteOpMetric("alloy_worker_succeed")
  this += _succeedMetric

  private[this]
  val _failMetric = VitalsReporterByteOpMetric("alloy_worker_fail")
  this += _failMetric


  private[this]
  val _workerCount = new LongAdder

  private[this]
  val _loadConcurrencyMetric = VitalsReporterFixedValueMetric("load_concurrency")
  this += _loadConcurrencyMetric

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def onWorkerStart(): Unit = {
    newSample()
    _workerCount.increment()
  }

  def onWorkerCompletion(bytes: Long, elapsedTime: Long): Unit = {
    newSample()
    _succeedMetric.recordOpWithTimeAndSize(elapsedTime, bytes)
  }

  def onWorkerFailure(elapsedTime: Long): Unit = {
    newSample()
    _failMetric.recordOpWithTime(elapsedTime)
  }

  def recordConcurrency(concurrency: Int): Unit = {
    newSample()
    _loadConcurrencyMetric.record(concurrency)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    // the workers attempted since the last report
    val c = _workerCount.sumThenReset()
    val counts = s"\t alloy_workers_count=$c (${prettySizeString(c)})\n"
    s"$counts${
      _succeedMetric.report
    }${
      _failMetric.report
    }${
      _loadConcurrencyMetric.report
    }"
  }

}
