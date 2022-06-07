/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

import java.util.concurrent.atomic.LongAdder

import org.burstsys.vitals.instrument.{prettyByteSizeString, prettySizeString}

import scala.language.postfixOps

/**
 * a report for byte quantum free/alloc parts. This designed for very high rate operations and as such
 * has no fancy histograms etc which cause measurable GC churn. This is nothing but simple [[LongAdder]]
 * * type primitives. Because of this all you get is simple counts and max values not rates.
 */
abstract class VitalsByteQuantReporter(prefix: String, quantum: String) extends VitalsReporter {

  final val dName: String = s"${prefix}_${quantum}"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _currentInUseTally = new LongAdder

  private[this]
  val _maxInUseTally = new LongAdder

  private[this]
  val _currentAllocTally = new LongAdder

  private[this]
  val _maxAllocTally = new LongAdder

  private[this]
  val _currentByteTally = new LongAdder

  private[this]
  val _maxByteTally = new LongAdder

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

  /**
   * record a block release
   *
   * @param bytes
   */
  final
  def alloc(bytes: Long): Unit = {
    newSample()
    _currentAllocTally.increment()
    _currentByteTally.add(bytes)
    if (_currentAllocTally.sum > _maxAllocTally.sum) {
      _maxAllocTally.increment()
    }
    if (_currentByteTally.sum > _maxByteTally.sum) {
      _maxByteTally.add(bytes)
    }
  }

  /**
   * record a block release
   *
   * @param bytes
   */
  final
  def free(bytes: Long): Unit = {
    newSample()
    _currentAllocTally.decrement()
    _currentByteTally.add(-bytes)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    val currentInUseTally = s"\t${dName}_current_in_use=${_currentInUseTally.longValue()} (${prettySizeString(_currentInUseTally.longValue())})\n"
    val maxUnUseTally = s"\t${dName}_max_in_use=${_currentInUseTally.longValue()} (${prettySizeString(_currentInUseTally.longValue())})\n"
    val currentAllocTally = s"\t${dName}_current_alloc=${_currentAllocTally.longValue()} (${prettySizeString(_currentAllocTally.longValue())})\n"
    val maxAllocTally = s"\t${dName}_max_alloc=${_maxAllocTally.longValue()} (${prettySizeString(_maxAllocTally.longValue())})\n"
    val currentByteTally = s"\t${dName}_current_bytes=${_currentByteTally.longValue()} (${prettyByteSizeString(_currentByteTally.longValue())})\n"
    val maxByteTally = s"\t${dName}_max_bytes=${_maxByteTally.longValue()} (${prettyByteSizeString(_maxByteTally.longValue())})\n"
    s"$currentInUseTally$maxUnUseTally$currentAllocTally$maxAllocTally$currentByteTally$maxByteTally"
  }

}
