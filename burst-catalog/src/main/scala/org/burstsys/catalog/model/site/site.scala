/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.model

import org.burstsys.catalog.api.BurstCatalogApiSite
import org.burstsys.catalog.cannedDataLabel
import org.burstsys.catalog.persist.NamedCatalogEntity
import org.burstsys.fabric.metadata.model.FabricSiteKey
import org.burstsys.fabric.topology.model.site.FabricSite
import org.burstsys.vitals.properties.{VitalsLabelsMap, VitalsPropertyMap}

import scala.language.implicitConversions

package object site {

  type CatalogSite = BurstCatalogApiSite.Proxy with NamedCatalogEntity with FabricSite

  object CatalogSite {
    def apply(
               pk: Long,
               moniker: String,
               siteProperties: Map[String, String] = Map(),
               labels: Option[Map[String, String]] = None
             ): BurstCatalogApiSite =
      BurstCatalogApiSite(pk = pk, moniker = moniker, siteProperties = siteProperties, labels = labels)
  }

  final case
  class CatalogSqlSite(_underlying_BurstCatalogApiSite: BurstCatalogApiSite)
    extends BurstCatalogApiSite.Proxy with NamedCatalogEntity with FabricSite {

    override def siteKey: FabricSiteKey = pk
  }

  implicit def siteApiToProxy(s: BurstCatalogApiSite): CatalogSite =
    CatalogSqlSite(s)

  implicit def siteProxyToApi(s: CatalogSite): BurstCatalogApiSite =
    BurstCatalogApiSite(s.pk, s.moniker, s.siteProperties, s.labels)

  final case
  class CatalogJsonSite(pk: Long, moniker: String, labels: Map[String, String], siteProperties: Map[String, String])

  implicit def siteProxyToJson(s: CatalogSite): CatalogJsonSite =
    CatalogJsonSite(s.pk, s.moniker, if (s.labels.isDefined) s.labels.get.toMap else Map.empty, s.siteProperties.toMap)

  implicit def siteJsonToProxy(s: CatalogJsonSite): CatalogSite =
    BurstCatalogApiSite(s.pk, s.moniker, s.siteProperties, if (s.labels.isEmpty) None else Some(s.labels))

  final case
  class CatalogCannedSite(moniker: String, siteProperties: VitalsPropertyMap = Map.empty,
                          var labels: Option[VitalsLabelsMap] = None) {
    labels = Some(Map(cannedDataLabel -> "true"))
  }
}
