/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.metrics

import com.codahale.metrics.{Meter, MetricRegistry}

import scala.collection.mutable

/**
  * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
  * exponentially-weighted moving average throughputs.
 * @deprecated going way for OPEN SOURCE
  */
trait VitalsMetricsMeter extends Any {
  /**
    * TODO
    */
  def count(): Unit

  /**
    * TODO
    */
  def count(items: Long): Unit

}

/**
  * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
  * exponentially-weighted moving average throughputs.
  */
object VitalsMetricsMeter {

  // make sure we do not get duplicate in various scenarios
  val map = new mutable.HashMap[String, VitalsMetricsMeter]

  /**
    * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
    * exponentially-weighted moving average throughputs.
    *
    * @param metricType
    * @param name
    * @param registry
    * @param metricsPackageName
    * @return
    */
  def apply(metricType: MetricType, name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName): VitalsMetricsMeter = {
    synchronized {
      map get name match {
        case None =>
          val m = metricType match {
            case DevMetricType =>
              VitalsMetricsMeterContext(s"$metricsPackageName.$name")
            case OpsMetricType =>
              VitalsMetricsMeterContext(s"OPS.$name")
          }
          map += name -> m
          m

        case Some(g) => g
      }
    }
  }

}

/**
  * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
  * exponentially-weighted moving average throughputs.
  *
  * @param name
  * @param registry
  * @param metricsPackageName
  */
private final case
class VitalsMetricsMeterContext(name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName)
  extends VitalsMetricsMeter {
  private val meter = new Meter()
  registry.register(name, meter)

  /**
    * Mark the occurrence of multiple events.
    *
    * @param events
    */
  def count(events: Long): Unit = {
    meter.mark(events)
  }

  /**
    * Mark the occurrence of a single event.
    */
  def count(): Unit = {
    meter.mark()
  }
}

