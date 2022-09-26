/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.metrics

import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.KryoSerializable
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.execution.model.gather.data.FabricDataGather
import org.burstsys.vitals.instrument._
import org.burstsys.vitals.stats.stdSkewStat

/**
 * metrics associated with the scan execution
 */
trait FabricExecutionMetrics extends FabricMetrics[FabricExecutionMetrics] {

  /**
   * the scan time (time spent in scan) for this execution/scan instance
   *
   * @return
   */
  def scanTime: Long

  /**
   * aggregate item scan times (overall cluster horsepower applied)
   *
   * @return
   */
  def scanWork: Long

  /**
   * variation across all scanTimes
   *
   * @return
   */
  def scanTimeSkew: Double

  /**
   * variation across all scanWork values
   *
   * @return
   */
  def scanWorkSkew: Double

  /**
   * the number of parallel queries in this scan
   *
   * @return
   */
  def queryCount: Long

  /**
   * number of queries that succeeded
   *
   * @return
   */
  def succeeded: Long

  /**
   * did any of the queries fail?
   *
   * @return
   */
  final
  def hadFailures: Boolean = succeeded != queryCount

  /**
   * how many queries were row limited?
   *
   * @return
   */
  def limited: Long

  /**
   * how many queries  had dictionary overflow?
   *
   * @return
   */
  def overflowed: Long

  /**
   * total row count for all queries
   *
   * @return
   */
  def rowCount: Long

  /**
   * compile time for this execution
   *
   * @return
   */
  def compileTime: Long

  /**
   * number of cache hits for this query
   * TODO - messed up
   *
   * @return
   */
  def cacheHits: Long

  /**
   *
   * @param scanTime
   * @param scanWork
   * @param queryCount
   * @param rowCount
   * @param succeeded
   * @param limited
   * @param overflowed
   * @param compileTime
   * @param cacheHits
   * @return
   */
  def init(scanTime: Long, scanWork: Long, scanTimeSkew: Long, scanWorkSkew: Long, queryCount: Long, rowCount: Long, succeeded: Long, limited: Long,
           overflowed: Long, compileTime: Long, cacheHits: Long): FabricExecutionMetrics

  //------------------ WORKER SIDE -----------------------------

  /**
   * called after each item is scanned on the worker
   *
   * @param scanTime
   */
  def recordItemScanOnWorker(scanTime: Long): Unit

  /**
   * called after all the regions are merged into a single slice result on the worker
   *
   * @param scanTime
   */
  def recordSliceScanOnWorker(scanTime: Long): Unit

  //------------------ SUPERVISOR SIDE -----------------------------

  /**
   * record execution metrics associated with a final result
   *
   * @param gather
   */
  def recordFinalMetricsOnSupervisor(gather: FabricDataGather): Unit

  final override
  def toString: String =
    s"""|EXECUTION_METRICS:
        |   scanTime=$scanTime (${prettyTimeFromNanos(scanTime)}), scanWork=$scanWork (${prettyTimeFromNanos(scanWork)})
        |   scanTimeSkew=$scanTimeSkew, scanWorkSkew=$scanWorkSkew
        |   queryCount=$queryCount (${prettyFixedNumber(queryCount)}), rowCount=$rowCount (${prettyFixedNumber(rowCount)})
        |   succeeded=$succeeded (${prettyFixedNumber(succeeded)}), limited=$limited (${prettyFixedNumber(limited)}),
        |   overflowed=$overflowed (${prettyFixedNumber(overflowed)})
        |   compileTime=$compileTime (${prettyTimeFromNanos(compileTime)}), cacheHits=$cacheHits""".stripMargin


}

object FabricExecutionMetrics {

  def apply(): FabricExecutionMetrics = FabricExecutionMetricsContext()

}

private[fabric] final case
class FabricExecutionMetricsContext() extends FabricExecutionMetrics with KryoSerializable {

  ///////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////

  private
  var _scanTime: Long = _

  private
  var _scanWork: Long = _

  private
  var _scanTimeSkew: Double = _

  private
  var _scanWorkSkew: Double = _

  private
  var _queryCount: Long = _

  private
  var _succeeded: Long = _

  private
  var _limited: Long = _

  private
  var _overflowed: Long = _

  private
  var _rowCount: Long = _

  private
  var _compileTime: Long = _

  private
  var _cacheHits: Long = _

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override def scanTimeSkew: Double = _scanTimeSkew

  override def scanWorkSkew: Double = _scanWorkSkew

  override def scanTime: Long = _scanTime

  override def scanWork: Long = _scanWork

  override def queryCount: Long = _queryCount

  override def succeeded: Long = _succeeded

  override def limited: Long = _limited

  override def overflowed: Long = _overflowed

  override def rowCount: Long = _rowCount

  override def compileTime: Long = _compileTime

  override def cacheHits: Long = _cacheHits

  override def init(scanTime: Long, scanWork: Long, scanTimeSkew: Long, scanWorkSkew: Long, queryCount: Long, rowCount: Long, succeeded: Long,
                    limited: Long, overflowed: Long, compileTime: Long, cacheHits: Long): FabricExecutionMetrics = {
    this._scanTime = scanTime
    this._scanWork = scanWork
    this._scanTimeSkew = scanTimeSkew.toDouble
    this._scanWorkSkew = scanWorkSkew.toDouble

    this._queryCount = queryCount
    this._rowCount = rowCount

    this._succeeded = succeeded
    this._limited = limited
    this._overflowed = overflowed

    this._compileTime = compileTime
    this._cacheHits = cacheHits
    this
  }

  ///////////////////////////////////////////////////////////////////
  // JSON
  ///////////////////////////////////////////////////////////////////

  override
  def toJson: FabricExecutionMetrics =
    JsonFabricExecutionMetrics(scanTime, scanWork, scanTimeSkew, scanWorkSkew, queryCount, succeeded, limited,
      overflowed, rowCount, compileTime, cacheHits)

  ///////////////////////////////////////////////////////////////////
  // MERGES
  ///////////////////////////////////////////////////////////////////

  override
  def initMetrics(key: FabricGenerationKey): Unit = {
    _scanTime = 0
    _scanWork = 0

    _scanTimeSkew = 0
    _scanWorkSkew = 0

    _queryCount = 0
    _rowCount = 0

    _succeeded = 0
    _limited = 0
    _overflowed = 0

    _compileTime = 0
    _cacheHits = 0
  }

  //------------------ SUPERVISOR SIDE -----------------------------

  override
  def recordFinalMetricsOnSupervisor(gather: FabricDataGather): Unit = {
    _queryCount = gather.queryCount
    _rowCount = gather.rowCount
    _succeeded = gather.successCount
    _limited = gather.limitCount
    _overflowed = gather.overflowCount
  }

  override
  def finalizeWaveMetricsOnSupervisor(sliceMetrics: Array[FabricExecutionMetrics]): Unit = {
    val scanTimes = sliceMetrics.map(_.scanTime)
    _scanTimeSkew = stdSkewStat(scanTimes.min, scanTimes.max)
    val scanWorks = sliceMetrics.map(_.scanWork)
    _scanWorkSkew = stdSkewStat(scanWorks.min, scanWorks.max)
    _scanTime = math.max(_scanTime, scanTimes.max)
    _scanWork += scanTimes.sum
  }

  //------------------ WORKER SIDE -----------------------------

  override
  def mergeItemMetricsOnWorker(metrics: FabricExecutionMetrics): Unit = {
    _scanWork += metrics.scanWork

    _queryCount += metrics.queryCount
    _rowCount += metrics.rowCount

    _succeeded += metrics.succeeded
    _limited += metrics.limited
    _overflowed += metrics.overflowed
  }

  override
  def finalizeRegionMetricsOnWorker(): Unit = {
    // NOOP
  }

  override
  def finalizeSliceMetricsOnWorker(): Unit = {
    // NOOP
  }

  override
  def recordItemScanOnWorker(scanWork: Long): Unit = {
    _scanWork += scanWork
  }

  override
  def recordSliceScanOnWorker(scanTime: Long): Unit = {
    _scanTime = scanTime
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    _scanTime = input.readLong
    _scanWork = input.readLong
    _scanTimeSkew = input.readDouble
    _scanWorkSkew = input.readDouble
    _compileTime = input.readLong
    _queryCount = input.readLong
    _succeeded = input.readLong
    _limited = input.readLong
    _overflowed = input.readLong
    _rowCount = input.readLong
    _cacheHits = input.readLong
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeLong _scanTime
    output writeLong _scanWork
    output writeDouble _scanTimeSkew
    output writeDouble _scanWorkSkew
    output writeLong _compileTime
    output writeLong _queryCount
    output writeLong _succeeded
    output writeLong _limited
    output writeLong _overflowed
    output writeLong _rowCount
    output writeLong _cacheHits
  }

}
