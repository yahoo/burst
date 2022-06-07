/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.metrics

import java.util.concurrent.TimeUnit

import com.codahale.metrics.Timer.Context
import com.codahale.metrics.{MetricRegistry, Snapshot, Timer}

import scala.collection.mutable

/**
  * A timer metric which aggregates timing durations and provides duration statistics, plus
  * throughput statistics
 * @deprecated going way for OPEN SOURCE
  */
trait VitalsMetricsTimer extends Any {

  /**
    * Adds a recorded duration.
    *
    * @param duration the length of the duration
    * @param unit     the scale unit of the duration
    */
  def update(duration: Long, unit: TimeUnit): Unit

}

object VitalsMetricsTimer {

  // make sure we do not get duplicate in various scenarios
  val map = new mutable.HashMap[String, VitalsMetricsTimer]

  def apply(metricType: MetricType, name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName): VitalsMetricsTimer = {
    map get name match {
      case None =>
        val m = metricType match {
          case DevMetricType =>
            VitalsMetricsTimerContext(s"$metricsPackageName.$name")
          case OpsMetricType =>
            VitalsMetricsTimerContext(s"OPS.$name")
        }
        map += name -> m
        m

      case Some(g) => g
    }
  }

}

private final case
class VitalsMetricsTimerContext(name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName)
  extends VitalsMetricsTimer {
  private lazy val timer: Timer = registry.timer(name)

  def time(): Context = timer.time

  override
  def update(duration: Long, unit: TimeUnit): Unit = timer.update(duration, unit)

  def getCount: Long = timer.getCount

  def getFifteenMinuteRate: Double = timer.getFifteenMinuteRate

  def getFiveMinuteRate: Double = timer.getFiveMinuteRate

  def getMeanRate: Double = timer.getMeanRate

  def getOneMinuteRate: Double = timer.getOneMinuteRate

  def getSnapshot: Snapshot = timer.getSnapshot

}
