/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.metrics.Meter
import org.burstsys.vitals.logging._

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
}
