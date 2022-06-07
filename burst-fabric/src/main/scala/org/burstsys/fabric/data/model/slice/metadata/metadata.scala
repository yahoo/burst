/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.slice

import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.data.model.slice.state.FabricDataState
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostName

package object metadata extends VitalsLogger {

  final case
  class JsonFabricSliceMetadata(datasource: FabricDatasource,
                                sliceKey: FabricSliceKey,
                                hostname: VitalsHostName,
                                state: FabricDataState,
                                failure: String,
                                generationMetrics: FabricGenerationMetrics)
    extends FabricSliceMetadata with VitalsJsonObject {

    override def state_=(s: FabricDataState): Unit = jsonMethodException

    override def failure(t: Throwable): Unit = jsonMethodException

    override def failure(msg: String): Unit = jsonMethodException

    override def reset(): Unit = jsonMethodException
  }

}
