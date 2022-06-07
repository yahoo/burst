/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.cell

import org.burstsys.catalog.api._
import org.burstsys.catalog.model.cell._
import org.burstsys.catalog.persist.NamedCatalogEntityPersister
import org.burstsys.relate._
import org.burstsys.relate.dialect.{RelateDerbyDialect, RelateMySqlDialect}
import org.burstsys.vitals.properties._
import scalikejdbc.{WrappedResultSet, _}

final case
class CatalogCellPersister(service: RelateService) extends NamedCatalogEntityPersister[CatalogCell]
  with CatalogDerbyCellSql with CatalogMySqlCellSql {

  override
  val sqlTableName: String = "burst_catalog_cell"

  private val siteFkColumn = "site_fk"
  private val cellPropertiesColumn = "cell_properties"

  override def columnNames: Seq[String] = Seq(entityPkColumn, labelsColumn, monikerColumn, siteFkColumn, cellPropertiesColumn)

  override def resultToEntity(rs: WrappedResultSet): CatalogSqlCell = CatalogSqlCell(
    BurstCatalogApiCell(
      pk = rs.long(entityPkColumn),
      moniker = rs.string(monikerColumn),
      labels = stringToOptionalPropertyMap(rs.string(labelsColumn)),
      siteFk = rs.long(siteFkColumn),
      cellProperties = stringToPropertyMap(rs.string(cellPropertiesColumn))
    )
  )

  override def createTableSql: TableCreateSql = service.dialect match {
    case RelateMySqlDialect => mysqlCreateTableSql
    case RelateDerbyDialect => derbyCreateTableSql
  }

  override def insertEntitySql(entity: CatalogCell): WriteSql = {
    sql"""
     INSERT INTO  ${this.table}
       (
            ${this.column.labels},
            ${this.column.moniker},
            ${this.column.siteFk},
            ${this.column.cellProperties}
      )
     VALUES
       (
          {labels},
          {moniker},
          {siteFk},
          {cellProperties}
       )
     """.bindByName(
      'labels -> optionalPropertyMapToString(entity.labels),
      'moniker -> entity.moniker,
      'siteFk -> entity.siteFk,
      'cellProperties -> propertyMapToString(entity.cellProperties)
    )
  }

  override def updateEntityByPkSql(entity: CatalogCell): WriteSql = {
    sql"""
     UPDATE  ${this.table}
     SET
       ${this.column.moniker} = {moniker},
       ${this.column.labels} = {labels},
       ${this.column.siteFk} = {siteFk},
       ${this.column.cellProperties} = {cellProperties}
     WHERE
       ${this.column.pk} = {pk}
     """.bindByName(
      'pk -> entity.pk,
      'moniker -> entity.moniker,
      'labels -> optionalPropertyMapToString(entity.labels),
      'siteFk -> entity.siteFk,
      'cellProperties -> propertyMapToString(entity.cellProperties)
    )
  }

  def deleteCellsForSite(siteFk: RelatePk)(implicit session: DBSession): Unit = {
    sql"""DELETE FROM ${this.table}
        WHERE ${this.column.siteFk} = {siteFk}
    """.bindByName('siteFk -> siteFk).update().apply()
  }

  def allCellsForSite(siteFk: RelatePk, limit: Option[Int])(implicit session: DBSession): List[CatalogCell] = {
    sql""" SELECT * FROM ${this.table}
           WHERE ${this.column.siteFk} = {siteFk} ${service.dialect.limitClause(limit)}
       """.bindByName('siteFk -> siteFk).map(resultToEntity).list().apply()
  }
}
