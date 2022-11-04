/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model

import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.execution.model.gather.data.FabricDataGather
import org.burstsys.vitals.json.VitalsJsonObject

package object metrics {

  final case
  class JsonFabricExecutionMetrics(scanTime: Long,
                                   scanWork: Long,
                                   scanTimeSkew: Double,
                                   scanWorkSkew: Double,
                                   queryCount: Long,
                                   succeeded: Long,
                                   limited: Long,
                                   overflowed: Long,
                                   rowCount: Long,
                                   compileTime: Long,
                                   cacheHits: Long)
    extends FabricExecutionMetrics with VitalsJsonObject {

    override def init(scanTime: Long, scanWork: Long, scanTimeSkew: Long, scanWorkSkew: Long, queryCount: Long, rowCount: Long, succeeded: Long, limited: Long, overflowed: Long, compileTime: Long, cacheHits: Long): FabricExecutionMetrics = jsonMethodException

    override def recordItemScanOnWorker(scanTime: Long): Unit = jsonMethodException

    override def recordSliceScanOnWorker(scanTime: Long): Unit = jsonMethodException

    override def recordFinalMetricsOnSupervisor(gather: FabricDataGather): Unit = jsonMethodException

    override def initMetrics(key: FabricGenerationKey): Unit = jsonMethodException

    override def mergeItemMetricsOnWorker(metrics: FabricExecutionMetrics): Unit = jsonMethodException

    override def finalizeRegionMetricsOnWorker(): Unit = jsonMethodException

    override def finalizeSliceMetricsOnWorker(): Unit = jsonMethodException

    override def finalizeWaveMetricsOnSupervisor(sliceMetrics: Array[FabricExecutionMetrics]): Unit = jsonMethodException
  }

}
