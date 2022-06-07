/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.metrics

import com.codahale.metrics.MetricRegistry
import org.burstsys.vitals.configuration.{burstVitalsMetricsHttpPeriodDuration, burstVitalsMetricsHttpUrlProperty}
import org.burstsys.vitals.metrics.logging.{VitalsHttpReporter, VitalsLogReporter}

import scala.language.postfixOps

/**
  * The general idea for this class moving forward is that all
  * metrics config must be in ENV variables so that we can guarantee the interface for
  * getting things like InfluxDb host addresses is something that can be guaranteed to be
  * available at any point in the VM lifecycle including in spark worker contexts and
  * that the interface for getting it is identical. Otherwise its too confusing.
 * @deprecated going way for OPEN SOURCE
  */
object VitalsMetricsRegistry {

  private var disabled = false
  private var _logReporter: Option[VitalsLogReporter] = None
  private var _httpReporter: Option[VitalsHttpReporter] = None

  def disable(): Unit = {
    VitalsMetricsRegistry.synchronized {
      disabled = true
      _logReporter.foreach(_.stop())
      _httpReporter.foreach(_.stop())
    }
  }

  private var _registry: MetricRegistry = _

  def registry: MetricRegistry = {
    VitalsMetricsRegistry.synchronized {
      _registry match {
        case null =>
          _registry = new MetricRegistry
          if (!disabled) {
            val url = burstVitalsMetricsHttpUrlProperty.get
            if (url.isDefined && !url.get.trim.isEmpty) {
              _httpReporter = Option(VitalsHttpReporter(_registry, burstVitalsMetricsHttpPeriodDuration))
            }
          }
          _registry
        case r => r
      }
    }
  }

}
