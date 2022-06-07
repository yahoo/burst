/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.worker

import org.burstsys.catalog.api._
import org.burstsys.catalog.model.worker._
import org.burstsys.catalog.persist.NamedCatalogEntityPersister
import org.burstsys.relate._
import org.burstsys.relate.dialect.{RelateDerbyDialect, RelateMySqlDialect}
import org.burstsys.vitals.properties._
import scalikejdbc.{WrappedResultSet, _}

final case
class CatalogWorkerPersister(service: RelateService) extends NamedCatalogEntityPersister[CatalogWorker]
  with CatalogDerbyWorkerSql with CatalogMySqlWorkerSql {

  override val sqlTableName: String = "burst_catalog_worker"

  private val workerPropertiesColumn = "worker_properties"
  private val nodeNameColumn = "node_name"
  private val nodeAddressColumn = "node_address"
  private val siteFkColumn = "site_fk"
  private val cellFkColumn = "cell_fk"

  override def columnNames: Seq[String] =
    Seq(entityPkColumn, labelsColumn, monikerColumn, workerPropertiesColumn, nodeNameColumn, nodeAddressColumn, siteFkColumn, cellFkColumn)

  override def resultToEntity(rs: WrappedResultSet): CatalogSqlWorker = CatalogSqlWorker(
    BurstCatalogApiWorker(
      pk = rs.long(entityPkColumn),
      moniker = rs.string(monikerColumn),
      nodeName = rs.string(nodeNameColumn),
      nodeAddress = rs.string(nodeAddressColumn),
      siteFk = rs.long(siteFkColumn),
      cellFk = Option(rs.long(cellFkColumn)),
      labels = stringToOptionalPropertyMap(rs.string(labelsColumn)),
      workerProperties = stringToPropertyMap(rs.string(workerPropertiesColumn))
    )
  )

  override def createTableSql: TableCreateSql = service.dialect match {
    case RelateMySqlDialect => mysqlCreateTableSql
    case RelateDerbyDialect => derbyCreateTableSql
  }

  override def insertEntitySql(entity: CatalogWorker): WriteSql = {
    sql"""
     INSERT INTO  ${this.table}
       (
            ${this.column.nodeName},
            ${this.column.nodeAddress},
            ${this.column.siteFk},
            ${this.column.cellFk},
            ${this.column.labels},
            ${this.column.moniker},
            ${this.column.workerProperties}
      )
     VALUES
       (
          {nodeName},
          {nodeAddress},
          {siteFk},
          {cellFk},
          {labels},
          {moniker},
          {workerProperties}
       )
     """.bindByName(
      'labels -> optionalPropertyMapToString(entity.labels),
      'nodeName -> entity.nodeName,
      'nodeAddress -> entity.nodeAddress,
      'siteFk -> entity.siteFk,
      'cellFk -> (if (entity.cellFk.isDefined) entity.cellFk.get else null),
      'moniker -> entity.moniker,
      'workerProperties -> propertyMapToString(entity.workerProperties)
    )
  }

  override def updateEntityByPkSql(entity: CatalogWorker): WriteSql = {
    sql"""
     UPDATE  ${this.table}
     SET
       ${this.column.nodeName} = {nodeName},
       ${this.column.nodeAddress} = {nodeAddress},
       ${this.column.siteFk} = {siteFk},
       ${this.column.cellFk} = {cellFk},
       ${this.column.moniker} = {moniker},
       ${this.column.labels} = {labels},
       ${this.column.workerProperties} = {workerProperties}
     WHERE
       ${this.column.pk} = {pk}
     """.bindByName(
      'pk -> entity.pk,
      'nodeName -> entity.nodeName,
      'nodeAddress -> entity.nodeAddress,
      'siteFk -> entity.siteFk,
      'cellFk -> (if (entity.cellFk.isDefined) entity.cellFk.get else null),
      'moniker -> entity.moniker,
      'labels -> optionalPropertyMapToString(entity.labels),
      'workerProperties -> propertyMapToString(entity.workerProperties)
    )
  }

  def deleteWorkersForSite(siteFk: RelatePk)(implicit session: DBSession): Unit = {
    sql"""
    DELETE FROM ${this.table}
        WHERE ${this.column.siteFk} = {siteFk}
    """.bindByName('siteFk -> siteFk).update().apply()
  }

  def deleteWorkersForCell(cellFk: RelatePk)(implicit session: DBSession): Unit = {
    sql"""
    DELETE FROM ${this.table}
        WHERE ${this.column.cellFk} = {cellFk}
    """.bindByName('cellFk -> cellFk).update().apply()
  }

  def allWorkersForSite(siteFk: RelatePk, limit: Option[Int])(implicit session: DBSession): List[CatalogWorker] = {
    sql"""SELECT * FROM ${this.table}
      WHERE ${this.column.siteFk} = {siteFk} ${service.dialect.limitClause(limit)}
      """.bindByName('siteFk -> siteFk).map(resultToEntity).list().apply()
  }

  def allWorkersForCell(cellFk: RelatePk, limit: Option[Int])(implicit session: DBSession): List[CatalogWorker] = {
    sql"""SELECT * FROM ${this.table}
         WHERE ${this.column.cellFk} = {cellFk} ${service.dialect.limitClause(limit)}
      """.bindByName('cellFk -> cellFk).map(resultToEntity).list().apply()
  }
}
