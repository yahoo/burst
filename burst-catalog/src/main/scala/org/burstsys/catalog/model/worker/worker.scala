/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.model

import org.burstsys.catalog.api.BurstCatalogApiWorker
import org.burstsys.catalog.cannedDataLabel
import org.burstsys.catalog.persist.NamedCatalogEntity
import org.burstsys.fabric.topology.model.node.FabricNodeId
import org.burstsys.fabric.topology.model.node.worker.FabricWorker
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName}
import org.burstsys.vitals.properties.{VitalsLabelsMap, VitalsPropertyMap}

import scala.collection.immutable.Map
import scala.language.implicitConversions

package object worker {

  type CatalogWorker = BurstCatalogApiWorker.Proxy with NamedCatalogEntity with FabricWorker

  object CatalogWorker {
    def apply(
               pk: Long,
               moniker: String,
               nodeName: VitalsHostName,
               nodeAddress: VitalsHostAddress,
               siteFk: Long,
               cellFk: Option[Long] = None,
               labels: Option[Map[String, String]] = None,
               workerProperties: Map[String, String] = Map.empty
             ): BurstCatalogApiWorker =
      BurstCatalogApiWorker(
        pk = pk,
        moniker = moniker,
        nodeName = nodeName,
        nodeAddress = nodeAddress,
        siteFk = siteFk,
        cellFk = cellFk,
        labels = labels,
        workerProperties = workerProperties
      )
  }

  final case
  class CatalogSqlWorker(_underlying_BurstCatalogApiWorker: BurstCatalogApiWorker)
    extends BurstCatalogApiWorker.Proxy with NamedCatalogEntity with FabricWorker {

    override def nodeMoniker: String = _underlying_BurstCatalogApiWorker.nodeMoniker

    override def nodeName: VitalsHostName = _underlying_BurstCatalogApiWorker.nodeName

    override def nodeAddress: VitalsHostAddress = _underlying_BurstCatalogApiWorker.nodeAddress

    override def nodeId: FabricNodeId = _underlying_BurstCatalogApiWorker.pk

  }

  implicit def workerApiToProxy(c: BurstCatalogApiWorker): CatalogWorker =
    CatalogSqlWorker(c)

  implicit def workerProxyToApi(c: CatalogWorker): BurstCatalogApiWorker =
    BurstCatalogApiWorker(
      pk = c.pk,
      moniker = c.moniker,
      nodeName = c.nodeName,
      nodeAddress = c.nodeAddress,
      siteFk = c.siteFk,
      cellFk = c.cellFk,
      labels = c.labels,
      workerProperties = c.workerProperties
    )

  final case
  class CatalogJsonWorker(
                           pk: Long,
                           moniker: String,
                           nodeName: VitalsHostName,
                           nodeAddress: VitalsHostAddress,
                           siteFk: Long,
                           cellFk: Option[Long],
                           labels: Map[String, String],
                           workerProperties: Map[String, String]
                         )

  implicit def workerProxyToJson(c: CatalogWorker): CatalogJsonWorker =
    CatalogJsonWorker(
      pk = c.pk,
      moniker = c.moniker,
      nodeName = c.nodeName,
      nodeAddress = c.nodeAddress,
      siteFk = c.siteFk,
      cellFk = c.cellFk,
      labels = if (c.labels.isDefined) c.labels.get.toMap else Map.empty,
      workerProperties = c.workerProperties.toMap
    )

  implicit def workerJsonToProxy(c: CatalogJsonWorker): CatalogWorker =
    BurstCatalogApiWorker(
      pk = c.pk,
      moniker = c.moniker,
      nodeName = c.nodeName,
      nodeAddress = c.nodeAddress,
      siteFk = c.siteFk,
      cellFk = c.cellFk,
      labels = if (c.labels.isEmpty) None else Some(c.labels),
      workerProperties = c.workerProperties
    )

  final case
  class CatalogCannedWorker(
                             moniker: String,
                             nodeName: VitalsHostName,
                             nodeAddress: VitalsHostAddress,
                             siteMoniker: String,
                             cellMoniker: Option[String],
                             workerProperties: VitalsPropertyMap = Map.empty,
                             var labels: Option[VitalsLabelsMap] = None
                           ) {
    labels = Some(Map(cannedDataLabel -> "true"))
  }

}
