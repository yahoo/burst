/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.model

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.burstsys.catalog.api.BurstCatalogApiDomain
import org.burstsys.catalog.cannedDataLabel
import org.burstsys.catalog.persist.UdkCatalogEntity
import org.burstsys.fabric.wave.metadata.model.FabricDomainKey
import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.relate.RelateEntity
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.json.VitalsJsonSanatizers._
import org.burstsys.vitals.properties.VitalsLabelsMap
import org.burstsys.vitals.properties.VitalsPropertyKey
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.collection.immutable.Map
import scala.language.implicitConversions

package object domain {

  type CatalogDomain = BurstCatalogApiDomain.Proxy with UdkCatalogEntity with FabricDomain

  object CatalogDomain {
    def apply(
               pk: Long, moniker: String, domainProperties: Map[String, String] = Map(),
               udk: Option[String] = None, labels: Option[Map[String, String]] = None
             ): BurstCatalogApiDomain =
      BurstCatalogApiDomain(pk, moniker, domainProperties, udk, labels)

    def apply(
               pk: Long, moniker: String, domainProperties: Map[String, String],
               udk: String, labels: Map[String, String]
             ): CatalogDomain =
      domainApiToProxy(BurstCatalogApiDomain(pk, moniker, domainProperties, Some(udk), Some(labels)))

    def apply(d: FabricDomain, moniker: String, udk: Option[String], labels: Option[Map[String, String]]): CatalogDomain =
      BurstCatalogApiDomain(d.domainKey, moniker, d.domainProperties, udk, labels)

    final val DomainBeastProjectIdProperty: VitalsPropertyKey = "beast.domain.project.id"
    final val DomainProjectIdProperty: VitalsPropertyKey = "project_id"
  }

  final case
  class CatalogSqlDomain(_underlying_BurstCatalogApiDomain: BurstCatalogApiDomain)
    extends BurstCatalogApiDomain.Proxy with UdkCatalogEntity with FabricDomain {
    override def domainKey: FabricDomainKey = pk

    /**
     * @param storedEntity the version of this entity that exists in the database
     * @return true if this version of the entity should be written tot the database
     */
    override def shouldUpdate(storedEntity: RelateEntity): Boolean = {
      storedEntity match {
        case fromDB: CatalogDomain =>
          val labelsDiffer = labels.isDefined && labels != fromDB.labels
          val monikerDiffers = moniker != "" && moniker != fromDB.moniker
          val udkDiffers = udk != fromDB.udk
          val propsDiffer = domainProperties != fromDB.domainProperties

          labelsDiffer || monikerDiffers || udkDiffers || propsDiffer
        case _ => throw VitalsException(s"Something has gone horribly wrong. Expected a ${this.getClass} but found a ${storedEntity.getClass}")
      }
    }
  }

  implicit def domainApiToProxy(c: BurstCatalogApiDomain): CatalogDomain = CatalogSqlDomain(c)

  implicit def domainProxyToApi(c: CatalogDomain): BurstCatalogApiDomain =
    BurstCatalogApiDomain(c.pk, c.moniker, c.domainProperties, c.udk, c.labels)

  final case
  class CatalogJsonDomain(pk: Long,
                          @JsonSerialize(using = classOf[Values]) moniker: String,
                          @JsonSerialize(keyUsing = classOf[Keys], contentUsing = classOf[Values]) labels: Map[String, String],
                          @JsonSerialize(using = classOf[Values]) udk: String,
                          @JsonSerialize(keyUsing = classOf[Keys], contentUsing = classOf[Values]) domainProperties: Map[String, String])

  implicit def domainProxyToJson(c: CatalogDomain): CatalogJsonDomain =
    CatalogJsonDomain(c.pk, c.moniker, if (c.labels.isDefined) c.labels.get.toMap else Map.empty,
      if (c.udk.isDefined) c.udk.get else null, c.domainProperties.toMap)

  implicit def domainJsonToProxy(c: CatalogJsonDomain): CatalogDomain = BurstCatalogApiDomain(
    c.pk, c.moniker,
    if (c.domainProperties != null) c.domainProperties else Map.empty,
    Option(c.udk),
    if (c.labels == null || c.labels.isEmpty) None else Some(c.labels)
  )

  final case
  class CatalogCannedDomain(moniker: String, domainProperties: VitalsPropertyMap = Map.empty,
                            udk: Option[String] = None, var labels: Option[VitalsLabelsMap] = None) {
    labels = labels.orElse(Some(Map(cannedDataLabel -> "true")))
  }


}
