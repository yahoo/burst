/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.gather

import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.wave.execution.model.metrics.FabricExecutionMetrics
import org.burstsys.fabric.wave.execution.model.result.status.FabricResultStatus
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.logging.VitalsLogger

package object metrics extends VitalsLogger {

  final case
  class JsonFabricGatherMetrics(generationKey: FabricGenerationKey,
                                generationMetrics: FabricGenerationMetrics,
                                executionMetrics: FabricExecutionMetrics,
                                resultStatus: FabricResultStatus,
                                resultMessage: String)
    extends FabricGatherMetrics with VitalsJsonObject {

    override def initMetrics(key: FabricGenerationKey): Unit = jsonMethodException

    override def mergeItemMetricsOnWorker(metrics: FabricGatherMetrics): Unit = jsonMethodException

    override def finalizeRegionMetricsOnWorker(): Unit = jsonMethodException

    override def finalizeSliceMetricsOnWorker(): Unit = jsonMethodException

    override def finalizeWaveMetricsOnSupervisor(sliceMetrics: Array[FabricGatherMetrics]): Unit = jsonMethodException
  }

}
