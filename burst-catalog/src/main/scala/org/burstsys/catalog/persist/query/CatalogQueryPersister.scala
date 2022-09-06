/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.query

import org.burstsys.catalog.api._
import org.burstsys.catalog.model.query._
import org.burstsys.catalog.persist.NamedCatalogEntityPersister
import org.burstsys.relate._
import org.burstsys.relate.dialect.{RelateDerbyDialect, RelateMySqlDialect}
import org.burstsys.vitals.properties._
import scalikejdbc.{WrappedResultSet, _}

final case
class CatalogQueryPersister(service: RelateService) extends NamedCatalogEntityPersister[CatalogQuery]
  with CatalogDerbyQuerySql with CatalogMySqlQuerySql {

  override val sqlTableName: String = "burst_catalog_query"

  private val languageColumn = "language_type"
  private val sourceColumn = "source"

  override def columnNames: Seq[String] = Seq(entityPkColumn, labelsColumn, monikerColumn, languageColumn, sourceColumn)

  override def resultToEntity(rs: WrappedResultSet): CatalogSqlQuery = CatalogSqlQuery(
    BurstCatalogApiQuery(
      pk = rs.long(entityPkColumn),
      moniker = rs.string(monikerColumn),
      labels = stringToOptionalPropertyMap(rs.string(labelsColumn)),
      languageType = BurstCatalogApiQueryLanguageType.valueOf(rs.string(languageColumn)).get,
      source = rs.string(sourceColumn)
    )
  )

  override def createTableSql: TableCreateSql = service.dialect match {
    case RelateMySqlDialect => mysqlCreateTableSql
    case RelateDerbyDialect => derbyCreateTableSql
  }

  override def insertEntitySql(entity: CatalogQuery): WriteSql = {
    sql"""
     INSERT INTO  ${this.table}
       (${this.column.labels}, ${this.column.moniker}, ${this.column.languageType}, ${this.column.source})
     VALUES
       (
          {labels},
          {moniker},
          {languageType},
          {source}
        )
     """.bindByName(
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("moniker") -> entity.moniker,
      Symbol("languageType") -> entity.languageType.name,
      Symbol("source") -> entity.source
    )
  }

  override def updateEntityByPkSql(entity: CatalogQuery): WriteSql = {
    sql"""
     UPDATE  ${this.table}
     SET
       ${this.column.moniker} = {moniker},
       ${this.column.labels} = {labels},
       ${this.column.languageType} = {languageType},
       ${this.column.source} = {source}
     WHERE
       ${this.column.pk} = {pk}
     """.bindByName(
      Symbol("pk") -> entity.pk,
      Symbol("moniker") -> entity.moniker,
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("languageType") -> entity.languageType.name,
      Symbol("source") -> entity.source
    )
  }

}
