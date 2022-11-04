/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model

import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.wave.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.wave.data.model.slice.state.FabricDataState
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.logging._

import scala.language.implicitConversions

package object generation extends VitalsLogger {

  implicit def datasourceToGenerationKey(datasource: FabricDatasource): FabricGenerationKey = {
    FabricGenerationKey(datasource.domain.domainKey, datasource.view.viewKey, datasource.view.generationClock)
  }

  final case class JsonFabricGeneration(datasource: FabricDatasource,
                                        state: FabricDataState,
                                        slices: Array[FabricSliceMetadata],
                                        generationMetrics: FabricGenerationMetrics)
    extends FabricGeneration with VitalsJsonObject {

    override def addSlices(slices: Array[FabricSliceMetadata]): Unit = jsonMethodException

    override def finalizeMetrics(): Unit = jsonMethodException
  }

}
