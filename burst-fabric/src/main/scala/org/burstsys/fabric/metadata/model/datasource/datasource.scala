/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.metadata.model

import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.properties.VitalsPropertyMap

package object datasource {

  final case class JsonFabricDatasource(domain: FabricDomain, view: FabricView)
    extends FabricDatasource with VitalsJsonObject {
    override def postWaveMetricsUpdate(generationMetrics: FabricGenerationMetrics): VitalsPropertyMap = jsonMethodException
  }

  final
  def jsonClone(ds: FabricDatasource): FabricDatasource = JsonFabricDatasource(ds.domain.toJson, ds.view.toJson)
}
