/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.metrics.logging

import com.codahale.metrics._
import org.burstsys.vitals.configuration.burstCellNameProperty
import org.burstsys.vitals.logging.VitalsLoggingHelper
import org.burstsys.vitals.metrics._

import java.util
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

/**
  * log to log4j in a way that is useful for splunk metrics collection/dashboards
 * @deprecated going way for OPEN SOURCE
  */
trait VitalsLogReporter extends ScheduledReporter

/**
  * A reporter class for logging metrics values to a log4j logger with an eye towards splunk compatibility
 * @deprecated going way for OPEN SOURCE
  */
object VitalsLogReporter {

  def apply(registry: MetricRegistry, period: Duration): VitalsLogReporter = {
    val reporter = VitalsLogReporterContext(registry)
    reporter.start(period.toMillis, TimeUnit.MILLISECONDS)
    reporter
  }
}

private final case
class VitalsLogReporterContext(
                                registry: MetricRegistry,
                                rateUnit: TimeUnit = TimeUnit.SECONDS,
                                durationUnit: TimeUnit = TimeUnit.MILLISECONDS
                              )
  extends ScheduledReporter(registry, "burst-log-reporter", MetricFilter.ALL, rateUnit, durationUnit)
    with VitalsLogReporter {

  lazy final val cellName = burstCellNameProperty.get
  override def
  report(
          gauges: util.SortedMap[String, Gauge[_]],
          counters: util.SortedMap[String, Counter],
          histograms: util.SortedMap[String, Histogram],
          meters: util.SortedMap[String, Meter],
          timers: util.SortedMap[String, Timer]
        ): Unit = {
    gauges.entrySet.stream.forEach(e => logGauge(e.getKey, e.getValue))
    counters.entrySet.stream.forEach(e => logCounter(e.getKey, e.getValue))
    histograms.entrySet.stream.forEach(e => logHistogram(e.getKey, e.getValue))
    meters.entrySet.stream.forEach(e => logMeter(e.getKey, e.getValue))
    timers.entrySet.stream.forEach(e => logTimer(e.getKey, e.getValue))
  }

  private
  def logTimer(name: String, timer: Timer): Unit = {
    val snapshot = timer.getSnapshot
    val nm = prefix(name)
    val msgs = Array(
      (s"$nm.count", s"${timer.getCount}"),
      (s"$nm.min", s"${convertDuration(snapshot.getMin.toDouble)}"),
      (s"$nm.max", s"${convertDuration(snapshot.getMax.toDouble)}"),
      (s"$nm.mean", s"${convertDuration(snapshot.getMean)}"),
      (s"$nm.stddev", s"${convertDuration(snapshot.getStdDev)}"),
      (s"$nm.median", s"${convertDuration(snapshot.getMedian)}"),
      (s"$nm.p75", s"${convertDuration(snapshot.get75thPercentile)}"),
      (s"$nm.p95", s"${convertDuration(snapshot.get95thPercentile)}"),
      (s"$nm.p98", s"${convertDuration(snapshot.get98thPercentile)}"),
      (s"$nm.p99", s"${convertDuration(snapshot.get99thPercentile)}"),
      (s"$nm.p999", s"${convertDuration(snapshot.get999thPercentile)}"),
      (s"$nm.mean_rate", s"${convertRate(timer.getMeanRate)}"),
      (s"$nm.m1", s"${convertRate(timer.getOneMinuteRate)}"),
      (s"$nm.m5", s"${convertRate(timer.getFiveMinuteRate)}"),
      (s"$nm.m15", s"${convertRate(timer.getFifteenMinuteRate)}")
    )
    // log info s"TIMER: ${msgs.map(t => s"${t._1}=${t._2}").mkString(", ")}"
    val now = Long.box(System.currentTimeMillis())
    msgs.foreach{t: (String, String) => VitalsLoggingHelper.info(log, s"metric_name=${t._1} metric_value=${t._2}, cell=$cellName", now, t._1, t._2, cellName)}
  }

  private
  def logMeter(name: String, meter: Meter): Unit = {
    val nm = prefix(name)
    val msgs = Array(
      (s"$nm.count",s"${meter.getCount}"),
      (s"$nm.mean_rate",s"${convertRate(meter.getMeanRate)}"),
      (s"$nm.m1",s"${convertRate(meter.getOneMinuteRate)}"),
      (s"$nm.m5",s"${convertRate(meter.getFiveMinuteRate)}"),
      (s"$nm.m15",s"${convertRate(meter.getFifteenMinuteRate)}")
    )
    val now = Long.box(System.currentTimeMillis())
    msgs.foreach{t: (String, String) => VitalsLoggingHelper.info(log, s"metric_name=${t._1} metric_value=${t._2}, cell=$cellName", now, t._1, t._2, cellName)}
  }

  private
  def logHistogram(name: String, histogram: Histogram): Unit = {
    val nm = prefix(name)
    val snapshot = histogram.getSnapshot
    val msgs = Array(
      (s"$nm.count",s"${histogram.getCount}"),
      (s"$nm.min",s"${snapshot.getMin}"),
      (s"$nm.max",s"${snapshot.getMax}"),
      (s"$nm.mean",s"${snapshot.getMean}"),
      (s"$nm.stddev",s"${snapshot.getStdDev}"),
      (s"$nm.median",s"${snapshot.getMedian}"),
      (s"$nm.p75",s"${snapshot.get75thPercentile}"),
      (s"$nm.p95",s"${snapshot.get95thPercentile}"),
      (s"$nm.p98",s"${snapshot.get98thPercentile}"),
      (s"$nm.p99",s"${snapshot.get99thPercentile}"),
      (s"$nm.p999",s"${snapshot.get999thPercentile}")
    )
    val now = Long.box(System.currentTimeMillis())
    msgs.foreach{t: (String, String) => VitalsLoggingHelper.info(log, s"${t._1}=${t._2}", now, t._1, t._2)}
  }

  private
  def logCounter(name: String, counter: Counter): Unit = {
    val nm = prefix(name)
    val msgs = Array(
      (s"$nm.count",s"${counter.getCount}")
    )
    val now = Long.box(System.currentTimeMillis())
    msgs.foreach{t: (String, String) => VitalsLoggingHelper.info(log, s"metric_name=${t._1} metric_value=${t._2}, cell=$cellName", now, t._1, t._2, cellName)}
  }

  private
  def logGauge(name: String, gauge: Gauge[_]): Unit = {
    val nm = prefix(name)
    val msgs = Array(
      (s"$nm.value",s"${gauge.getValue}")
    )
    val now = Long.box(System.currentTimeMillis())
    msgs.foreach{t: (String, String) => VitalsLoggingHelper.info(log, s"metric_name=${t._1} metric_value=${t._2}, cell=$cellName", now, t._1, t._2, cellName)}
  }

  override protected
  def getRateUnit: String = "events/" + super.getRateUnit

  private
  def prefix(components: String*) = MetricRegistry.name("", components: _*)

}
