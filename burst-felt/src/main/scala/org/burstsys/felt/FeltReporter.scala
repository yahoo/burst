/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import io.opentelemetry.api.metrics.LongUpDownCounter
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterUnitOpMetric

object FeltReporter extends VitalsReporter {

  final val dName: String = "felt"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _travelerGenerateMetric = VitalsReporterUnitOpMetric("felt_traveler_generate", "lines")

  private[this]
  val _travelerCompileMetric = VitalsReporterUnitOpMetric("felt_traveler_compile", "lines")

  private[this]
  val _travelerCacheHitMetric = VitalsReporterUnitOpMetric("felt_traveler_cache_hit")

  private[this]
  val _sweepCompileMetric = VitalsReporterUnitOpMetric("felt_sweep_compile", "lines")

  private[this]
  val _sweepCacheHitMetric = VitalsReporterUnitOpMetric("felt_sweep_cache_hit")

  private[this]
  val _sweepCacheCleanMetric = VitalsReporterUnitOpMetric("felt_sweep_cache_clean", "sweep")

  private[this]
  val _sweepCountCounter: LongUpDownCounter = metric.upDownCounter(s"felt_sweep_current_counter")
    .setDescription(s"current active sweeps")
    .setUnit("sweep")
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def recordTravelerGenerate(elapsedNs: Long): Unit = {
        _travelerGenerateMetric.recordOpWithTime(elapsedNs)
  }

  final
  def recordTravelerCacheHit(): Unit = {
    _travelerCacheHitMetric.recordOp()
  }

  final
  def recordTravelerCompile(elapsedNs: Long, source: String): Unit = {
    _travelerCompileMetric.recordOpWithTimeAndSize(elapsedNs, augmentString(source).linesIterator.size)
  }

  final
  def recordSweepCompile(elapsedNs: Long, source: String): Unit = {
    _sweepCompileMetric.recordOpWithTimeAndSize(elapsedNs, augmentString(source).linesIterator.size)
  }

  final
  def recordSweepCountIncrement(): Unit = {
    _sweepCountCounter.add(1)
  }

  final
  def recordSweepCountDecrement(): Unit = {
    _sweepCountCounter.add(-1)
  }

  final
  def recordSweepCacheHit(): Unit = {
    _sweepCacheHitMetric.recordOp()
  }

  final
  def recordSweepClean(): Unit = {
    _sweepCacheCleanMetric.recordOp()
  }
}
