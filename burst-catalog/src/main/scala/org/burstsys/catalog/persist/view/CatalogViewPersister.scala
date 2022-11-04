/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.view

import org.burstsys.catalog.api._
import org.burstsys.catalog.model.view._
import org.burstsys.catalog.persist.ScopedUdkCatalogEntityPersister
import org.burstsys.catalog.persist.domain.CatalogDomainPersister
import org.burstsys.fabric
import org.burstsys.relate.RelateExceptions.BurstUnknownPrimaryKeyException
import org.burstsys.relate._
import org.burstsys.relate.dialect.RelateDerbyDialect
import org.burstsys.relate.dialect.RelateMySqlDialect
import org.burstsys.relate.dialect.SelectLockLevel.UpdateLock
import org.burstsys.vitals.properties._
import org.joda.time.DateTime
import scalikejdbc.WrappedResultSet
import scalikejdbc._

import scala.language.implicitConversions
import scala.util.Try

//noinspection SqlNoDataSourceInspection
final case
class CatalogViewPersister(service: RelateService) extends ScopedUdkCatalogEntityPersister[CatalogView] {

  override def sqlTableName: String = "burst_catalog_view"

  final val domainFkColumn = "domain_fk"
  final val generationClockColumn = "generation_clock"
  final val storePropertiesColumn = "store_properties"
  final val viewMotifColumn = "view_motif"
  final val viewPropertiesColumn = "view_properties"
  final val schemaColumn = "schema_name"
  final val createTSColumn = "create_timestamp"
  final val modifyTSColumn = "modify_timestamp"
  final val accessTSColumn = "access_timestamp"

  override val columnNames: Seq[String] = Seq(
    entityPkColumn, labelsColumn, monikerColumn, domainFkColumn, storePropertiesColumn, viewMotifColumn, viewPropertiesColumn,
    schemaColumn, createTSColumn, modifyTSColumn, accessTSColumn, generationClockColumn, udkColumn
  )

  override def scopeTableName: String = CatalogDomainPersister.tableName

  override def scopeFkField: String = domainFkColumn

  override def resultToEntity(rs: WrappedResultSet): CatalogSqlView = CatalogSqlView(
    BurstCatalogApiView(
      pk = rs.long(entityPkColumn),
      moniker = rs.string(monikerColumn),
      labels = stringToOptionalPropertyMap(rs.string(labelsColumn)),
      domainFk = rs.long(domainFkColumn),
      generationClock = rs.long(generationClockColumn),
      storeProperties = stringToPropertyMap(rs.string(storePropertiesColumn)),
      viewMotif = rs.string(viewMotifColumn),
      viewProperties = stringToPropertyMap(rs.string(viewPropertiesColumn)),
      schemaName = rs.string(schemaColumn),
      createTimestamp = timestampColToMillis(rs, createTSColumn),
      modifyTimestamp = timestampColToMillis(rs, modifyTSColumn),
      accessTimestamp = timestampColToMillis(rs, accessTSColumn),
      udk = rs.stringOpt(udkColumn)
    )
  )

  override protected def createTableSql: TableCreateSql = service.dialect match {
    case RelateMySqlDialect => mysqlCreateTableSql
    case RelateDerbyDialect => derbyCreateTableSql
  }

  override def insertEntitySql(entity: CatalogView): WriteSql = {
    sql"""
     INSERT INTO  ${this.table}
       (
        ${this.column.labels},
        ${this.column.domainFk},
        ${this.column.generationClock},
        ${this.column.moniker},
        ${this.column.storeProperties},
        ${this.column.schemaName},
        ${this.column.viewMotif},
        ${this.column.viewProperties},
        ${this.column.udk}
      )
     VALUES
       (
          {labels},
          {domainFk},
          {generationClock},
          {moniker},
          {storeProperties},
          {schemaName},
          {viewMotif},
          {viewProperties},
          {udk}
        )
     """.bindByName(
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("domainFk") -> entity.domainFk,
      Symbol("generationClock") -> System.currentTimeMillis(),
      Symbol("moniker") -> entity.moniker,
      Symbol("storeProperties") -> propertyMapToString(entity.storeProperties),
      Symbol("schemaName") -> entity.schemaName,
      Symbol("viewMotif") -> entity.viewMotif,
      Symbol("viewProperties") -> propertyMapToString(entity.viewProperties),
      Symbol("udk") -> entity.udk
    )
  }

  /**
    * This method should not be called directly, upsertEntity is the only
    * intended method for updating catalog views.
    * @param entity the entity to generate update SQL for
    * @return SQL to update this entity in the database
    */
  override def updateEntityByPkSql(entity: CatalogView): WriteSql = {
    val (newClock, newProperties) = updateGenerationClock(entity)
    sql"""
     UPDATE  ${this.table}
     SET
       ${this.column.domainFk} = {domainFk},
       ${this.column.generationClock} = {generationClock},
       ${this.column.moniker} = {moniker},
       ${this.column.labels} = {labels},
       ${this.column.storeProperties} = {storeProperties},
       ${this.column.schemaName} = {schemaName},
       ${this.column.viewMotif} = {viewMotif},
       ${this.column.viewProperties} = {viewProperties},
       ${this.column.udk} = {udk}
     WHERE
       ${this.column.pk} = {pk}
     """.bindByName(
      Symbol("pk") -> entity.pk,
      Symbol("moniker") -> entity.moniker,
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("domainFk") -> entity.domainFk,
      Symbol("generationClock") -> newClock,
      Symbol("storeProperties") -> propertyMapToString(entity.storeProperties),
      Symbol("schemaName") -> entity.schemaName,
      Symbol("viewMotif") -> entity.viewMotif,
      Symbol("viewProperties") -> propertyMapToString(newProperties),
      Symbol("udk") -> entity.udk
    )
  }

  override def updatesForEntityByUdk(proposed: CatalogView, stored: CatalogView): WriteSql = {
    val entity = fieldsForUpdate(proposed, stored)
    val (generationClock, viewProperties) = updateGenerationClock(entity)
    sql"""
     UPDATE  ${this.table}
     SET
       ${this.column.domainFk} = {domainFk},
       ${this.column.generationClock} = {generationClock},
       ${this.column.moniker} = {moniker},
       ${this.column.labels} = {labels},
       ${this.column.storeProperties} = {storeProperties},
       ${this.column.schemaName} = {schemaName},
       ${this.column.viewMotif} = {viewMotif},
       ${this.column.viewProperties} = {viewProperties}
     WHERE
       ${this.column.udk} = {udk}
     """.bindByName(
      Symbol("moniker") -> entity.moniker,
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("domainFk") -> entity.domainFk,
      Symbol("generationClock") -> generationClock,
      Symbol("storeProperties") -> propertyMapToString(entity.storeProperties),
      Symbol("schemaName") -> entity.schemaName,
      Symbol("viewMotif") -> entity.viewMotif,
      Symbol("viewProperties") -> propertyMapToString(viewProperties),
      Symbol("udk") -> entity.udk
    )
  }

  override def updatesForEntityByPk(proposed: CatalogView, stored: CatalogView): WriteSql = {
    val entity = fieldsForUpdate(proposed, stored)
    val (generationClock, viewProperties) = updateGenerationClock(entity)
    sql"""
     UPDATE  ${this.table}
     SET
        ${this.column.domainFk} = {domainFk},
        ${this.column.generationClock} = {generationClock},
        ${this.column.moniker} = {moniker},
        ${this.column.labels} = {labels},
        ${this.column.storeProperties} = {storeProperties},
        ${this.column.schemaName} = {schemaName},
        ${this.column.viewMotif} = {viewMotif},
        ${this.column.viewProperties} = {viewProperties},
        ${this.column.udk} = {udk}
     WHERE
        ${this.column.pk} = {pk}
     """.bindByName(
      Symbol("moniker") -> entity.moniker,
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("domainFk") -> entity.domainFk,
      Symbol("generationClock") -> generationClock,
      Symbol("storeProperties") -> propertyMapToString(entity.storeProperties),
      Symbol("schemaName") -> entity.schemaName,
      Symbol("viewMotif") -> entity.viewMotif,
      Symbol("viewProperties") -> propertyMapToString(viewProperties),
      Symbol("udk") -> entity.udk,
      Symbol("pk") -> entity.pk
    )
  }

  private def fieldsForUpdate(proposed: CatalogView, stored: CatalogView): CatalogView = {
    val pk = stored.pk
    val moniker = if (proposed.moniker != "") proposed.moniker else stored.moniker
    val domainFk = stored.domainFk
    val schemaName = if (proposed.schemaName != "") proposed.schemaName else stored.schemaName
    val (generationClock, proposedWithNewGenClock) = updateGenerationClock(proposed)
    val storeProperties = (stored.storeProperties ++ proposed.storeProperties).toMap
    val viewMotif = if (proposed.viewMotif != "") proposed.viewMotif else stored.viewMotif
    val viewProperties = (stored.viewProperties ++ proposedWithNewGenClock).toMap
    val udk = proposed.udk.orElse(stored.udk)
    val labels: Option[Map[String, String]] = (proposed.labels, stored.labels) match {
      case (Some(proposedLabels), Some(storedLabels)) => Some((storedLabels ++ proposedLabels).toMap)
      case (None, Some(labels)) => Some(labels.toMap)
      case (Some(labels), None) => Some(labels.toMap)
      case (None, None) => None
    }

    CatalogView(pk, moniker, domainFk, schemaName, generationClock, storeProperties, viewMotif, viewProperties, labels, udk)
  }

  private def updateGenerationClock(entity: CatalogView): (Long, VitalsPropertyMap) = {
    val now = System.currentTimeMillis()
    (now, entity.viewProperties.concat(Array(fabric.wave.metadata.ViewEarliestLoadAtProperty -> s"$now")))
  }

  def updateGenClockAtomically(entity: CatalogView, minNewGenClock: Long)(implicit session: DBSession): CatalogView = {
    /* write lock the item before checking */
    findEntityByPk(entity.pk, lockLevel = UpdateLock) match {
      // if the provided pk doesn't exist we can do nothing
      case None => throw BurstUnknownPrimaryKeyException(entity.pk)
      case Some(stored) =>
        if (stored.generationClock < minNewGenClock) {
           updateGenClockForView(stored)
        } else
          stored
    }
  }

  def recordViewLoad(pk: RelatePk, viewProperties: VitalsPropertyMap)(implicit session: DBSession): Unit = {
    sql"""
     UPDATE  ${this.table} SET
       ${this.column.accessTimestamp} = {ts},
       ${this.column.viewProperties} = {viewProperties}
     WHERE
       ${this.column.pk} = {pk}
     """.bindByName(
      Symbol("pk") -> pk,
      Symbol("ts") -> DateTime.now,
      Symbol("viewProperties") -> propertyMapToString(viewProperties)
    ).update().apply()
  }

  def deleteViewsForDomain(domainFk: RelatePk)(implicit session: DBSession): Unit = {
    sql"""
    DELETE FROM ${this.table}
        WHERE ${this.column.domainFk} = {domainFk}
    """.bindByName(Symbol("domainFk") -> domainFk).update().apply()
  }

  def updateGenClockForViewsInDomain(domainFk: RelatePk)(implicit session: DBSession): Unit = {
    sql"UPDATE ${this.table} SET ${this.column.generationClock} = {clock} WHERE ${this.column.domainFk} = {domainFk}".bindByName(
      Symbol("domainFk") -> domainFk,
      Symbol("clock") -> System.currentTimeMillis()
    ).map(resultToEntity).update().apply()

  }

  def updateGenClockForView(entity: CatalogView)(implicit session: DBSession): CatalogView = {
    val (newClock, newProperties) = updateGenerationClock(entity)
    sql"""
       UPDATE ${this.table} SET
          ${this.column.generationClock} = {clock},
          ${this.column.viewProperties} = {viewProperties}
       WHERE ${this.column.pk} = {viewPk}
    """.bindByName(
      Symbol("viewPk") -> entity.pk,
      Symbol("clock") -> newClock,
      Symbol("viewProperties") -> propertyMapToString(newProperties)
    ).map(resultToEntity).update().apply()
    entity.copy(generationClock = newClock, viewProperties = newProperties)
  }

  def allViewsForDomain(domainFk: RelatePk, limit: Option[Int])(implicit session: DBSession): List[CatalogView] = {
    sql"""SELECT * FROM ${this.table}
         WHERE ${this.column.domainFk} = {domainFk} ${service.dialect.limitClause(limit)}
      """.bindByName(Symbol("domainFk") -> domainFk).map(resultToEntity).list().apply()
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Table Schema
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def mysqlCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
        ${this.column.pk} BIGINT NOT NULL AUTO_INCREMENT,
        ${this.column.moniker} VARCHAR(255) NOT NULL,
        ${this.column.labels} TEXT,
        ${this.column.domainFk} BIGINT,
        ${this.column.generationClock} BIGINT DEFAULT 0,
        ${this.column.udk} VARCHAR(255),
        ${this.column.schemaName} VARCHAR(255) NOT NULL,
        ${this.column.viewMotif} TEXT,
        ${this.column.storeProperties} TEXT,
        ${this.column.viewProperties} TEXT,
        ${this.column.modifyTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
        ${this.column.createTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        ${this.column.accessTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (${this.column.pk}),
        UNIQUE (${this.column.udk}, ${this.column.domainFk})
     ) ENGINE=InnoDb DEFAULT CHARSET=utf8
     """

  def derbyCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
      ${this.column.pk} BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
      ${this.column.domainFk} BIGINT,
      ${this.column.generationClock} BIGINT DEFAULT 0,
      ${this.column.moniker} VARCHAR(255) NOT NULL UNIQUE,
      ${this.column.udk} VARCHAR(255),
      ${this.column.labels} VARCHAR(32672),
      ${this.column.schemaName} VARCHAR(255) NOT NULL,
      ${this.column.viewMotif} VARCHAR(32672),
      ${this.column.storeProperties} VARCHAR(32672),
      ${this.column.viewProperties} VARCHAR(32672),
      ${this.column.modifyTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      ${this.column.createTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      ${this.column.accessTimestamp} TIMESTAMP,

      UNIQUE (${this.column.udk}, ${this.column.domainFk})
    )
    """

}
