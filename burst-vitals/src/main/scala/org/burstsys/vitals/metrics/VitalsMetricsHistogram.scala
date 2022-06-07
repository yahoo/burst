/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.metrics

import com.codahale.metrics.{Histogram, MetricRegistry, SlidingWindowReservoir}

import scala.collection.mutable

/**
  * A metric which calculates the distribution of a value.
 * @deprecated going way for OPEN SOURCE
  */
trait VitalsMetricsSlidingWindowHistogram extends Any {

  /**
    * TODO
    *
    * @param duration
    */
  def mark(duration: Long): Unit

}

object VitalsMetricsSlidingWindowHistogram {

  // make sure we do not get duplicate in various scenarios
  val map = new mutable.HashMap[String, VitalsMetricsSlidingWindowHistogram]

  def apply(metricType: MetricType, name: String, samples: Int)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName): VitalsMetricsSlidingWindowHistogram = {
    synchronized {
      map get name match {
        case None =>
          val m = metricType match {
            case DevMetricType =>
              VitalsMetricsSlidingWindowHistogramContext(s"$metricsPackageName.$name", samples)
            case OpsMetricType =>
              VitalsMetricsSlidingWindowHistogramContext(s"OPS.$name", samples)
          }
          map += name -> m
          m

        case Some(g) => g
      }
    }
  }

}

private final case
class VitalsMetricsSlidingWindowHistogramContext(name: String, samples: Int)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName)
  extends VitalsMetricsSlidingWindowHistogram {

  private lazy val histogram = new Histogram(new SlidingWindowReservoir(samples))
  registry.register(name, histogram)

  def mark(value: Long): Unit = {
    histogram.update(value)
  }
}
