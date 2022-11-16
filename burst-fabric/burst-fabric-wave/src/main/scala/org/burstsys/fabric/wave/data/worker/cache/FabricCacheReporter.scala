/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.cache

import java.util.concurrent.atomic.LongAdder

import org.burstsys.fabric.wave.data.model.limits
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.vitals.reporter.instrument.prettyByteSizeString
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterFixedValueMetric, VitalsReporterPercentValueMetric}

import scala.language.postfixOps

/**
 * a generic report for all cache metrics on worker
 */
private[fabric]
object FabricCacheReporter extends VitalsReporter with FabricSnapCacheListener {

  final val dName: String = "fab-cache"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _coldLoadMetric = VitalsReporterByteOpMetric("cache_load_cold")
  this += _coldLoadMetric

  private[this]
  val _warmLoadMetric = VitalsReporterByteOpMetric("cache_load_warm")
  this += _warmLoadMetric

  private[this]
  val _hotLoadMetric = VitalsReporterByteOpMetric("cache_load_hot")
  this += _hotLoadMetric

  private[this]
  val _evictMetric = VitalsReporterByteOpMetric("cache_evict")
  this += _evictMetric

  private[this]
  val _flushMetric = VitalsReporterByteOpMetric("cache_flush")
  this += _flushMetric

  private[this]
  val _eraseMetric = VitalsReporterByteOpMetric("cache_erase")
  this += _eraseMetric

  private[this]
  val _memoryUsageMetric = VitalsReporterPercentValueMetric("cache_usage_memory")
  this += _memoryUsageMetric

  private[this]
  val _diskUsageMetric = VitalsReporterPercentValueMetric("cache_usage_disk")
  this += _diskUsageMetric

  private[this]
  val _diskTallyMetric = VitalsReporterFixedValueMetric("cache_tally_disk")
  this += _diskTallyMetric

  private[this]
  val _memoryTallyMetric = VitalsReporterFixedValueMetric("cache_tally_memory")
  this += _memoryTallyMetric

  private[this]
  val _currentMemoryTally = new LongAdder

  private[this]
  val _currentDiskTally = new LongAdder

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    _memoryUsageMetric.record(limits.memoryPercentUsed)
    _diskUsageMetric.record(limits.diskPercentUsed)
    _diskTallyMetric.record(_currentDiskTally.sum())
    _memoryTallyMetric.record(_currentMemoryTally.sum())
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // listener
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def onSnapEvict(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    newSample()
    _evictMetric.recordOpWithTimeAndSize(elapsedNs, bytes)
    _currentMemoryTally.add(-bytes)
  }

  override def onSnapFlush(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    newSample()
    _flushMetric.recordOpWithTimeAndSize(elapsedNs, bytes)
    _currentDiskTally.add(-bytes)
  }

  override def onSnapErase(snap: FabricSnap): Unit = {
    newSample()
    _eraseMetric.recordOp()
  }

  override def onSnapColdLoad(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    newSample()
    _coldLoadMetric.recordOpWithTimeAndSize(elapsedNs, bytes)
    _currentMemoryTally.add(bytes)
    _currentDiskTally.add(bytes)
  }

  override def onSnapWarmLoad(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    newSample()
    _warmLoadMetric.recordOpWithTimeAndSize(elapsedNs, bytes)
    _currentMemoryTally.add(bytes)
  }

  override def onSnapHotLoad(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    newSample()
    _hotLoadMetric.recordOpWithTimeAndSize(elapsedNs, bytes)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def report: String = {
    if (nullData) return ""
    val currentTally = s"\tcache_current_memory=${_currentMemoryTally.sum} (${prettyByteSizeString(_currentMemoryTally.sum)}), cache_current_disk=${_currentDiskTally.sum} (${prettyByteSizeString(_currentDiskTally.sum)})\n"
    val tallyMetric = s"\n\t${_memoryTallyMetric.report}${_diskTallyMetric.report}"
    s"$currentTally$tallyMetric${_coldLoadMetric.report}${_warmLoadMetric.report}${_hotLoadMetric.report}${_evictMetric.report}${_flushMetric.report}${_eraseMetric.report}${_memoryUsageMetric.report}${_diskUsageMetric.report}"
  }

}
