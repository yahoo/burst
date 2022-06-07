/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.model

import org.burstsys.catalog.api.BurstCatalogApiMaster
import org.burstsys.catalog.cannedDataLabel
import org.burstsys.catalog.persist.NamedCatalogEntity
import org.burstsys.fabric.topology.model.node.FabricNodeId
import org.burstsys.fabric.topology.model.node.master.FabricMaster
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort}
import org.burstsys.vitals.properties.{VitalsLabelsMap, VitalsPropertyMap}

import scala.collection.immutable.Map
import scala.language.implicitConversions

package object master {

  type CatalogMaster = BurstCatalogApiMaster.Proxy with NamedCatalogEntity with FabricMaster

  object CatalogMaster {
    def apply(
               pk: Long,
               moniker: String,
               nodeName: VitalsHostName,
               nodeAddress: VitalsHostAddress,
               masterPort: VitalsHostPort,
               siteFk: Long,
               cellFk: Option[Long] = None,
               labels: Option[Map[String, String]] = None,
               masterProperties: Map[String, String] = Map.empty
             ): BurstCatalogApiMaster =
      BurstCatalogApiMaster(
        pk = pk,
        moniker = moniker,
        nodeName = nodeName,
        nodeAddress = nodeAddress,
        masterPort = masterPort,
        siteFk = siteFk,
        cellFk = cellFk,
        labels = labels,
        masterProperties = masterProperties
      )
  }

  final case
  class CatalogSqlMaster(_underlying_BurstCatalogApiMaster: BurstCatalogApiMaster)
    extends BurstCatalogApiMaster.Proxy with NamedCatalogEntity with FabricMaster {

    override def nodeMoniker: String = _underlying_BurstCatalogApiMaster.nodeMoniker

    override def nodeName: VitalsHostName = _underlying_BurstCatalogApiMaster.nodeName

    override def nodeAddress: VitalsHostAddress = _underlying_BurstCatalogApiMaster.nodeAddress

    override def nodeId: FabricNodeId = _underlying_BurstCatalogApiMaster.pk

    override def masterPort: VitalsHostPort = _underlying_BurstCatalogApiMaster.masterPort
  }

  implicit def masterApiToProxy(c: BurstCatalogApiMaster): CatalogMaster =
    CatalogSqlMaster(c)

  implicit def masterProxyToApi(c: CatalogMaster): BurstCatalogApiMaster =
    BurstCatalogApiMaster(
      pk = c.pk,
      moniker = c.moniker,
      nodeName = c.nodeName,
      nodeAddress = c.nodeAddress,
      masterPort = c.masterPort,
      siteFk = c.siteFk,
      cellFk = c.cellFk,
      labels = c.labels,
      masterProperties = c.masterProperties
    )

  final case
  class CatalogJsonMaster(
                           pk: Long,
                           moniker: String,
                           nodeName: VitalsHostName,
                           nodeAddress: VitalsHostAddress,
                           masterPort: VitalsHostPort,
                           siteFk: Long,
                           cellFk: Option[Long],
                           labels: Map[String, String],
                           masterProperties: Map[String, String]
                         )

  implicit def masterProxyToJson(c: CatalogMaster): CatalogJsonMaster =
    CatalogJsonMaster(
      pk = c.pk,
      moniker = c.moniker,
      nodeName = c.nodeName,
      nodeAddress = c.nodeAddress,
      masterPort = c.masterPort,
      siteFk = c.siteFk,
      cellFk = c.cellFk,
      labels = if (c.labels.isDefined) c.labels.get.toMap else Map.empty,
      masterProperties = c.masterProperties.toMap
    )

  implicit def masterJsonToProxy(c: CatalogJsonMaster): CatalogMaster =
    BurstCatalogApiMaster(
      pk = c.pk,
      moniker = c.moniker,
      nodeName = c.nodeName,
      nodeAddress = c.nodeAddress,
      masterPort = c.masterPort,
      siteFk = c.siteFk,
      cellFk = c.cellFk,
      masterProperties = c.masterProperties,
      labels = if (c.labels.isEmpty) None else Some(c.labels)
    )

  final case
  class CatalogCannedMaster(
                             moniker: String,
                             nodeName: VitalsHostName,
                             nodeAddress: VitalsHostAddress,
                             masterPort: VitalsHostPort,
                             siteMoniker: String,
                             cellMoniker: Option[String],
                             masterProperties: VitalsPropertyMap = Map.empty,
                             var labels: Option[VitalsLabelsMap] = None
                           ) {
    labels = Some(Map(cannedDataLabel -> "true"))
  }

}
