/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.model

import org.burstsys.catalog.api.BurstCatalogApiCell
import org.burstsys.catalog.cannedDataLabel
import org.burstsys.catalog.persist.NamedCatalogEntity
import org.burstsys.fabric.metadata.model.FabricCellKey
import org.burstsys.fabric.topology.model.cell.FabricCell
import org.burstsys.vitals.properties.{VitalsLabelsMap, VitalsPropertyMap}

import scala.collection.immutable.Map
import scala.language.implicitConversions

package object cell {

  type CatalogCell = BurstCatalogApiCell.Proxy with NamedCatalogEntity with FabricCell

  object CatalogCell {
    def apply(
               pk: Long,
               moniker: String,
               siteFk: Long,
               cellProperties: Map[String, String] = Map(),
               labels: Option[Map[String, String]] = None
             ): BurstCatalogApiCell =
      BurstCatalogApiCell(pk = pk, moniker = moniker, siteFk = siteFk, cellProperties = cellProperties, labels = labels)
  }

  final case
  class CatalogSqlCell(_underlying_BurstCatalogApiCell: BurstCatalogApiCell)
    extends BurstCatalogApiCell.Proxy with NamedCatalogEntity with FabricCell {

    override def cellKey: FabricCellKey = pk
  }

  implicit def cellApiToProxy(c: BurstCatalogApiCell): CatalogCell =
    CatalogSqlCell(c)

  implicit def cellProxyToApi(c: CatalogCell): BurstCatalogApiCell =
    BurstCatalogApiCell(c.pk, c.moniker, c.siteFk, c.cellProperties)

  final case
  class CatalogJsonCell(pk: Long, moniker: String, siteFk: Long, labels: Map[String, String], cellProperties: Map[String, String])

  implicit def cellProxyToJson(c: CatalogCell): CatalogJsonCell =
    CatalogJsonCell(c.pk, c.moniker, c.siteFk, if (c.labels.isDefined) c.labels.get.toMap else Map.empty, c.cellProperties.toMap)

  implicit def cellJsonToProxy(c: CatalogJsonCell): CatalogCell =
    BurstCatalogApiCell(c.pk, c.moniker, c.siteFk, c.cellProperties, if (c.labels.isEmpty) None else Some(c.labels))

  final case
  class CatalogCannedCell(moniker: String, siteMoniker: String, cellProperties: VitalsPropertyMap = Map.empty,
                          var labels: Option[VitalsLabelsMap] = None) {
    labels = Some(Map(cannedDataLabel -> "true"))
  }

}
