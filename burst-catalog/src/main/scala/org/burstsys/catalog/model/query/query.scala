/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.model

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.burstsys.catalog.api.BurstCatalogApiQuery
import org.burstsys.catalog.api.BurstCatalogApiQueryLanguageType
import org.burstsys.catalog.cannedDataLabel
import org.burstsys.catalog.persist.NamedCatalogEntity
import org.burstsys.vitals.json.VitalsJsonSanatizers._
import org.burstsys.vitals.properties.VitalsLabelsMap
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.collection.immutable.Map
import scala.language.implicitConversions

package object query {

  type CatalogQuery = BurstCatalogApiQuery.Proxy with NamedCatalogEntity

  object BurstCatalogQuery {
    def apply(pk: Long, moniker: String, languageType: BurstCatalogApiQueryLanguageType, source: String,
              labels: Option[VitalsPropertyMap] = None): BurstCatalogApiQuery =
      BurstCatalogApiQuery(pk, moniker, languageType, source, labels)
  }

  final case
  class CatalogSqlQuery(_underlying_BurstCatalogApiQuery: BurstCatalogApiQuery)
    extends BurstCatalogApiQuery.Proxy with NamedCatalogEntity

  implicit def queryApiToProxy(a: BurstCatalogApiQuery): CatalogQuery =
    CatalogSqlQuery(a)

  implicit def queryProxyToApi(a: CatalogQuery): BurstCatalogApiQuery =
    BurstCatalogApiQuery(a.pk, a.moniker, a.languageType, a.source, a.labels)

  final case
  class CatalogJsonQuery(pk: Long,
                         @JsonSerialize(using = classOf[Values]) moniker: String,
                         @JsonSerialize(keyUsing = classOf[Keys], contentUsing = classOf[Values]) labels: Map[String, String],
                         @JsonSerialize(using = classOf[Values]) languageType: String,
                         source: String)

  implicit def queryProxyToJson(c: CatalogQuery): CatalogJsonQuery =
    CatalogJsonQuery(c.pk, c.moniker, if (c.labels.isDefined) c.labels.get.toMap else Map.empty, c.languageType.name, c.source)

  implicit def queryJsonToProxy(c: CatalogJsonQuery): CatalogQuery =
    BurstCatalogQuery(c.pk, c.moniker, BurstCatalogApiQueryLanguageType.valueOf(c.languageType).get, c.source,
      if (c.labels.isEmpty) None else Some(c.labels))

  /**
   * Canned representation of query
   */
  final case
  class CatalogCannedQuery(moniker: String,
                           languageType: BurstCatalogApiQueryLanguageType,
                           source: String,
                           var labels: Option[VitalsLabelsMap] = None) {
    labels = labels.orElse(Some(Map(cannedDataLabel -> "true")))
  }

}
