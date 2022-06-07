/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.metrics

import java.util.concurrent.atomic.LongAdder

import com.codahale.metrics.{Gauge, MetricRegistry}

import org.burstsys.vitals.logging._

import scala.collection.mutable

/**
  * A gauge metric is an instantaneous reading of a particular value.
 * @deprecated going way for OPEN SOURCE
  */
trait VitalsMetricsGauge extends Any {

  def increment(): Unit

  def decrement(): Unit

  def add(value: Long): Unit

  def subtract(value: Long): Unit

}

/**
  * instantaneous reading of a particular value.
  */
object VitalsMetricsGauge {

  // make sure we do not get duplicate in various scenarios
  val map = new mutable.HashMap[String, VitalsMetricsGauge]

  /**
    * instantaneous reading of a particular value.
    *
    * @param metricType
    * @param name
    * @param registry
    * @param metricsPackageName
    * @return
    */
  def apply(metricType: MetricType, name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName): VitalsMetricsGauge = {
    synchronized {
      map get name match {
        case None =>
          val m = metricType match {
            case DevMetricType =>
              VitalsMetricsGaugeContext(s"$metricsPackageName.$name")
            case OpsMetricType =>
              VitalsMetricsGaugeContext(s"OPS.$name")
          }
          map += name -> m
          m

        case Some(g) => g
      }
    }
  }

}

/**
  * A gauge metric is an instantaneous reading of a particular value
  *
  * @param name
  * @param registry
  * @param metricsPackageName
  */
private case
class VitalsMetricsGaugeContext(name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName)
  extends VitalsMetricsGauge {
  final val count = new LongAdder()
  private val gauge = new Gauge[Long]() {
    override def getValue: Long = count.sum()
  }
  registry.register(name, gauge)

  def increment(): Unit = count.increment()

  def decrement(): Unit = count.decrement()

  def add(value: Long): Unit = count.add(value)

  def subtract(value: Long): Unit = count.add(-value)
}

abstract class VitalsMetricsActiveDevGauge[N](name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName)
  extends VitalsMetricsActiveGauge[N](s"$metricsPackageName.$name")

abstract class VitalsMetricsActiveOpsGauge[N](name: String)(implicit registry: MetricRegistry)
  extends VitalsMetricsActiveGauge[N](s"OPS.$name")

abstract class VitalsMetricsActiveGauge[N](name: String)(implicit registry: MetricRegistry) {

  def value: N

  val gauge: Gauge[N] = new Gauge[N] {
    override def getValue: N = value
  }

  log debug burstStdMsg(s"registering gauge $name")
  registry.register(name, gauge)
}
