/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import java.util.concurrent.atomic.AtomicBoolean

import com.codahale.metrics.{ExponentiallyDecayingReservoir, Histogram, Meter}
import org.burstsys.vitals.instrument._

/**
 * measures all aspects of a recurring operation that takes a certain amount of time and has a varying byte size.
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
 * <h2>`{name}_byte_rate`</h2>
 * if `withSizes`
 * <ol>
 * <li><b>`{name}_byte_rate_1m`:</b> ''1 minute byte/s rate''</li>
 * <li><b>`{name}_byte_rate_5m`:</b> ''5 minute byte/s rate''</li>
 * <li><b>`{name}_byte_rate_15m`:</b> ''15 minute byte/s rate''</li>
 * </ol>
 * <h2>`{name}_byte_size`</h2>
 * if `withSizes` (this is an exponential decay histogram)
 * <ol>
 * <li><b>`{name}_byte_size_min`:</b> ''min byte size''</li>
 * <li><b>`{name}_byte_size_mean`:</b> ''mean byte size''</li>
 * <li><b>`{name}_byte_size_max`:</b> ''max byte size''</li>
 * </ol>
 */
trait VitalsReporterByteOpMetric extends VitalsReporterMetric {

  def recordOp(): Unit

  def recordOps(ops: Long): Unit

  def recordOpWithTime(ns: Long): Unit

  def recordOpsWithTime(ops: Long, ns: Long): Unit

  def recordOpWithSize(bytes: Long): Unit

  def recordOpWithTimeAndSize(ns: Long, bytes: Long): Unit

  def recordOpsWithTimeAndSize(ops: Long, ns: Long, bytes: Long): Unit

}

object VitalsReporterByteOpMetric {

  def apply(name: String): VitalsReporterByteOpMetric =
    VitalsReporterByteOpMetricContext(name: String)

}

private final case
class VitalsReporterByteOpMetricContext(name: String) extends VitalsReporterMetricContext(name) with VitalsReporterByteOpMetric {

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
  def recordOpWithSize(bytes: Long): Unit = {
    newSample()
    _withSizes.set(true)
    _opMeter.mark()
    _sizeMeter.mark(bytes)
    _opSizeHistogram.update(bytes)
  }

  override
  def recordOpWithTimeAndSize(ns: Long, bytes: Long): Unit = {
    newSample()
    _withTimes.set(true)
    _withSizes.set(true)
    _opMeter.mark()
    _opTimeHistogram.update(ns)
    _opSizeHistogram.update(bytes)
    _sizeMeter.mark(bytes)
  }

  override
  def recordOpsWithTimeAndSize(ops: Long, ns: Long, bytes: Long): Unit = {
    newSample()
    _withTimes.set(true)
    _withSizes.set(true)
    _opMeter.mark(ops)
    _opTimeHistogram.update(ns)
    _opSizeHistogram.update(bytes)
    _sizeMeter.mark(bytes)
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

    // {name}_byte_rate
    val byteRate1m = _sizeMeter.getOneMinuteRate
    val byteRate5m = _sizeMeter.getFiveMinuteRate
    val byteRate15m = _sizeMeter.getFifteenMinuteRate
    val byteRates = if (!_withSizes.get) "" else
      s"\t${dName}_byte_rate_1m=$byteRate1m (${prettyByteSizeString(byteRate1m)}/s), ${dName}_byte_rate_5m=$byteRate5m (${prettyByteSizeString(byteRate5m.toLong)}/s), ${dName}_byte_rate_15m=$byteRate15m (${prettyByteSizeString(byteRate15m)}/s) \n"

    // {name}_byte_size
    val minByteSize = _opSizeHistogram.getSnapshot.getMin
    val meanByteSize = _opSizeHistogram.getSnapshot.getMean
    val maxByteSize = _opSizeHistogram.getSnapshot.getMax
    val byteSizes = if (!_withSizes.get) "" else
      s"\t${dName}_byte_size_min=$minByteSize (${prettyByteSizeString(minByteSize)}), ${dName}_byte_size_mean=$meanByteSize (${prettyByteSizeString(meanByteSize.toLong)}), ${dName}_byte_size_max=$maxByteSize (${prettyByteSizeString(maxByteSize)}) \n"

    s"$opRates$opLatencies$byteRates$byteSizes"
  }

}
