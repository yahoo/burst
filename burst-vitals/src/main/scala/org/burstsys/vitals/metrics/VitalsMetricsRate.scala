/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.metrics

import java.util.concurrent.atomic.LongAdder

import com.codahale.metrics.{Meter, MetricRegistry}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.collection.mutable
import scala.concurrent.Future

/**
  * A metric which is a time series of the rate of some ''amount'' per ''second''
  * e.g. '''bytes/seconds over time'''
 * @deprecated going way for OPEN SOURCE
  */
trait VitalsMetricsRate extends Any {

  /**
    * TODO
    *
    * @param amount
    * @param ns
    */
  def recordRateSample(amount: Long, ns: Long): Unit

}

object VitalsMetricsRate {

  // make sure we do not get duplicate in various scenarios
  val map = new mutable.HashMap[String, VitalsMetricsRate]

  def apply(metricType: MetricType, name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName): VitalsMetricsRate = {
    synchronized {
      map get name match {
        case None =>
          val m = metricType match {
            case DevMetricType =>
              VitalsMetricsRateContext(s"$metricsPackageName.$name")
            case OpsMetricType =>
              VitalsMetricsRateContext(s"OPS.$name")
          }
          map += name -> m
          m

        case Some(g) => g
      }
    }
  }

}

private final case
class VitalsMetricsRateContext(name: String)(implicit registry: MetricRegistry, metricsPackageName: VitalsMetricsPackageName)
  extends VitalsMetricsRate {

  //  private lazy val histogram = new Histogram(new ExponentiallyDecayingReservoir(100000, 0.015))
  private lazy val meter = new Meter()
  registry.register(name, meter)

  val amountTally = new LongAdder
  val timeTally = new LongAdder

  import scala.concurrent.ExecutionContext.Implicits.global

  Future {
    Thread.currentThread setName s"MetricsRate$name"
    try {
      while (true) {
        // record every second
        Thread.sleep(1000)
        val rate = synchronized {
          val amount = amountTally.sum()
          val time = timeTally.sum()
          amountTally.reset()
          timeTally.reset()
          val seconds = (time / 1 / 1e9).toLong
          if (seconds == 0) 0 else amount / seconds
        }
        meter.mark(rate)
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"rate recorder thread died!!!")
    }
  }

  // 12 145 758 E7

  override
  def recordRateSample(amount: Long, ns: Long): Unit = {
    synchronized {
      amountTally.add(amount)
      timeTally.add(ns)
    }
  }
}
