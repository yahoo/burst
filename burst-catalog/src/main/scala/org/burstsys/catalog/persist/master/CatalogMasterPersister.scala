/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.master

import org.burstsys.catalog.api._
import org.burstsys.catalog.model.master._
import org.burstsys.catalog.persist.NamedCatalogEntityPersister
import org.burstsys.relate._
import org.burstsys.relate.dialect.{RelateDerbyDialect, RelateMySqlDialect}
import org.burstsys.vitals.properties._
import scalikejdbc.{WrappedResultSet, _}

final case
class CatalogMasterPersister(service: RelateService) extends NamedCatalogEntityPersister[CatalogMaster]
  with CatalogDerbyMasterSql with CatalogMySqlMasterSql {

  override val sqlTableName: String = "burst_catalog_master"

  private val masterPropertiesColumn = "master_properties"
  private val nodeNameColumn = "node_name"
  private val nodeAddressColumn = "node_address"
  private val masterPortColumn = "master_port"
  private val siteFkColumn = "site_fk"
  private val cellFkColumn = "cell_fk"

  override def columnNames: Seq[String] =
    Seq(entityPkColumn, labelsColumn, monikerColumn, masterPropertiesColumn, nodeNameColumn, nodeAddressColumn, masterPortColumn, siteFkColumn, cellFkColumn)

  override def resultToEntity(rs: WrappedResultSet): CatalogSqlMaster = CatalogSqlMaster(
    BurstCatalogApiMaster(
      pk = rs.long(entityPkColumn),
      moniker = rs.string(monikerColumn),
      nodeName = rs.string(nodeNameColumn),
      nodeAddress = rs.string(nodeAddressColumn),
      masterPort = rs.int(masterPortColumn),
      siteFk = rs.long(siteFkColumn),
      cellFk = Option(rs.long(cellFkColumn)),
      labels = stringToOptionalPropertyMap(rs.string(labelsColumn)),
      masterProperties = stringToPropertyMap(rs.string(masterPropertiesColumn))
    )
  )

  override def createTableSql: TableCreateSql = service.dialect match {
    case RelateMySqlDialect => mysqlCreateTableSql
    case RelateDerbyDialect => derbyCreateTableSql
  }

  override def insertEntitySql(entity: CatalogMaster): WriteSql = {
    sql"""
     INSERT INTO  ${this.table}
       (
            ${this.column.nodeName},
            ${this.column.nodeAddress},
            ${this.column.masterPort},
            ${this.column.siteFk},
            ${this.column.cellFk},
            ${this.column.labels},
            ${this.column.moniker},
            ${this.column.masterProperties}
      )
     VALUES
       (
          {nodeName},
          {nodeAddress},
          {masterPort},
          {siteFk},
          {cellFk},
          {labels},
          {moniker},
          {masterProperties}
       )
     """.bindByName(
      'labels -> optionalPropertyMapToString(entity.labels),
      'nodeName -> entity.nodeName,
      'nodeAddress -> entity.nodeAddress,
      'masterPort -> entity.masterPort,
      'siteFk -> entity.siteFk,
      'cellFk -> (if (entity.cellFk.isDefined) entity.cellFk.get else null),
      'moniker -> entity.moniker,
      'masterProperties -> propertyMapToString(entity.masterProperties)
    )
  }

  override def updateEntityByPkSql(entity: CatalogMaster): WriteSql = {
    sql"""
     UPDATE  ${this.table}
     SET
       ${this.column.nodeName} = {nodeName},
       ${this.column.nodeAddress} = {nodeAddress},
       ${this.column.masterPort} = {masterPort},
       ${this.column.siteFk} = {siteFk},
       ${this.column.cellFk} = {cellFk},
       ${this.column.moniker} = {moniker},
       ${this.column.labels} = {labels},
       ${this.column.masterProperties} = {masterProperties}
     WHERE
       ${this.column.pk} = {pk}
     """.bindByName(
      'pk -> entity.pk,
      'nodeName -> entity.nodeName,
      'nodeAddress -> entity.nodeAddress,
      'masterPort -> entity.masterPort,
      'siteFk -> entity.siteFk,
      'cellFk -> (if (entity.cellFk.isDefined) entity.cellFk.get else null),
      'moniker -> entity.moniker,
      'labels -> optionalPropertyMapToString(entity.labels),
      'masterProperties -> propertyMapToString(entity.masterProperties)
    )
  }

  def deleteMastersForSite(siteFk: RelatePk)(implicit session: DBSession): Unit = {
    sql"""
    DELETE FROM ${this.table}
        WHERE ${this.column.siteFk} = {siteFk}
    """.bindByName('siteFk -> siteFk).update().apply()
  }

  def deleteMastersForCell(cellFk: RelatePk)(implicit session: DBSession): Unit = {
    sql"""
    DELETE FROM ${this.table}
        WHERE ${this.column.cellFk} = {cellFk}
    """.bindByName('cellFk -> cellFk).update().apply()
  }

  def allMastersForSite(siteFk: RelatePk, limit: Option[Int])(implicit session: DBSession): List[CatalogMaster] = {
    sql""" SELECT * FROM ${this.table}
           WHERE ${this.column.siteFk} = {siteFk} ${service.dialect.limitClause(limit)}
       """.bindByName('siteFk -> siteFk).map(resultToEntity).list().apply()
  }

  def allMastersForCell(cellFk: RelatePk, limit: Option[Int])(implicit session: DBSession): List[CatalogMaster] = {
    sql""" SELECT * FROM ${this.table}
         WHERE ${this.column.cellFk} = {cellFk} ${service.dialect.limitClause(limit)}
      """.bindByName('cellFk -> cellFk).map(resultToEntity).list().apply()
  }
}
