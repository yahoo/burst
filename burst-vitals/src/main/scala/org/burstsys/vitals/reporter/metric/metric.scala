/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

import com.codahale.metrics.Histogram
import com.codahale.metrics.SlidingTimeWindowArrayReservoir
import org.burstsys.vitals.logging._

import java.util.concurrent.TimeUnit

package object metric extends VitalsLogger {

  /**
   * @return a new histogram with a sliding time window equal to the default reporting period
   */
  final def defaultHistogram(): Histogram =
    new Histogram(new SlidingTimeWindowArrayReservoir(defaultReportPeriod.length, defaultReportPeriod.unit))

  /**
   * create a new histogram with a sliding time window
   * @param interval the length of the window. The default value is the current `reportPeriod.length`
   * @param unit     the time unit for the window. The default value is the current `reportPeriod.unit`
   * @return a new histogram
   */
  final def newHistogram(interval: Long = reportPeriod.length, unit: TimeUnit = reportPeriod.unit): Histogram =
    new Histogram(new SlidingTimeWindowArrayReservoir(interval, unit))

  /**
   * we keep a double input value scaled up as a long in the histogram
   * and then scale down when we read it as a final value. This allows us to store
   * floats into a long based histogram without losing too much precision (we only print
   * out two decimal points)
   */
  final val scaleFactor = 1e6

  final def externalToInternal(d: Double): Long = (d * scaleFactor).toLong

  final def internalToExternal(l: Long): Double = (l / scaleFactor)

  final def internalToExternal(d: Double): Double = (d / scaleFactor)

}
