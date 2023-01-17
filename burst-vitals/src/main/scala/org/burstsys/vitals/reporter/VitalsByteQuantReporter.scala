/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

import io.opentelemetry.api.metrics.LongUpDownCounter

import scala.language.postfixOps

/**
 * a report for byte quantum free/alloc parts. Built on the block grab/release it tracks allocations within the larger
 * block
 */
abstract class VitalsByteQuantReporter(prefix: String, quantum: String) extends VitalsUnitQuantReporter(prefix, quantum) {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////


  private val _currentAllocCounter: LongUpDownCounter = metric.meter.upDownCounterBuilder(s"${dName}_alloc_counter")
    .setDescription(s"$prefix $quantum blocks alloc'ed")
    .setUnit(quantum)
    .build()


  private val _currentByteCounter: LongUpDownCounter = metric.meter.upDownCounterBuilder(s"${dName}_bytes_counter")
    .setDescription(s"$prefix $quantum bytes alloc'ed")
    .setUnit(quantum)
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * record a block alloc
   */
  final
  def alloc(bytes: Long): Unit = {
    _currentAllocCounter.add(-1)
    _currentByteCounter.add(bytes)
  }

  /**
   * record a block release
   */
  final
  def free(bytes: Long): Unit = {
    _currentAllocCounter.add(-1)
    _currentByteCounter.add(-bytes)
  }
}
