/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.cache

import io.opentelemetry.api.metrics.LongUpDownCounter
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterUnitOpMetric

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
  val _coldLoadMetric = VitalsReporterUnitOpMetric("cache_load_cold")

  private[this]
  val _warmLoadMetric = VitalsReporterUnitOpMetric("cache_load_warm")

  private[this]
  val _hotLoadMetric = VitalsReporterUnitOpMetric("cache_load_hot")

  private[this]
  val _evictMetric = VitalsReporterUnitOpMetric("cache_evict")

  private[this]
  val _flushMetric = VitalsReporterUnitOpMetric("cache_flush")

  private[this]
  val _eraseMetric = VitalsReporterUnitOpMetric("cache_erase")

  private[this]
  val _currentMemoryCounter: LongUpDownCounter = metric.meter.upDownCounterBuilder(s"cache_memory_counter")
    .setDescription(s"cache memory in use")
    .setUnit("bytes")
    .build()

  private[this]
  val _currentDiskCounter: LongUpDownCounter = metric.meter.upDownCounterBuilder(s"cache_disk_counter")
    .setDescription(s"disk memory in use")
    .setUnit("bytes")
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // listener
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def onSnapEvict(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    _evictMetric.recordOpWithTimeAndSize(elapsedNs, bytes)
    _currentMemoryCounter.add(-bytes)
  }

  override def onSnapFlush(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    _flushMetric.recordOpWithTimeAndSize(elapsedNs, bytes)
    _currentDiskCounter.add(-bytes)
  }

  override def onSnapErase(snap: FabricSnap): Unit = {
    _eraseMetric.recordOp()
  }

  override def onSnapColdLoad(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    _coldLoadMetric.recordOpWithTimeAndSize(elapsedNs, bytes)
    _currentMemoryCounter.add(bytes)
    _currentDiskCounter.add(bytes)
  }

  override def onSnapWarmLoad(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    _warmLoadMetric.recordOpWithTimeAndSize(elapsedNs, bytes)
    _currentMemoryCounter.add(bytes)
  }

  override def onSnapHotLoad(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    _hotLoadMetric.recordOpWithTimeAndSize(elapsedNs, bytes)
  }
}
