/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy.views

import java.nio.file.Path

import org.burstsys.alloy
import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.{AlloyJsonFileProperty, AlloyJsonRootVersionProperty}
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.metadata.model.{FabricDomainKey, FabricViewKey}
import org.burstsys.vitals.properties.VitalsPropertyMap

final case
class AlloyJsonFileView(schema: BrioSchema, domainKey: FabricDomainKey, viewKey: FabricViewKey, source: Path)
  extends AnyRef with AlloyView with FabricDatasource {

  def rootVersion: BrioVersionKey = 1

  override
  def domain: FabricDomain = FabricDomain(
    domainKey = domainKey
  )

  override
  def view: FabricView = FabricView(
    domainKey = domainKey,
    viewKey = viewKey,
    schemaName = schema.name,
    storeProperties = Map(
      FabricStoreNameProperty -> alloy.alloy.store.AlloyJsonStoreName,
      AlloyJsonFileProperty -> source.normalize().toString,
      AlloyJsonRootVersionProperty -> rootVersion.toString
    )
  )

  override
  def postWaveMetricsUpdate(generationMetrics: FabricGenerationMetrics): VitalsPropertyMap = view.viewProperties

}
