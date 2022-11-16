/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

import java.util.concurrent.atomic.LongAdder

import org.burstsys.vitals.reporter.instrument.prettySizeString

import scala.language.postfixOps

/**
 * a report for byte quantum free/alloc parts. This designed for very high rate operations and as such
 * * has no fancy histograms etc which cause measurable GC churn. This is nothing but simple [[LongAdder]]
 * * type primitives. Because of this all you get is simple counts and max values not rates.
 */
abstract class VitalsUnitQuantReporter(prefix: String, quantum: String) extends VitalsReporter {

  final val dName: String = s"${prefix}_${quantum}"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _maxInUseTally = new LongAdder

  private[this]
  val _currentInUseTally = new LongAdder

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def sample(sampleMs: Long): Unit = {
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * record a block allocation
   *
   */
  final
  def grab(): Unit = {
    newSample()
    _currentInUseTally.increment()
    if (_currentInUseTally.sum > _maxInUseTally.sum) {
      _maxInUseTally.increment()
    }
  }

  /**
   * record a block release
   *
   */
  final
  def release(): Unit = {
    newSample()
    _currentInUseTally.decrement()
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    val maxInUseTally = s"\t${dName}_current_in_use=${_maxInUseTally.longValue()} (${prettySizeString(_maxInUseTally.longValue())})\n"
    val currentInUseTally = s"\t${dName}_max_in_use=${_currentInUseTally.longValue()} (${prettySizeString(_currentInUseTally.longValue())})\n"
    s"$maxInUseTally$currentInUseTally"
  }

}
