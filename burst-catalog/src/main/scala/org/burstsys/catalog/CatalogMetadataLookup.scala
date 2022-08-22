/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog

import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.metadata.model.{FabricDomainKey, FabricMetadataLookup, FabricViewKey}
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.util.Try

/**
 * This is a version of the [[FabricMetadataLookup]] implemented using the Burst Catalog
 */
private[catalog] final case
class CatalogMetadataLookup(catalog: CatalogService) extends AnyRef with FabricMetadataLookup {

  override
  def domainLookup(key: FabricDomainKey): Try[FabricDomain] = {
    resultOrFailure {
      catalog.findDomainByPk(key) map {
        d => FabricDomain(d.pk, d.domainProperties)
      }
    }
  }

  override
  def viewLookup(key: FabricViewKey, validate: Boolean): Try[FabricView] = {
    resultOrFailure {
      catalog.findViewByPk(key) map {
        v => FabricView(v.domainFk, v.pk, v.generationClock, v.schemaName, v.viewMotif, v.viewProperties, v.storeProperties)
      }
    }
  }

  override
  def recordViewLoad(key: FabricGenerationKey, updatedProperties: VitalsPropertyMap): Try[Boolean] = {
    val propList = updatedProperties.map(e => s"${e._1.replace(".", "_")}=${e._2}").mkString("\n\t", ",\n\t", "")
    log info s"VIEW_LOAD_RECORD CatalogMetadataLookup.recordViewLoad(generation=$key) $propList"
    resultOrFailure {
      catalog.recordViewLoad(key.viewKey, updatedProperties) map { _ => true }
    }
  }
}
