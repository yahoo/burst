/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.domain

import org.burstsys.catalog.api._
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.persist.UdkCatalogEntityPersister
import org.burstsys.relate._
import org.burstsys.relate.dialect.RelateDerbyDialect
import org.burstsys.relate.dialect.RelateMySqlDialect
import org.burstsys.vitals.properties._
import org.joda.time.DateTime
import scalikejdbc.WrappedResultSet
import scalikejdbc._

object CatalogDomainPersister {
  val tableName: String = "burst_catalog_domain"
}

final case class CatalogDomainPersister(service: RelateService) extends UdkCatalogEntityPersister[CatalogDomain] {

  override val sqlTableName: String = CatalogDomainPersister.tableName

  final val domainPropertiesColumn = "domain_properties"
  final val createTSColumn = "create_timestamp"
  final val modifyTSColumn = "modify_timestamp"

  override val columnNames: Seq[String] = Seq(
    entityPkColumn, monikerColumn, labelsColumn, domainPropertiesColumn,
    udkColumn, createTSColumn, modifyTSColumn
  )

  override def resultToEntity(rs: WrappedResultSet): CatalogSqlDomain = CatalogSqlDomain(
    BurstCatalogApiDomain(
      pk = rs.long(entityPkColumn),
      moniker = rs.string(monikerColumn),
      labels = stringToOptionalPropertyMap(rs.string(labelsColumn)),
      domainProperties = stringToPropertyMap(rs.string(domainPropertiesColumn)),
      udk = rs.stringOpt(udkColumn), // TODO fsg 180109 Does stringOpt return None for null?
      createTimestamp = timestampColToMillis(rs, createTSColumn),
      modifyTimestamp = timestampColToMillis(rs, modifyTSColumn),
    )
  )

  override protected def createTableSql: TableCreateSql = service.dialect match {
    case RelateMySqlDialect => mysqlCreateTableSql
    case RelateDerbyDialect => derbyCreateTableSql
  }

  override protected def insertEntityWithPkSql(entity: CatalogDomain): WriteSql = {
    sql"""
     INSERT INTO  ${this.table}
       (
        ${this.column.pk},
        ${this.column.labels},
        ${this.column.moniker},
        ${this.column.domainProperties},
        ${this.column.udk},
        ${this.column.createTimestamp}
      )
     VALUES
       ( {pk}, {labels}, {moniker}, {domainProperties}, {udk}, {createTime}  )
     """.bindByName(
      Symbol("pk") -> entity.pk,
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("moniker") -> entity.moniker,
      Symbol("domainProperties") -> propertyMapToString(entity.domainProperties),
      Symbol("udk") -> entity.udk.orNull,
      Symbol("createTime") -> DateTime.now
    )
  }

  override protected def insertEntitySql(entity: CatalogDomain): WriteSql = {
    sql"""
     INSERT INTO  ${this.table}
       (
        ${this.column.labels},
        ${this.column.moniker},
        ${this.column.domainProperties},
        ${this.column.udk},
        ${this.column.createTimestamp}
      )
     VALUES
       ( {labels}, {moniker}, {domainProperties}, {udk}, {createTime}  )
     """.bindByName(
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("moniker") -> entity.moniker,
      Symbol("domainProperties") -> propertyMapToString(entity.domainProperties),
      Symbol("udk") -> entity.udk.orNull,
      Symbol("createTime") -> DateTime.now
    )
  }

  override protected def updateEntityByPkSql(entity: CatalogDomain): WriteSql = {
    sql"""
     UPDATE  ${this.table}
     SET
       ${this.column.moniker} = {moniker},
       ${this.column.labels} = {labels},
       ${this.column.domainProperties} = {domainProperties},
       ${this.column.udk} = {udk}
     WHERE
       ${this.column.pk} = {pk}
     """.bindByName(
      Symbol("pk") -> entity.pk,
      Symbol("moniker") -> entity.moniker,
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("domainProperties") -> propertyMapToString(entity.domainProperties),
      Symbol("udk") -> entity.udk.orNull
    )
  }

  /// CatalogUdkPersister methods

  override protected def updatesForEntityByUdk(proposed: CatalogDomain, stored: CatalogDomain): WriteSql = {
    val entity = fieldsForUpdate(proposed, stored)
    sql"""
     UPDATE  ${this.table}
     SET
       ${this.column.moniker} = {moniker},
       ${this.column.labels} = {labels},
       ${this.column.domainProperties} = {domainProperties}
     WHERE
       ${this.column.udk} = {udk}
     """.bindByName(
      Symbol("moniker") -> entity.moniker,
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("domainProperties") -> propertyMapToString(entity.domainProperties),
      Symbol("udk") -> entity.udk.orNull
    )
  }

  override protected def updatesForEntityByPk(proposed: CatalogDomain, stored: CatalogDomain): WriteSql = {
    val entity = fieldsForUpdate(proposed, stored)
    sql"""UPDATE  ${this.table}
     SET
       ${this.column.moniker} = {moniker},
       ${this.column.labels} = {labels},
       ${this.column.domainProperties} = {domainProperties},
       ${this.column.udk} = {udk}
     WHERE
        ${this.column.pk} = {pk}""".bindByName(
      Symbol("moniker") -> entity.moniker,
      Symbol("labels") -> optionalPropertyMapToString(entity.labels),
      Symbol("domainProperties") -> propertyMapToString(entity.domainProperties),
      Symbol("udk") -> entity.udk.orNull,
      Symbol("pk") -> entity.pk
    )
  }

  private def fieldsForUpdate(proposed: CatalogDomain, stored: CatalogDomain): CatalogDomain = {
    val pk = stored.pk
    val moniker = if (proposed.moniker != "") proposed.moniker else stored.moniker
    val domainProperties = (stored.domainProperties ++ proposed.domainProperties).toMap
    val udk = proposed.udk.orElse(stored.udk)
    val labels: Option[Map[String, String]] = (proposed.labels, stored.labels) match {
      case (Some(proposedLabels), Some(storedLabels)) => Some((storedLabels ++ proposedLabels).toMap)
      case (None, Some(labels)) => Some(labels.toMap)
      case (Some(labels), None) => Some(labels.toMap)
      case (None, None) => None
    }
    CatalogDomain(pk, moniker, domainProperties, udk, labels)
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Table Schema
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def mysqlCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
        ${this.column.pk} BIGINT NOT NULL AUTO_INCREMENT,
        ${this.column.moniker} VARCHAR(255) NOT NULL UNIQUE,
        ${this.column.labels} TEXT,
        ${this.column.domainProperties} TEXT,
        ${this.column.udk} VARCHAR(255) UNIQUE,
        ${this.column.modifyTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        ${this.column.createTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (${this.column.pk}),
        UNIQUE (${this.column.udk})
     ) ENGINE=InnoDb DEFAULT CHARSET=utf8
     """

  def derbyCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
        ${this.column.pk} BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
        ${this.column.moniker} VARCHAR(255) NOT NULL,
        ${this.column.labels} VARCHAR(32672),
        ${this.column.domainProperties} VARCHAR(32672),
        ${this.column.udk} VARCHAR(255),
        ${this.column.modifyTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        ${this.column.createTimestamp} TIMESTAMP,
        UNIQUE (${this.column.udk})
    )
    """

}
