/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.views

import org.burstsys.alloy
import org.burstsys.alloy.AlloyDatasetSpec
import org.burstsys.alloy.store.mini.MiniView
import org.burstsys.brio.flurry.provider.quo.BurstQuoMockPressSource
import org.burstsys.brio.flurry.provider.unity.BurstUnityMockPressSource
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.press.BrioPressSource
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.metadata.model.FabricDomainKey
import org.burstsys.fabric.metadata.model.FabricViewKey
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.vitals.properties.VitalsPropertyMap

trait UnitMiniView extends MiniView with FabricDatasource

object UnitMiniView {

  def apply(schema: BrioSchema, domainKey: FabricDomainKey, viewKey: FabricViewKey, items: Array[BrioPressInstance]): UnitMiniView =
    UnitMiniViewContext(schema: BrioSchema, domainKey: FabricDomainKey, viewKey: FabricViewKey, items: Array[BrioPressInstance])

  def apply(ds: AlloyDatasetSpec, items: Array[BrioPressInstance]): UnitMiniView =
    UnitMiniViewContext(ds.schema, ds.domainKey, ds.viewKey, items)

}

final case
class UnitMiniViewContext(schema: BrioSchema, domainKey: FabricDomainKey, viewKey: FabricViewKey, items: Array[BrioPressInstance])
  extends UnitMiniView {

  override
  def rootVersion: BrioVersionKey = 1

  override
  def presser(root: BrioPressInstance): BrioPressSource = {
    val quoSchema = BrioSchema("Quo").name
    val unitySchema = BrioSchema("Unity").name
    schema.name match {
      case `quoSchema` => BurstQuoMockPressSource(root)
      case `unitySchema` => BurstUnityMockPressSource(root)
      case _ => ???
    }
  }

  override
  def domain: FabricDomain = FabricDomain(domainKey)

  override
  def view: FabricView = FabricView(domainKey, viewKey, generationClock = 0, schema.name,
    storeProperties = Map(FabricStoreNameProperty -> alloy.store.mini.MiniStoreName)
  )

  override
  def postWaveMetricsUpdate(generationMetrics: FabricGenerationMetrics): VitalsPropertyMap = view.viewProperties

}
