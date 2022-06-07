/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.site

import org.burstsys.catalog.api._
import org.burstsys.catalog.model.site._
import org.burstsys.catalog.persist.NamedCatalogEntityPersister
import org.burstsys.relate._
import org.burstsys.relate.dialect.{RelateDerbyDialect, RelateMySqlDialect}
import org.burstsys.vitals.properties._
import scalikejdbc.{WrappedResultSet, _}

final case
class CatalogSitePersister(service: RelateService) extends NamedCatalogEntityPersister[CatalogSite]
  with CatalogDerbySiteSql with CatalogMySqlSiteSql {

  override val sqlTableName: String = "burst_catalog_site"

  private val sitePropertiesColumn = "site_properties"

  override def columnNames: Seq[String] = Seq(entityPkColumn, labelsColumn, monikerColumn, sitePropertiesColumn)

  override def resultToEntity(rs: WrappedResultSet): CatalogSqlSite = CatalogSqlSite(
    BurstCatalogApiSite(
      pk = rs.long(entityPkColumn),
      moniker = rs.string(monikerColumn),
      labels = stringToOptionalPropertyMap(rs.string(labelsColumn)),
      siteProperties = stringToPropertyMap(rs.string(sitePropertiesColumn))
    )
  )

  override def createTableSql: TableCreateSql = service.dialect match {
    case RelateMySqlDialect => mysqlCreateTableSql
    case RelateDerbyDialect => derbyCreateTableSql
  }

  override def insertEntitySql(entity: CatalogSite): WriteSql = {
    sql"""
     INSERT INTO  ${this.table}
       (
            ${this.column.labels},
            ${this.column.moniker},
            ${this.column.siteProperties}
      )
     VALUES
       (
          {labels},
          {moniker},
          {siteProperties}
       )
     """.bindByName(
      'labels -> optionalPropertyMapToString(entity.labels),
      'moniker -> entity.moniker,
      'siteProperties -> propertyMapToString(entity.siteProperties)
    )
  }

  override def updateEntityByPkSql(entity: CatalogSite): WriteSql = {
    sql"""
     UPDATE  ${this.table}
     SET
       ${this.column.moniker} = {moniker},
       ${this.column.labels} = {labels},
       ${this.column.siteProperties} = {siteProperties}
     WHERE
       ${this.column.pk} = {pk}
     """.bindByName(
      'pk -> entity.pk,
      'moniker -> entity.moniker,
      'labels -> optionalPropertyMapToString(entity.labels),
      'siteProperties -> propertyMapToString(entity.siteProperties)
    )
  }
}
