/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.metrics

import com.codahale.metrics.{Counter, MetricRegistry}

import scala.collection.mutable

/**
  * An incrementing and decrementing counter metric.
 * @deprecated going way for OPEN SOURCE
  */
trait VitalsMetricsCounter extends Any  {

  /**
    * TODO
    */
  def inc(): Unit

}

object VitalsMetricsCounter {

  // make sure we do not get duplicate in various scenarios
  val map = new mutable.HashMap[String, VitalsMetricsCounter]

  def apply(metricType: MetricType, name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName): VitalsMetricsCounter = {
    synchronized {
      map get name match {
        case None =>
          val m = metricType match {
            case DevMetricType =>
              VitalsMetricsCounterContext(s"$metricsPackageName.$name")
            case OpsMetricType =>
              VitalsMetricsCounterContext(s"OPS.$name")
          }
          map += name -> m
          m

        case Some(g) => g
      }
    }
  }

}

private final case
class VitalsMetricsCounterContext(name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName)
  extends VitalsMetricsCounter {
  private lazy val counter: Counter = registry.counter(name)

  def getCount: Long = counter.getCount

  def inc(): Unit = counter.inc()

  def dec(): Unit = counter.dec()

  def inc(count: Long): Unit = counter.inc(count)

  def dec(count: Long): Unit = counter.dec(count)
}
