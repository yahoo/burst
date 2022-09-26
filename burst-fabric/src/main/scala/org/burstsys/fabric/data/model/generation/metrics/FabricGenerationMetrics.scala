/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.generation.metrics

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.KryoSerializable
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.burstsys.fabric.configuration
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKeyContext
import org.burstsys.fabric.data.model.slice.state._
import org.burstsys.fabric.execution.model.metrics.FabricMetrics
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.instrument._
import org.burstsys.vitals.stats.stdSkewStat

import scala.language.implicitConversions

/**
 * All metrics related to the generation of a [[org.burstsys.fabric.metadata.model.view.FabricView]]
 */
trait FabricGenerationMetrics extends FabricMetrics[FabricGenerationMetrics] {

  /**
   * @return generation clock
   */
  def generationKey: FabricGenerationKey

  /**
   * @return the state of the data
   */
  def state: FabricDataState

  def state_=(s: FabricDataState): Unit

  /**
   * @return what was the time skew across all slices
   */
  def timeSkew: Double

  /**
   * @return what was the space skew across all slices
   */
  def sizeSkew: Double

  /**
   * @return how many bytes is this generation across all slices
   */
  def byteCount: Long

  /**
   * @return the count of items in this generation
   */
  def itemCount: Long

  /**
   * @return the count of slices in this generation
   */
  def sliceCount: Long

  /**
   * @return number of regions in this slice
   */
  def regionCount: Long

  /**
   * @return when was this slice generated
   */
  def coldLoadAt: Long

  /**
   * @return how long did the generation take (ms)
   */
  def coldLoadTook: Long

  /**
   * @return when was the slice _last_ warm loaded
   */
  def warmLoadAt: Long

  /**
   * @return how long did the last load take (ms)
   */
  def warmLoadTook: Long

  /**
   * @return how many times was this slice loaded
   */
  def warmLoadCount: Long

  ////////////////////////////////////////////////////////////////////////////////
  // System actual values from the load
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * @return the actual average bytes/item for this dataset (updated each load)
   */
  def itemSize: Double

  /**
   * @return the actual variation factor for item sizes (updated each load)
   */
  def itemVariation: Double

  /**
   * @return true if the next-dataset-size and next-sample-rate were not achievable (updated each load)
   */
  def loadInvalid: Boolean

  /**
   * @return a query after this time would result in a cold load
   */
  def earliestLoadAt: Long

  /**
   * @return number of items rejected cause they exceeded next-item-max (updated each load)
   */
  def rejectedItemCount: Long

  ////////////////////////////////////////////////////////////////////////////////
  // EVICT/FLUSH
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * @return number of times this slice was evicted (average over workers)
   */
  def evictCount: Int

  /**
   * @return number of times this slice was flushed (average over workers)
   */
  def flushCount: Int

  ////////////////////////////////////////////////////////////////////////////////
  // System estimated values for the load
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * @return the number of items that the fabric store intends to be in the generation, based on sampling ratio or max data size
   */
  def expectedItemCount: Long

  /**
   * @return The potential number of items the fabric store believes could to be in the generation, if all size and sampling constraints were lifted
   */
  def potentialItemCount: Long

  ////////////////////////////////////////////////////////////////////////////////
  // System suggested values based on the load
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * @return The system calculated sample rate recommended for next load. If next-dataset-size does not change, using this
   *         value for next-sample-rate will result in a actual-cap-ratio of 1.0. (updated each load)
   */
  def suggestedSampleRate: Double

  /**
   * @return The system calculated slice count recommended for next load (updated each load)
   */
  def suggestedSliceCount: Long

  //------------------ WORKER SIDE -----------------------------

  /**
   * called each time an normal (non-empty) slice is cold loaded into disk cache
   *
   * @param loadTookMs
   * @param itemCount
   * @param regionCount
   * @param potentialItemCount
   * @param rejectedItemCount
   * @param byteCount
   */
  def recordSliceNormalColdLoad(loadTookMs: Long,
                                regionCount: Long,
                                itemCount: Long,
                                expectedItemCount: Long,
                                potentialItemCount: Long,
                                rejectedItemCount: Long,
                                byteCount: Long): Unit

  /**
   * called each time an empty slice is cold loaded into disk cache
   *
   * @param loadTookMs  how long the cold load took
   * @param regionCount how many regions are in the slice
   */
  def recordSliceEmptyColdLoad(loadTookMs: Long, regionCount: Long): Unit

  /**
   * called each time a slice is loaded from disk into memory
   *
   * @param warmLoadMs how long the warm load took
   */
  def recordSliceNormalWarmLoad(warmLoadMs: Long): Unit

  /**
   * called each time a slice is removed from memory
   */
  def recordSliceEvictOnWorker(): Unit

  /**
   * called each time a slice is removed from disk
   */
  def recordSliceFlushOnWorker(): Unit

  /**
   * we store all ongoing generation load metrics in cache (snap) and for each wave that executes against
   * that generation we need to transfer those cache load metrics to the overall wave metrics.
   * This means each wave gets not only execution but generation metrics each time.
   *
   * @param cacheGenerationMetrics
   */
  def xferSliceCacheLoadMetrics(cacheGenerationMetrics: FabricGenerationMetrics): Unit

  //------------------ Supervisor SIDE -----------------------------

  /**
   *
   * @param loadStaleMs
   */
  def calcEarliestLoadAt(loadStaleMs: Long): Unit

  /**
   * init the metrics object to a set of values - we do this so we do not create new objects
   * all the time.
   *
   * @param generationKey
   * @param state
   * @param byteCount
   * @param itemCount
   * @param sliceCount
   * @param regionCount
   * @param coldLoadAt
   * @param coldLoadTook
   * @param warmLoadAt
   * @param warmLoadTook
   * @param warmLoadCount
   * @param sizeSkew
   * @param timeSkew
   * @param itemSize
   * @param itemVariation
   * @param loadInvalid
   * @param earliestLoadAt
   * @param rejectedItemCount
   * @param potentialItemCount
   * @param suggestedSampleRate
   * @param suggestedSliceCount
   * @return
   */
  def init(
            generationKey: FabricGenerationKey,
            state: FabricDataState,
            byteCount: Long,
            itemCount: Long,
            sliceCount: Long,
            regionCount: Long,
            coldLoadAt: Long,
            coldLoadTook: Long,
            warmLoadAt: Long,
            warmLoadTook: Long,
            warmLoadCount: Long,
            sizeSkew: Double,
            timeSkew: Double,
            itemSize: Double,
            itemVariation: Double,
            loadInvalid: Boolean,
            earliestLoadAt: Long,
            rejectedItemCount: Long,
            expectedItemCount: Long,
            potentialItemCount: Long,
            suggestedSampleRate: Double,
            suggestedSliceCount: Long
          ): FabricGenerationMetrics

  final override
  def toString: String =
    s"FabGenMetrics($metricsString)"

  final def metricsString: String =
    s"""$generationKey state=${state}, loadInvalid=$loadInvalid
       |  itemCount=$itemCount rejectedItemCount=$rejectedItemCount, potentialItemCount=$potentialItemCount
       |  suggestedSampleRate=$suggestedSampleRate, suggestedSliceCount=$suggestedSliceCount
       |  byteCount=$byteCount (${prettyByteSizeString(byteCount)}), sliceCount=$sliceCount, regionCount=$regionCount
       |  coldLoadAt=$coldLoadAt (${prettyDateTimeFromMillisNoSpaces(coldLoadAt)}), coldLoadTook=$coldLoadTook (${prettyTimeFromMillis(coldLoadTook)}), earliestLoadAt=$earliestLoadAt (${prettyDateTimeFromMillisNoSpaces(earliestLoadAt)})
       |  warmLoadAt=$warmLoadAt (${prettyDateTimeFromMillisNoSpaces(warmLoadAt)}), warmLoadTook=$warmLoadTook (${prettyTimeFromNanos(warmLoadTook)}), warmLoadCount=$warmLoadCount
       |  sizeSkew=$sizeSkew, timeSkew=$timeSkew, itemSize=$itemSize, itemVariation=$itemVariation""".stripMargin
}

object FabricGenerationMetrics {

  def apply(): FabricGenerationMetrics = FabricGenerationMetricsContext()

  def apply(datasource: FabricDatasource): FabricGenerationMetrics = {
    val metrics = FabricGenerationMetricsContext()
    metrics.initMetrics(datasource)
    metrics
  }

}

private[fabric] final case
class FabricGenerationMetricsContext() extends KryoSerializable with FabricGenerationMetrics {

  ///////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////

  private var _generationKey: FabricGenerationKey = FabricGenerationKey()

  private var _state: FabricDataState = FabricDataCold

  private var _byteCount: Long = _

  private var _itemCount: Long = _

  private var _sliceCount: Long = _

  private var _regionCount: Long = _

  private var _coldLoadAt: Long = _

  private var _coldLoadTook: Long = _

  private var _warmLoadAt: Long = _

  private var _warmLoadTook: Long = _

  private var _warmLoadCount: Long = _

  private var _sizeSkew: Double = _

  private var _timeSkew: Double = _

  private var _itemSize: Double = _

  private var _itemVariation: Double = _

  private var _loadInvalid: Boolean = _

  private var _earliestLoadAt: Long = _

  private var _evictCount: Int = _

  private var _flushCount: Int = _

  private var _rejectedItemCount: Long = _

  private var _expectedItemCount: Long = _

  private var _potentialItemCount: Long = _

  private var _suggestedSampleRate: Double = _

  private var _suggestedSliceCount: Long = _

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override def state_=(s: FabricDataState): Unit = _state = s

  override def evictCount: Int = _evictCount

  override def flushCount: Int = _flushCount

  override def generationKey: FabricGenerationKey = _generationKey

  override def state: FabricDataState = _state

  override def timeSkew: Double = _timeSkew

  override def sizeSkew: Double = _sizeSkew

  override def byteCount: Long = _byteCount

  override def itemCount: Long = _itemCount

  override def sliceCount: Long = _sliceCount

  override def regionCount: Long = _regionCount

  override def coldLoadAt: Long = _coldLoadAt

  override def coldLoadTook: Long = _coldLoadTook

  override def warmLoadAt: Long = _warmLoadAt

  override def warmLoadTook: Long = _warmLoadTook

  override def warmLoadCount: Long = _warmLoadCount

  override def itemSize: Double = _itemSize

  override def itemVariation: Double = _itemVariation

  override def loadInvalid: Boolean = _loadInvalid

  override def earliestLoadAt: Long = _earliestLoadAt

  override def rejectedItemCount: Long = _rejectedItemCount

  override def expectedItemCount: Long = _expectedItemCount

  override def potentialItemCount: Long = _potentialItemCount

  override def suggestedSampleRate: Double = _suggestedSampleRate

  override def suggestedSliceCount: Long = _suggestedSliceCount

  override def init(
                     generationKey: FabricGenerationKey,
                     state: FabricDataState,
                     byteCount: Long,
                     itemCount: Long,
                     sliceCount: Long,
                     regionCount: Long,
                     coldLoadAt: Long,
                     coldLoadTook: Long,
                     warmLoadAt: Long,
                     warmLoadTook: Long,
                     warmLoadCount: Long,
                     sizeSkew: Double,
                     timeSkew: Double,
                     itemSize: Double,
                     itemVariation: Double,
                     loadInvalid: Boolean,
                     earliestLoadAt: Long,
                     rejectedItemCount: Long,
                     expectedItemCount: Long,
                     potentialItemCount: Long,
                     suggestedSampleRate: Double,
                     suggestedSliceCount: Long
                   ): FabricGenerationMetrics = {
    this._generationKey.init(generationKey)
    this._state = state
    this._byteCount = byteCount
    this._itemCount = itemCount
    this._sliceCount = sliceCount
    this._regionCount = regionCount
    this._coldLoadAt = coldLoadAt
    this._coldLoadTook = coldLoadTook
    this._warmLoadAt = warmLoadAt
    this._warmLoadTook = warmLoadTook
    this._warmLoadCount = warmLoadCount
    this._sizeSkew = sizeSkew
    this._timeSkew = timeSkew
    this._itemSize = itemSize
    this._itemVariation = itemVariation
    this._loadInvalid = loadInvalid
    this._earliestLoadAt = earliestLoadAt
    this._rejectedItemCount = rejectedItemCount
    this._expectedItemCount = expectedItemCount
    this._potentialItemCount = potentialItemCount
    this._suggestedSampleRate = suggestedSampleRate
    this._suggestedSliceCount = suggestedSliceCount
    this
  }

  ///////////////////////////////////////////////////////////////////
  // EVICT/FLUCH
  ///////////////////////////////////////////////////////////////////

  override def recordSliceEvictOnWorker(): Unit = {
    _evictCount += 1
    _state = FabricDataWarm
  }

  override def recordSliceFlushOnWorker(): Unit = {
    _flushCount += 1
    _state = FabricDataCold
  }

  ///////////////////////////////////////////////////////////////////
  // JSON
  ///////////////////////////////////////////////////////////////////

  override
  def toJson: FabricGenerationMetrics = JsonFabricGenerationMetrics(
    generationKey.toJson, state, timeSkew, sizeSkew, byteCount, itemCount, sliceCount, regionCount, evictCount, flushCount,
    coldLoadAt, coldLoadTook, warmLoadAt, warmLoadTook, warmLoadCount, itemSize, itemVariation, loadInvalid,
    earliestLoadAt, rejectedItemCount, expectedItemCount, potentialItemCount, suggestedSampleRate, suggestedSliceCount
  )

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////
  override
  def initMetrics(key: FabricGenerationKey): Unit = {
    _generationKey.init(key)
    _state = FabricDataCold

    _byteCount = 0
    _itemCount = 0
    _sliceCount = 0
    _regionCount = 0

    _coldLoadAt = 0
    _coldLoadTook = 0
    _warmLoadAt = 0
    _warmLoadTook = 0
    _warmLoadCount = 0

    _evictCount = 0
    _flushCount = 0

    _timeSkew = 0
    _sizeSkew = 0

    _itemSize = 0.0
    _itemVariation = 0.0
    _loadInvalid = false
    _earliestLoadAt = 0
    _rejectedItemCount = 0

    _expectedItemCount = 0
    _potentialItemCount = 0

    _suggestedSampleRate = 0.0
    _suggestedSliceCount = 0
  }

  //------------------ Supervisor SIDE -----------------------------

  override
  def finalizeWaveMetricsOnSupervisor(sliceMetrics: Array[FabricGenerationMetrics]): Unit = {
    // this is a very important routine - last stop to massage a lot of numbers gathered from a lot of places
    var coldLoadAt = Long.MinValue
    var coldLoadTook: Long = 0
    var warmLoadAt = Long.MinValue
    var warmLoadTook: Long = 0
    var warmLoadCount: Long = 0

    var hadFailure = false
    var allEmpty = true
    var allCold = true
    var allNonEmptyOnDisk = true
    var allNonEmptyInMemory = true
    var sizeMax: Long = 0
    var sizeMin = Long.MaxValue
    val maxLoadBytes = configuration.burstFabricDatasourceMaxSizeProperty.getOrThrow

    var itemCountTally: Long = 0
    var byteCountTally: Long = 0
    var regionCountTally: Long = 0
    var expectedItemTally: Long = 0
    var potentialItemTally: Long = 0
    var rejectedItemTally: Long = 0

    var evictCountTally = 0
    var flushCountTally = 0

    sliceMetrics.foreach { slice =>
      itemCountTally += slice.itemCount
      byteCountTally += slice.byteCount
      regionCountTally += slice.regionCount
      expectedItemTally += slice.expectedItemCount
      potentialItemTally += slice.potentialItemCount
      rejectedItemTally += slice.rejectedItemCount

      evictCountTally += slice.evictCount
      flushCountTally += slice.flushCount

      warmLoadCount = math.max(warmLoadCount, slice.warmLoadCount)
      warmLoadAt = math.max(warmLoadAt, slice.warmLoadAt)
      warmLoadTook = math.max(warmLoadTook, slice.warmLoadTook)
      coldLoadAt = math.max(coldLoadAt, slice.coldLoadAt)
      coldLoadTook = math.max(coldLoadTook, slice.coldLoadTook)

      if (slice.state == FabricDataFailed) hadFailure = true
      allCold = allCold && slice.state == FabricDataCold
      if (slice.state != FabricDataNoData) {
        allEmpty = false
        if (slice.state != FabricDataWarm) allNonEmptyOnDisk = false
        if (slice.state != FabricDataHot) allNonEmptyInMemory = false

        sizeMax = math.max(sizeMax, slice.byteCount)
        sizeMin = math.min(sizeMin, slice.byteCount)
      }
    }
    _evictCount = if (_sliceCount.toInt == 0) 0 else evictCountTally / _sliceCount.toInt // average across slices
    _flushCount = if (_sliceCount.toInt == 0) 0 else flushCountTally / _sliceCount.toInt // average across slices

    if (hadFailure) {
      _state = FabricDataFailed
    } else if (allEmpty) {
      _state = FabricDataNoData
    } else if (allNonEmptyOnDisk) {
      _state = FabricDataWarm
    } else if (allNonEmptyInMemory) {
      _state = FabricDataHot
    } else if (allCold) {
      _state = FabricDataCold
    } else {
      _state = FabricDataMixed
    }
    _sliceCount = sliceMetrics.length

    _byteCount = byteCountTally
    _regionCount = regionCountTally

    _itemCount = itemCountTally
    _expectedItemCount = expectedItemTally
    _potentialItemCount = potentialItemTally
    _rejectedItemCount = rejectedItemTally
    _itemSize = if (_itemCount == 0) 0.0 else _byteCount / _itemCount.toDouble
    _itemVariation = 0

    _suggestedSliceCount = _sliceCount
    val sampleRateSizeAdjustFactor = math.min(maxLoadBytes.toDouble / _byteCount.toDouble, 1.0)
    _suggestedSampleRate = sampleRateSizeAdjustFactor * (_itemCount.toDouble / _potentialItemCount.toDouble)

    val coldLoadTooks = sliceMetrics.map(_.coldLoadTook)
    _timeSkew = if (coldLoadTooks.isEmpty) 0 else stdSkewStat(coldLoadTooks.min, coldLoadTooks.max)
    val byteCounts = sliceMetrics.map(_.byteCount)
    _sizeSkew = if (byteCounts.isEmpty) 0 else stdSkewStat(byteCounts.min, byteCounts.max)

    _loadInvalid = (_expectedItemCount != _itemCount) || (_byteCount > maxLoadBytes)
  }


  //------------------ WORKER SIDE -----------------------------

  override
  def mergeItemMetricsOnWorker(metrics: FabricGenerationMetrics): Unit = {
    // NOOP
  }

  override
  def finalizeRegionMetricsOnWorker(): Unit = {
    // NOOP
  }

  override
  def finalizeSliceMetricsOnWorker(): Unit = {
    // NOOP
  }

  ///////////////////////////////////////////////////////////////////
  // UPDATES
  ///////////////////////////////////////////////////////////////////

  override
  def recordSliceNormalColdLoad(loadTookMs: Long,
                                regionCount: Long,
                                itemCount: Long,
                                expectedItemCount: Long,
                                potentialItemCount: Long,
                                rejectedItemCount: Long,
                                byteCount: Long): Unit = {
    _state = FabricDataWarm
    _sliceCount = 1
    _regionCount = regionCount
    _itemCount = itemCount
    _potentialItemCount = potentialItemCount
    _expectedItemCount = expectedItemCount
    _rejectedItemCount = rejectedItemCount
    _coldLoadAt = System.currentTimeMillis
    _coldLoadTook = loadTookMs
    _byteCount = byteCount
  }

  override
  def recordSliceEmptyColdLoad(loadTookMs: Long,
                               regionCount: Long): Unit = {
    _state = FabricDataNoData
    _sliceCount = 1
    _regionCount = regionCount
    _itemCount = 0
    _byteCount = 0
    _potentialItemCount = 0
    _rejectedItemCount = 0
    _coldLoadAt = System.currentTimeMillis
    _coldLoadTook = loadTookMs
  }

  override
  def recordSliceNormalWarmLoad(warmLoadMs: Long): Unit = {
    _state = FabricDataHot
    _warmLoadCount += 1
    _warmLoadTook = warmLoadMs
    _warmLoadAt = System.currentTimeMillis
  }


  override
  def xferSliceCacheLoadMetrics(cacheGenerationMetrics: FabricGenerationMetrics): Unit = {
    this._generationKey.init(cacheGenerationMetrics.generationKey)
    this._state = cacheGenerationMetrics.state

    this._regionCount = cacheGenerationMetrics.regionCount
    this._sliceCount = cacheGenerationMetrics.sliceCount

    this._evictCount = cacheGenerationMetrics.evictCount
    this._flushCount = cacheGenerationMetrics.flushCount

    this._itemCount = cacheGenerationMetrics.itemCount
    this._expectedItemCount = cacheGenerationMetrics.expectedItemCount
    this._potentialItemCount = cacheGenerationMetrics.potentialItemCount
    this._rejectedItemCount = cacheGenerationMetrics.rejectedItemCount
    this._itemVariation = cacheGenerationMetrics.itemVariation

    this._byteCount = cacheGenerationMetrics.byteCount
    this._coldLoadAt = cacheGenerationMetrics.coldLoadAt
    this._coldLoadTook = cacheGenerationMetrics.coldLoadTook
    this._warmLoadAt = cacheGenerationMetrics.warmLoadAt
    this._warmLoadTook = cacheGenerationMetrics.warmLoadTook
    this._warmLoadCount = cacheGenerationMetrics.warmLoadCount
  }

  override
  def calcEarliestLoadAt(loadStaleMs: Long): Unit = {
    _earliestLoadAt = _coldLoadAt + loadStaleMs
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      _generationKey = kryo.readClassAndObject(input).asInstanceOf[FabricGenerationKeyContext]
      _state = kryo.readClassAndObject(input).asInstanceOf[FabricDataState]

      _byteCount = input.readLong
      _itemCount = input.readLong
      _sliceCount = input.readLong
      _regionCount = input.readLong

      _coldLoadAt = input.readLong
      _coldLoadTook = input.readLong
      _warmLoadAt = input.readLong
      _warmLoadTook = input.readLong
      _warmLoadCount = input.readLong

      _evictCount = input.readInt
      _flushCount = input.readInt

      _sizeSkew = input.readDouble
      _timeSkew = input.readDouble

      _itemSize = input.readDouble
      _itemVariation = input.readDouble
      _loadInvalid = input.readBoolean
      _earliestLoadAt = input.readLong
      _rejectedItemCount = input.readLong

      _expectedItemCount = input.readLong
      _potentialItemCount = input.readLong

      _suggestedSampleRate = input.readDouble
      _suggestedSliceCount = input.readLong
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      kryo.writeClassAndObject(output, _generationKey)
      kryo.writeClassAndObject(output, _state)

      output writeLong _byteCount
      output writeLong _itemCount
      output writeLong _sliceCount
      output writeLong _regionCount

      output writeLong _coldLoadAt
      output writeLong _coldLoadTook
      output writeLong _warmLoadAt
      output writeLong _warmLoadTook
      output writeLong _warmLoadCount

      output writeInt _evictCount
      output writeInt _flushCount

      output writeDouble _sizeSkew
      output writeDouble _timeSkew

      output writeDouble _itemSize
      output writeDouble _itemVariation
      output writeBoolean _loadInvalid
      output writeLong _earliestLoadAt
      output writeLong _rejectedItemCount

      output writeLong _expectedItemCount
      output writeLong _potentialItemCount

      output writeDouble _suggestedSampleRate
      output writeLong _suggestedSliceCount
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
