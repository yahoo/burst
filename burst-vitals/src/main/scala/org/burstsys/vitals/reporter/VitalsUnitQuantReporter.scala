/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

import io.opentelemetry.api.metrics.LongUpDownCounter

import java.util.concurrent.atomic.LongAdder
import scala.language.postfixOps

/**
 * a report for quantum grab and release parts. This designed for very high rate operations and as such
 * * has no fancy histograms etc which cause measurable GC churn. This is nothing but simple [[LongAdder]]
 * * type primitives. Because of this all you get is simple counts and max values not rates.
 */
abstract class VitalsUnitQuantReporter(prefix: String, quantum: String) extends VitalsReporter {

  final val dName: String = s"${prefix}_$quantum"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////


  private val _currentInUseCounter: LongUpDownCounter = metric.meter.upDownCounterBuilder(s"${dName}_counter")
    .setDescription(s"$prefix $quantum in use")
    .setUnit(quantum)
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * record a block allocation
   *
   */
  def grab(): Unit = {
    _currentInUseCounter.add(1)
  }

  /**
   * record a block release
   *
   */
  def release(): Unit = {
    _currentInUseCounter.add(-1)
  }

}
