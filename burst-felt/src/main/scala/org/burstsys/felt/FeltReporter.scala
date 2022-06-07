/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import org.burstsys.vitals.instrument.prettyFixedNumber
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterUnitOpMetric}

import java.util.concurrent.atomic.LongAdder

object FeltReporter extends VitalsReporter {

  final val dName: String = "felt"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _travelerGenerateMetric = VitalsReporterUnitOpMetric("felt_traveler_generate", "lines")
  this += _travelerGenerateMetric

  private[this]
  val _travelerCompileMetric = VitalsReporterUnitOpMetric("felt_traveler_compile", "lines")
  this += _travelerCompileMetric

  private[this]
  val _travelerCacheHitMetric = VitalsReporterByteOpMetric("felt_traveler_cache_hit")
  this += _travelerCacheHitMetric

  private[this]
  val _sweepCompileMetric = VitalsReporterUnitOpMetric("felt_sweep_compile", "lines")
  this += _sweepCompileMetric

  private[this]
  val _sweepCacheHitMetric = VitalsReporterByteOpMetric("felt_sweep_cache_hit")
  this += _sweepCacheHitMetric

  private[this]
  val _sweepCacheCleanMetric = VitalsReporterUnitOpMetric("felt_sweep_cache_clean", "sweep")
  this += _sweepCacheCleanMetric

  private[this]
  val _sweepCountTally = new LongAdder()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def recordTravelerGenerate(elapsedNs: Long): Unit = {
    newSample()
    _travelerGenerateMetric.recordOpWithTime(elapsedNs)
  }

  final
  def recordTravelerCacheHit(): Unit = {
    newSample()
    _travelerCacheHitMetric.recordOp()
  }

  final
  def recordTravelerCompile(elapsedNs: Long, source: String): Unit = {
    newSample()
    _travelerCompileMetric.recordOpWithTimeAndSize(elapsedNs, augmentString(source).lines.size)
  }

  final
  def recordSweepCompile(elapsedNs: Long, source: String): Unit = {
    newSample()
    _sweepCompileMetric.recordOpWithTimeAndSize(elapsedNs, augmentString(source).lines.size)
  }

  final
  def recordSweepCountIncrement(): Unit = {
    newSample()
    _sweepCountTally.increment()
  }

  final
  def recordSweepCountDecrement(): Unit = {
    newSample()
    _sweepCountTally.decrement()
  }

  final
  def recordSweepCacheHit(): Unit = {
    newSample()
    _sweepCacheHitMetric.recordOp()
  }

  final
  def recordSweepClean(): Unit = {
    newSample()
    _sweepCacheCleanMetric.recordOp()
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    val sweepCount = s"\tfelt_sweep_count=${_sweepCountTally.longValue} (${prettyFixedNumber(_sweepCountTally.longValue)}),\n"
    s"$sweepCount${_sweepCacheCleanMetric.report}${_travelerGenerateMetric.report}${_travelerCompileMetric.report}${_travelerCacheHitMetric.report}${_sweepCompileMetric.report}${_sweepCacheHitMetric.report}"
  }

}
