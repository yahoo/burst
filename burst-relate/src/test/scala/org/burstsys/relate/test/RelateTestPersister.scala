/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.test

import org.burstsys.relate.dialect.{RelateDerbyDialect, RelateMySqlDialect}
import org.burstsys.relate.provider.RelateMockProvider
import org.burstsys.relate.test.model.BurstSqlTestEntity
import org.burstsys.relate.{RelateService, _}
import scalikejdbc._

final case
class RelateTestPersister() extends RelatePersister[BurstSqlTestEntity] {

  def sqlTableName = "burst_test_entity"

  val sqltest1ColumnName = "test1"

  override def columnNames: Seq[String] = Seq(entityPkColumn, sqltest1ColumnName)

  val service: RelateService = RelateMockProvider()

  override def resultToEntity(rs: WrappedResultSet): BurstSqlTestEntity = BurstSqlTestEntity(
    pk = rs.long(entityPkColumn),
    test1 = rs.string(sqltest1ColumnName)
  )

  override protected def createTableSql: TableCreateSql = {
    service.dialect match {
      case RelateMySqlDialect => ???
      case RelateDerbyDialect =>
        log info s"loading test database"
        sql"""
     CREATE TABLE ${this.table} (
       ${this.column.pk} BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
       ${this.column.test1} VARCHAR(255),
       CONSTRAINT primary_key PRIMARY KEY (${this.column.pk})
     )
     """
    }
  }

  override def updateEntityByPkSql(entity: BurstSqlTestEntity): WriteSql = service.dialect match {
    case RelateMySqlDialect => ???
    case RelateDerbyDialect =>
      sql"""
     UPDATE ${this.table}
     SET
       ${this.column.test1} = ${entity.test1}
     WHERE
       ${this.column.pk} = ${entity.pk}
     """
  }

  override def insertEntitySql(entity: BurstSqlTestEntity): WriteSql = {
    service.dialect match {
      case RelateMySqlDialect => ???
      case RelateDerbyDialect =>
        sql"""
             INSERT INTO ${this.table} (${this.column.test1})
             VALUES (${entity.test1})
          """
    }
  }

  /**
   * @param entity the entity to insert
   * @return the sql required to insert a new record
   */
  override protected def insertEntityWithPkSql(entity: BurstSqlTestEntity): WriteSql = ???
}
