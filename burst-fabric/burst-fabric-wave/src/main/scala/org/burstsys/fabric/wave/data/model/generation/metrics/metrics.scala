/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.generation

import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.slice.state.FabricDataState
import org.burstsys.fabric.wave.metadata._
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.language.implicitConversions

package object metrics {

  implicit def generationMetricsToProperties(m: FabricGenerationMetrics): VitalsPropertyMap =
    Map(
      ViewLastDatasetSizeProperty -> m.byteCount.toString,
      ViewLastItemSizeProperty -> m.itemSize.toString,
      ViewLastItemVariationProperty -> m.itemVariation.toString,
      ViewLastColdLoadAtProperty -> m.coldLoadAt.toString,
      ViewLastLoadInvalidProperty -> m.loadInvalid.toString,
      ViewEarliestLoadAtProperty -> m.earliestLoadAt.toString,
      ViewLastColdLoadTookProperty -> m.coldLoadTook.toString,
      ViewLastSliceCountProperty -> m.sliceCount.toString,
      ViewLastRejectedItemCountProperty -> m.rejectedItemCount.toString,

      ViewLastPotentialItemCountProperty -> m.potentialItemCount.toString,

      ViewSuggestedSampleRateProperty -> m.suggestedSampleRate.toString,
      ViewSuggestedSliceCountProperty -> m.suggestedSliceCount.toString
    )

  final case
  class JsonFabricGenerationMetrics(generationKey: FabricGenerationKey, state: FabricDataState, timeSkew: Double,
                                    sizeSkew: Double, byteCount: Long, itemCount: Long, sliceCount: Long,
                                    regionCount: Long, evictCount: Int, flushCount: Int, coldLoadAt: Long,
                                    coldLoadTook: Long, warmLoadAt: Long, warmLoadTook: Long, warmLoadCount: Long,
                                    itemSize: Double, itemVariation: Double, loadInvalid: Boolean, earliestLoadAt: Long,
                                    rejectedItemCount: Long, expectedItemCount: Long, potentialItemCount: Long,
                                    suggestedSampleRate: Double, suggestedSliceCount: Long)
    extends FabricGenerationMetrics with VitalsJsonObject {

    override def state_=(s: FabricDataState): Unit = jsonMethodException

    override def recordSliceNormalColdLoad(loadTookMs: Long, regionCount: Long, itemCount: Long, expectedItemCount: Long, potentialItemCount: Long, rejectedItemCount: Long, byteCount: Long): Unit = jsonMethodException

    override def recordSliceEmptyColdLoad(loadTookMs: Long, regionCount: Long): Unit = jsonMethodException

    override def recordSliceNormalWarmLoad(warmLoadMs: Long): Unit = jsonMethodException

    override def recordSliceEvictOnWorker(): Unit = jsonMethodException

    override def recordSliceFlushOnWorker(): Unit = jsonMethodException

    override def xferSliceCacheLoadMetrics(cacheGenerationMetrics: FabricGenerationMetrics): Unit = jsonMethodException

    override def calcEarliestLoadAt(loadStaleMs: Long): Unit = jsonMethodException

    override def init(generationKey: FabricGenerationKey, state: FabricDataState, byteCount: Long, itemCount: Long, sliceCount: Long, regionCount: Long, coldLoadAt: Long, coldLoadTook: Long, warmLoadAt: Long, warmLoadTook: Long, warmLoadCount: Long, sizeSkew: Double, timeSkew: Double, itemSize: Double, itemVariation: Double, loadInvalid: Boolean, earliestLoadAt: Long, rejectedItemCount: Long, expectedItemCount: Long, potentialItemCount: Long, suggestedSampleRate: Double, suggestedSliceCount: Long): FabricGenerationMetrics = jsonMethodException

    override def initMetrics(key: FabricGenerationKey): Unit = jsonMethodException

    override def mergeItemMetricsOnWorker(metrics: FabricGenerationMetrics): Unit = jsonMethodException

    override def finalizeRegionMetricsOnWorker(): Unit = jsonMethodException

    override def finalizeSliceMetricsOnWorker(): Unit = jsonMethodException

    override def finalizeWaveMetricsOnSupervisor(sliceMetrics: Array[FabricGenerationMetrics]): Unit = jsonMethodException
  }

}
