/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.test.support

import org.burstsys.catalog.persist.UdkCatalogEntity
import org.burstsys.catalog.persist.UdkCatalogEntityPersister
import org.burstsys.relate.dialect.RelateDerbyDialect
import org.burstsys.relate.dialect.RelateMySqlDialect
import org.burstsys.relate.RelateEntity
import org.burstsys.relate.RelatePk
import org.burstsys.relate.RelateService
import org.burstsys.relate.TableCreateSql
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.properties.optionalPropertyMapToString
import org.burstsys.vitals.properties.stringToOptionalPropertyMap
import scalikejdbc._

case class TestUdkEntity(
                          pk: RelatePk = 0,
                          udk: Option[String] = None,
                          moniker: String = "",
                          labels: Option[VitalsPropertyMap] = None
                        ) extends UdkCatalogEntity {
  override def shouldUpdate(storedEntity: RelateEntity): Boolean = {
    if (!storedEntity.isInstanceOf[TestUdkEntity])
      throw VitalsException(s"Something bad happened in shouldUpdate found a ${storedEntity.getClass}")

    val fromDb = storedEntity.asInstanceOf[TestUdkEntity]
    if (fromDb.pk == pk) {
      fromDb.udk != udk || fromDb.moniker != moniker || fromDb.labels != labels
    } else if (pk == 0 && fromDb.udk == udk) {
      fromDb.moniker != moniker || fromDb.labels != labels
    } else {
      throw VitalsException(s"Something bad happened in shouldUpdate tried to check $this against $fromDb")
    }
  }
}

case class TestUdkEntityPersister(service: RelateService) extends UdkCatalogEntityPersister[TestUdkEntity] {
  override def sqlTableName: String = "TestUdkEntity"

  override def columnNames: Seq[String] = Seq(entityPkColumn, udkColumn, monikerColumn, labelsColumn)

  override protected def resultToEntity(rs: WrappedResultSet): TestUdkEntity = TestUdkEntity(
    pk = rs.long(entityPkColumn), udk = rs.stringOpt(udkColumn), moniker = rs.string(monikerColumn), labels = stringToOptionalPropertyMap(rs.string(labelsColumn))
  )

  override protected def createTableSql: TableCreateSql = service.dialect match {
    case RelateMySqlDialect =>
      sql"""
          CREATE TABLE ${this.table} (
            ${this.column.pk} BIGINT NOT NULL AUTO_INCREMENT,
            ${this.column.udk} VARCHAR(255),
            ${this.column.moniker} VARCHAR(255) NOT NULL,
            ${this.column.labels} TEXT,
            PRIMARY KEY (${this.column.pk}),
            UNIQUE (${this.column.moniker}),
            UNIQUE (${this.column.udk})
          ) ENGINE=InnoDb DEFAULT CHARSET=utf8
        """

    case RelateDerbyDialect =>
      sql"""
          CREATE TABLE ${this.table} (
            ${this.column.pk} BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
            ${this.column.udk} VARCHAR(255),
            ${this.column.moniker} VARCHAR(255) NOT NULL,
            ${this.column.labels} VARCHAR(32672),
            CONSTRAINT primary_key PRIMARY KEY (${this.column.pk}),
            UNIQUE (${this.column.moniker}),
            UNIQUE (${this.column.udk})
          )
        """
  }

  override protected def updateEntityByPkSql(entity: TestUdkEntity): WriteSql = {
    sql"""
        UPDATE ${this.table}
        SET
          ${this.column.udk} = {udk},
          ${this.column.moniker} = {moniker},
          ${this.column.labels} = {labels}
        WHERE
         ${this.column.pk} = {pk}
        """.bindByName(
      'pk -> entity.pk,
      'udk -> entity.udk.orNull,
      'moniker -> entity.moniker,
      'labels -> optionalPropertyMapToString(entity.labels)
    )
  }

  override protected def updatesForEntityByUdk(entity: TestUdkEntity, stored: TestUdkEntity): WriteSql = {
    sql"""
        UPDATE ${this.table}
        SET
          ${this.column.moniker} = {moniker},
          ${this.column.labels} = {labels}
        WHERE
         ${this.column.udk} = {udk}
        """.bindByName(
      'udk -> entity.udk.orNull,
      'moniker -> entity.moniker,
      'labels -> optionalPropertyMapToString(entity.labels)
    )
  }

  var waitForDuplicateInsert = false

  override protected def insertEntitySql(entity: TestUdkEntity): WriteSql = {
    if (waitForDuplicateInsert) Thread.sleep(250)
    sql"""
        INSERT INTO ${this.table}
          (
            ${this.column.udk},
            ${this.column.moniker},
            ${this.column.labels}
          )
        VALUES
         ({udk}, {moniker}, {labels})
        """.bindByName(
      'udk -> entity.udk.orNull,
      'moniker -> entity.moniker,
      'labels -> optionalPropertyMapToString(entity.labels)
    )
  }
}
