/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.common.{Attributes, AttributesBuilder}
import io.opentelemetry.api.metrics.Meter
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net

package object metric extends VitalsLogger {
  lazy val meter: Meter = GlobalOpenTelemetry.meterBuilder("org.burstsys").build()

  /**
   * we keep a double input value scaled up as a long in the histogram
   * and then scale down when we read it as a final value. This allows us to store
   * floats into a long based histogram without losing too much precision (we only print
   * out two decimal points)
   */
  private final val scaleFactor = 1e6

  final def externalToInternal(d: Double): Long = (d * scaleFactor).toLong

  private [metric] lazy val hostName = System.getenv("HOSTNAME")
  private [metric] lazy val podName = System.getenv("POD_NAME") // Set by in env by k8s - see start.sh
  private [metric] lazy val deployName = System.getenv("DEPLOY_ENV") // Set by in env by k8s - see start.sh
  lazy val metricAttributes: Attributes = {
    implicit val b: AttributesBuilder = Attributes.builder()
    putValidMetricValue("environmentHostname", hostName)
    putValidMetricValue("podName", podName)
    putValidMetricValue("deployName", deployName)
    putValidMetricValue("localHostname", net.getLocalHostName)
    putValidMetricValue("localAddress", net.getLocalHostAddress)
    putValidMetricValue("publicHostname", net.getPublicHostName)
    putValidMetricValue("publicAddress", net.getPublicHostAddress)
    b.build()
  }

  private def putValidMetricValue(k: String, v: String)(implicit attributes: AttributesBuilder): Boolean = {
    if (v == null) false
    else if (v.isEmpty) false
    else if (v == "NaN") false
    else {
      log info s"Adding metric attribute $k=$v"
      attributes.put(k, v)
      true
    }
  }
}
