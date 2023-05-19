/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.{LongCounter, LongHistogram}
import org.burstsys.vitals.reporter.metric

/**
 * measures all aspects of a recurring operation that takes a certain amount of time and has a varying 'unit' size.
 * you can choose what to record and the metric prints out only what it can derive from what you record
 * <p/>
 * <h2>`{name}_op_rate`</h2>
 * <ol>
 * <li><b>`{name}_op_rate_1m`:</b> ''1 minute op/s''</li>
 * <li><b>`{name}_op_rate_5m`:</b> ''5 minute op/s''</li>
 * <li><b>`{name}_op_rate_15m`:</b> ''15 minute op/s''</li>
 * </ol>
 * <h2>`{name}_op_latency`</h2>
 * if `withTimes` (this is an exponential decay histogram)
 * <ol>
 * <li><b>{name}_op_latency_min:</b> ''min ms latency''</li>
 * <li><b>`{name}_op_latency_mean`:</b> ''mean ms latency''</li>
 * <li><b>`{name}_op_latency_max`:</b> ''max ms latency''</li>
 * </ol>
 * <h2>`{name}_{unit}_rate`</h2>
 * if `withSizes`
 * <ol>
 * <li><b>`{name}_{unit}_rate_1m`:</b> ''1 minute unit/s rate''</li>
 * <li><b>`{name}_{unit}_rate_5m`:</b> ''5 minute unit/s rate''</li>
 * <li><b>`{name}_{unit}_rate_15m`:</b> ''15 minute unit/s rate''</li>
 * </ol>
 * <h2>`{name}_{unit}_size`</h2>
 * if `withSizes` (this is an exponential decay histogram)
 * <ol>
 * <li><b>`{name}_{unit}_size_min`:</b> ''min unit size''</li>
 * <li><b>`{name}_{unit}_size_mean`:</b> ''mean unit size''</li>
 * <li><b>`{name}_{unit}_size_max`:</b> ''max unit size''</li>
 * </ol>
 */
trait VitalsReporterUnitOpMetric extends VitalsReporterMetric {

  def recordOp(): Unit

  def recordOps(ops: Long): Unit

  def recordOpWithTime(ns: Long): Unit

  def recordOpsWithTime(ops: Long, ns: Long): Unit

  def recordOpWithSize(bytes: Long): Unit

  def recordOpsWithSize(ops: Long, bytes: Long): Unit

  def recordOpWithTimeAndSize(ns: Long, bytes: Long): Unit

  def recordOpsWithTimeAndSize(ops: Long, ns: Long, bytes: Long): Unit

}

object VitalsReporterUnitOpMetric {

  def apply(name: String, unit: String = "ops"): VitalsReporterUnitOpMetric =
    VitalsReporterUnitOpMetricContext(name: String, unit: String)

}

private final case
class VitalsReporterUnitOpMetricContext(name: String, unit: String) extends VitalsReporterMetricContext(name) with VitalsReporterUnitOpMetric {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private def fullName = s"${name}_$unit"

  private[this]
  val _opCounter: LongCounter = meter.counterBuilder(s"${fullName}_count")
    .setUnit("ops")
    .setDescription(s"$name $unit count")
    .build()

  private[this]
  val _opTimeHist: LongHistogram = meter.histogramBuilder(s"${fullName}_time_histo").ofLongs()
    .setDescription(s"$name $unit time histogram")
    .setUnit("nanos")
    .build()

  private[this]
  val _sizeCounter: LongCounter = meter.counterBuilder(s"${fullName}_size")
    .setUnit("bytes")
    .setDescription(s"$name bytes of data")
    .build()

  private[this]
  val _opSizeHist: LongHistogram = meter.histogramBuilder(s"${fullName}_size_histo").ofLongs()
    .setDescription(s"$name operation bytes histogram")
    .setUnit("bytes")
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def recordOp(): Unit = {
    _opCounter.add(1, hostNameAttributes)
  }

  override
  def recordOps(ops: Long): Unit = {
    _opCounter.add(ops, hostNameAttributes)
  }

  override
  def recordOpWithTime(ns: Long): Unit = {
    _opCounter.add(1, hostNameAttributes)
    _opTimeHist.record(ns, hostNameAttributes)
  }

  override
  def recordOpsWithTime(ops: Long, ns: Long): Unit = {
    _opCounter.add(ops, hostNameAttributes)
    _opTimeHist.record(ns, hostNameAttributes)
  }

  override
  def recordOpWithSize(bytes: Long): Unit = {
    _opCounter.add(1, hostNameAttributes)
    _sizeCounter.add(bytes, hostNameAttributes)
    _opSizeHist.record(bytes, hostNameAttributes)
  }

  override
  def recordOpsWithSize(ops: Long, bytes: Long): Unit = {
    _opCounter.add(ops, hostNameAttributes)
    _sizeCounter.add(bytes, hostNameAttributes)
    _opSizeHist.record(bytes, hostNameAttributes)
  }

  override
  def recordOpWithTimeAndSize(ns: Long, bytes: Long): Unit = {
    _opCounter.add(1, hostNameAttributes)
    _opTimeHist.record(ns, hostNameAttributes)
    _sizeCounter.add(bytes, hostNameAttributes)
    _opSizeHist.record(bytes, hostNameAttributes)
  }

  override
  def recordOpsWithTimeAndSize(ops: Long, ns: Long, bytes: Long): Unit = {
    _opCounter.add(ops, hostNameAttributes)
    _opTimeHist.record(ns, hostNameAttributes)
    _sizeCounter.add(bytes, hostNameAttributes)
    _opSizeHist.record(bytes, hostNameAttributes)
  }
}
