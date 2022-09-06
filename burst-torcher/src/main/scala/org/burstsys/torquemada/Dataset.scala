/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import java.util.concurrent.{ConcurrentHashMap, TimeUnit}
import com.codahale.metrics.{Counter, Timer}
import org.burstsys.fabric.execution.model.result.group.FabricResultGroupMetrics

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

object FunctionConverter {
  implicit def scalaFunctionToJava[From, To](function: From => To): java.util.function.Function[From, To] = {
    new java.util.function.Function[From, To] {
      override def apply(input: From): To = function(input)
    }
  }
}

final case class Dataset
(
  otherId: String,
  domainId: Long,
  viewId: Long,
  motif: String,
  queries: List[String],
  flush: Char,
  info:String = "",
  viewTemporary: Boolean = true,
  domainTemporary: Boolean = false
) {
  override def toString: String = s"${if (otherId != null && otherId.trim.length > 0) s"${otherId}_" else ""}${domainId}_$viewId"
}

class DatasetStatisticsTable() {
  val datasetStatistics: java.util.concurrent.ConcurrentHashMap[DatasetKey, DatasetState] =
    new ConcurrentHashMap[DatasetKey, DatasetState]()
  val summaryStatistics: DatasetStatistics = DatasetStatistics()

  private def getStateForDataset(dataset: Dataset): DatasetState = {
    val key = DatasetKey(dataset.domainId, dataset.viewId)
    // make sure the map has an entry

    // increment the query failure count
    import FunctionConverter._
    datasetStatistics.computeIfAbsent(key, { _: DatasetKey => DatasetState(dataset.info)}) match {
      case null =>
        throw new RuntimeException("unexpected lack of element")
      case  state =>
        // initialize the coldload a to current time to record the first attemps.
        state.coldLoadAt.set(System.currentTimeMillis)
        state
    }
  }

  def recordStatistics(dataset: Dataset, Gid: String, metrics: FabricResultGroupMetrics): DatasetStatistics = {
    val state = getStateForDataset(dataset)
    if (metrics.generationMetrics.generationKey.generationClock !=
      state.generationClock.getAndSet(metrics.generationMetrics.generationKey.generationClock)) {
      state.statistics.generationChange.inc()
      summaryStatistics.generationChange.inc()
    }
    if (metrics.generationMetrics.coldLoadAt != state.coldLoadAt.getAndSet(metrics.generationMetrics.coldLoadAt)) {
      state.statistics.coldLoadTimer.update(metrics.generationMetrics.coldLoadTook, TimeUnit.MILLISECONDS)
      state.statistics.itemCount.inc(metrics.generationMetrics.itemCount)
      state.statistics.byteCount.inc(metrics.generationMetrics.byteCount)
      summaryStatistics.coldLoadTimer.update(metrics.generationMetrics.coldLoadTook, TimeUnit.MILLISECONDS)
      summaryStatistics.itemCount.inc(metrics.generationMetrics.itemCount)
      summaryStatistics.byteCount.inc(metrics.generationMetrics.byteCount)
    }
    if (metrics.generationMetrics.warmLoadAt != state.warmLoadAt.getAndSet(metrics.generationMetrics.warmLoadAt)) {
      state.statistics.warmLoadTimer.update(metrics.generationMetrics.warmLoadTook, TimeUnit.MILLISECONDS)
      summaryStatistics.warmLoadTimer.update(metrics.generationMetrics.warmLoadTook, TimeUnit.MILLISECONDS)
    }

    metrics.executionMetrics.overflowed
    state.statistics.rowCount.inc(metrics.executionMetrics.rowCount)
    state.statistics.scanTime.update(metrics.executionMetrics.scanTime, TimeUnit.NANOSECONDS)
    state.statistics.limitCount.inc(metrics.executionMetrics.limited)
    state.statistics.overflowCount.inc(metrics.executionMetrics.overflowed)
    state.successGid.set(Gid)
    summaryStatistics.rowCount.inc(metrics.executionMetrics.rowCount)
    summaryStatistics.scanTime.update(metrics.executionMetrics.scanTime, TimeUnit.NANOSECONDS)
    summaryStatistics.limitCount.inc(metrics.executionMetrics.limited)
    summaryStatistics.overflowCount.inc(metrics.executionMetrics.overflowed)
    state.statistics
  }

  def startQueryTimer(dataset: Dataset, first: Boolean = false): DatasetTimer = {
    val state = getStateForDataset(dataset)
    if (first)
      DatasetTimer(state.statistics.firstQueryTimer.time, summaryStatistics.firstQueryTimer.time)
    else
      DatasetTimer(state.statistics.queryTimer.time, summaryStatistics.queryTimer.time)
  }

  def recordQueryFailure(dataset: Dataset, Gid: String, e: Throwable, first: Boolean = false): Unit = {
    val state = getStateForDataset(dataset)
    if (first) {
      state.statistics.firstQueryFailures.inc()
      summaryStatistics.firstQueryFailures.inc()
    } else {
      state.statistics.queryFailures.inc()
      summaryStatistics.queryFailures.inc()
    }
    state.successGid.set(Gid)
  }

  def recordQueryNoDataFailure(dataset: Dataset, Gid: String, first: Boolean = false): Unit = {
    val state = getStateForDataset(dataset)
    if (first) {
      state.statistics.firstQueryNoDataFailures.inc()
      summaryStatistics.firstQueryNoDataFailures.inc()
    } else {
      state.statistics.queryNoDataFailures.inc()
      summaryStatistics.queryNoDataFailures.inc()
    }
    state.successGid.set(Gid)
  }

  def recordObjectCount(dataset: Dataset, count: Long): Unit = {
    val state = getStateForDataset(dataset)
    state.statistics.objectCount.inc(count)
    summaryStatistics.objectCount.inc(count)
  }

  def tableStatisticsLabels: Array[String] = summaryStatistics.allStatsLabels
  def tableStatistics(labels: Boolean = true): String = {
    (for ((k, v) <- datasetStatistics.asScala) yield {
      s"${k.toString}: ${v.statistics.allStats(labels)}"
    }).mkString("\n")
  }
  def tableStatistics: Map[String, Array[Double]] = {
    (for ((k, v) <- datasetStatistics.asScala) yield {
      (k.toString, v.statistics.allStats)
    }).toMap
  }
}

final case class DatasetTimer(item: Timer.Context, summary: Timer.Context) {
  def stop(): Long = {
    item.stop()
    summary.stop()
  }
}

// Per Dataset Statistics
final case class DatasetKey(domainId: Long, viewId: Long)
final case class DatasetStatistics() {
  // agent cache stats
  val coldLoadTimer = new Timer()
  val warmLoadTimer = new Timer()
  val byteCount = new Counter()
  val itemCount = new Counter()

  // query metrics
  val scanTime = new Timer()
  val rowCount = new Counter()

  // observed stats
  val firstQueryTimer = new Timer()
  val firstQueryFailures = new Counter()
  val firstQueryNoDataFailures = new Counter()
  val queryTimer = new Timer()
  val queryFailures = new Counter()
  val queryNoDataFailures = new Counter()
  val objectCount = new Counter()
  val overflowCount = new Counter()
  val limitCount = new Counter()

  //  generationClock independent stats
  val generationChange = new Counter()

  def cacheStatLabels: Array[String] = Array("itemCount", "byteCount", "coldLoadTime95", "warmLoadTime95")
  def cacheStats: Array[Double] = Array(itemCount.getCount.toDouble, byteCount.getCount.toDouble, coldLoadTimer.getSnapshot.get95thPercentile(), warmLoadTimer.getSnapshot.get95thPercentile())
  def cacheStats(labels: Boolean = false): String = {
    if (labels)
      cacheStatLabels.zip(cacheStats).map{x => s"${x._1}=${x._2}"}.mkString(",")
     else
      cacheStats.mkString(",")
  }

  def queryStatLabels: Array[String] = Array("firstQueryTime95", "firstQueryCount", "firstNoDataFailureCount", "firstFailureCount", "queryTime95",
    "queryCount", "queryNoDataFailureCount", "queryFailureCount", "scanTime95", "rowCount", "objectCount", "generationChange")
  def queryStats: Array[Double] = Array(firstQueryTimer.getSnapshot.get95thPercentile(), firstQueryTimer.getCount.toDouble,
    firstQueryNoDataFailures.getCount.toDouble, firstQueryFailures.getCount.toDouble,
    queryTimer.getSnapshot.get95thPercentile(), queryTimer.getCount.toDouble,
    queryNoDataFailures.getCount.toDouble, queryFailures.getCount.toDouble,
    scanTime.getSnapshot.get95thPercentile(), rowCount.getCount.toDouble, objectCount.getCount.toDouble,
    generationChange.getCount.toDouble
  )
  def queryStats(labels: Boolean = false): String = {
    if (labels)
      queryStatLabels.zip(queryStats).map{x => f"${x._1}=${x._2}%2.3f"}.mkString(",")
    else
      queryStats.map{x => f"$x%2.3f"}.mkString(",")
  }

  def allStats: Array[Double] = queryStats ++ cacheStats
  def allStatsLabels: Array[String] = queryStatLabels ++ cacheStatLabels
  def allStats(labels: Boolean = false): String = queryStats(labels) + "," + cacheStats(labels)
  override def toString: String = allStats(true)
}

final case class DatasetState(info: String) {
  val statistics: DatasetStatistics = DatasetStatistics()
  var coldLoadAt = new AtomicLong(0)
  var warmLoadAt = new AtomicLong(0)
  var generationClock = new AtomicLong(0)
  val successGid = new AtomicReference[String]("NONE")
  val failureGid = new AtomicReference[String]("NONE")
}


