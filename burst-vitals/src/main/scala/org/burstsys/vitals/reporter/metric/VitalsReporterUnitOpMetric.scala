/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import java.util.concurrent.atomic.AtomicBoolean

import com.codahale.metrics.{ExponentiallyDecayingReservoir, Histogram, Meter}
import org.burstsys.vitals.instrument._

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

  def recordOpWithSize(units: Long): Unit

  def recordOpsWithSize(ops: Long, units: Long): Unit

  def recordOpWithTimeAndSize(ns: Long, units: Long): Unit

  def recordOpsWithTimeAndSize(ops: Long, ns: Long, units: Long): Unit

}

object VitalsReporterUnitOpMetric {

  def apply(name: String, unit: String = "no_units"): VitalsReporterUnitOpMetric =
    VitalsReporterUnitOpMetricContext(name: String, unit: String)

}

private final case
class VitalsReporterUnitOpMetricContext(name: String, unit: String) extends VitalsReporterMetricContext(name) with VitalsReporterUnitOpMetric {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _opMeter: Meter = new Meter()

  private[this]
  var _opTimeHistogram: Histogram = defaultHistogram()

  private[this]
  val _sizeMeter: Meter = new Meter()

  private[this]
  var _opSizeHistogram: Histogram = defaultHistogram()

  private[this]
  val _withTimes: AtomicBoolean = new AtomicBoolean

  private[this]
  val _withSizes: AtomicBoolean = new AtomicBoolean

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def sample(sampleMs: Long): Unit = {
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def recordOp(): Unit = {
    newSample()
    _opMeter.mark()
  }

  override
  def recordOps(ops: Long): Unit = {
    newSample()
    _opMeter.mark(ops)
  }

  override
  def recordOpWithTime(ns: Long): Unit = {
    newSample()
    _withTimes.set(true)
    _opMeter.mark()
    _opTimeHistogram.update(ns)
  }

  override
  def recordOpsWithTime(ops: Long, ns: Long): Unit = {
    newSample()
    _withTimes.set(true)
    _opMeter.mark(ops)
    _opTimeHistogram.update(ns)
  }

  override
  def recordOpWithSize(units: Long): Unit = {
    newSample()
    _withSizes.set(true)
    _opMeter.mark()
    _sizeMeter.mark(units)
    _opSizeHistogram.update(units)
  }

  override
  def recordOpsWithSize(ops: Long, units: Long): Unit = {
    newSample()
    _withSizes.set(true)
    _opMeter.mark(ops)
    _sizeMeter.mark(units)
    _opSizeHistogram.update(units)
  }

  override
  def recordOpWithTimeAndSize(ns: Long, units: Long): Unit = {
    newSample()
    _withTimes.set(true)
    _withSizes.set(true)
    _opMeter.mark()
    _opTimeHistogram.update(ns)
    _opSizeHistogram.update(units)
    _sizeMeter.mark(units)
  }

  override
  def recordOpsWithTimeAndSize(ops: Long, ns: Long, units: Long): Unit = {
    newSample()
    _withTimes.set(true)
    _withSizes.set(true)
    _opMeter.mark(ops)
    _opTimeHistogram.update(ns)
    _opSizeHistogram.update(units)
    _sizeMeter.mark(units)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def report: String = {
    if (nullData) return ""

    // {name}_op_rate
    val opRate1m = _opMeter.getOneMinuteRate
    val opRate5m = _opMeter.getFiveMinuteRate
    val opRate15m = _opMeter.getFifteenMinuteRate
    val opRates = s"\t${dName}_op_rate_1m=$opRate1m per sec (${prettySizeString(opRate1m)}), ${dName}_op_rate_5m=$opRate5m per sec (${prettySizeString(opRate5m)}), ${dName}_op_rate_15m=$opRate15m per sec (${prettySizeString(opRate15m)}) \n"

    // {name}_op_latency
    val minLatency = _opTimeHistogram.getSnapshot.getMin
    val meanLatency = _opTimeHistogram.getSnapshot.getMean
    val maxLatency = _opTimeHistogram.getSnapshot.getMax
    val opLatencies = if (!_withTimes.get) "" else
      s"\t${dName}_op_latency_min=$minLatency ns (${prettyTimeFromNanos(minLatency)}), ${dName}_op_latency_mean=$meanLatency ns (${prettyTimeFromNanos(meanLatency)}), ${dName}_op_latency_max=$maxLatency ns (${prettyTimeFromNanos(maxLatency)}) \n"

    // {name}_unit_rate
    val unitRate1m = _sizeMeter.getOneMinuteRate
    val unitRate5m = _sizeMeter.getFiveMinuteRate
    val unitRate15m = _sizeMeter.getFifteenMinuteRate
    val unitRates = if (!_withSizes.get) "" else
      s"\t${dName}_${unit}_rate_1m=$unitRate1m (${prettySizeString(unitRate1m)} $unit/s), ${dName}_${unit}_rate_5m=$unitRate5m (${prettySizeString(unitRate5m.toLong)} $unit/s), ${dName}_${unit}_rate_15m=$unitRate15m (${prettySizeString(unitRate15m)} $unit/s) \n"

    // {name}_unit_size
    val minUnitSize = _opSizeHistogram.getSnapshot.getMin
    val meanUnitSize = _opSizeHistogram.getSnapshot.getMean
    val maxUnitSize = _opSizeHistogram.getSnapshot.getMax
    val unitSizes = if (!_withSizes.get) "" else
      s"\t${dName}_${unit}_size_min=$minUnitSize (${prettySizeString(minUnitSize)}), ${dName}_${unit}_size_mean=$meanUnitSize (${prettySizeString(meanUnitSize.toLong)}), ${dName}_${unit}_size_max=$maxUnitSize (${prettySizeString(maxUnitSize)}) \n"

    s"$opRates$opLatencies$unitRates$unitSizes"
  }

}
