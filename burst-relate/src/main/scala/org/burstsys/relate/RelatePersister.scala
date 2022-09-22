/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate

import org.apache.logging.log4j.Logger
import org.burstsys.relate
import org.burstsys.relate.dialect.SelectLockLevel.NoLock
import org.burstsys.relate.dialect.RelateDerbyDialect
import org.burstsys.relate.dialect.RelateMySqlDialect
import org.burstsys.relate.dialect.SelectLockLevel
import org.burstsys.vitals.errors._
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.properties.propertyMapToString
import org.checkerframework.checker.units.qual.m
import org.checkerframework.checker.units.qual.s
import scalikejdbc._

/**
  * base class for all relate persisters
  */
abstract class RelatePersister[E <: RelateEntity] extends SQLSyntaxSupport[E] {

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // connection-related things
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final def log: Logger = relate.log

  lazy val parameters = s"table=$sqlTableName,database=$sqlDatabaseName"

  def service: RelateService

  final def sqlDatabaseName: String = service.dbName

  def sqlTableName: String

  type WriteSql = SQL[E, NoExtractor]

  final override def tableName: String = sqlTableName

  def connection: DBConnection = service.connection

  /**
   * this is a screwed up design pattern since its hard to inject the name of the NamedDB in the lifecycle of
   * an object. This is only used for dynamic database meta-data stuff
   *
   * @return
   */
  final override def connectionPoolName: String = sqlDatabaseName

  final def inTx[A](work: DBSession => A): A = connection localTx { s => work(s) }

  final def doesExist(implicit session: DBSession): Boolean = {
    session.connection.getMetaData.getTables(session.connection.getCatalog, null, this.sqlTableName.toLowerCase, null).next ||
      session.connection.getMetaData.getTables(session.connection.getCatalog, null, this.sqlTableName.toUpperCase, null).next
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Column names
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val entityPkColumn = "pk"
  final val monikerColumn = "moniker"
  final val labelsColumn = "labels"
  final val udkColumn = "udk"

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // SUBTYPE API
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected val t: QuerySQLSyntaxProvider[SQLSyntaxSupport[E], E] = this.syntax("t")

  protected def apply(t: ResultName[E])(rs: WrappedResultSet): E = ???

  /**
   * convert one row in the SQL result to an entity
   * @param rs the row to convert
   */
  protected def resultToEntity(rs: WrappedResultSet): E

  protected def timestampColToMillis(rs: WrappedResultSet, column: String): Option[Long] = {
    rs.timestampOpt(column).map(_.getTime)
  }

  /**
   * @return the SQL statement to create the table
   */
  protected def createTableSql: TableCreateSql

  /**
   * @return the text used to create the schmea
   */
  def createSchemaText: String = s"${createTableSql.statement.trim};"

  /**
   * @param entity the entity to insert
   * @return the sql required to insert a new record
   */
  protected def insertEntitySql(entity: E): WriteSql

  /**
   * @param entity the entity to insert
   * @return SQL that can be used to insert the entity
   */
  def insertEntityStatement(entity: E): String = {
    val sqlStmt = insertEntitySql(entity)
    val values = sqlStmt.parameters.map({
      case props: VitalsPropertyMap => propertyMapToString(props)
      case x => x
    }).map({
      case str: String => s"'$str'"
      case o => o.toString
    })
    val tuples = sqlStmt.statement.trim.split("\\?").zipAll(values, "", "")
    s"${tuples.map(t => s"${t._1}${t._2}").mkString}; "
  }

  /**
   * @param entity the new final state of the entity
   * @return the sql statement to update an entity
   */
  protected def updateEntityByPkSql(entity: E): WriteSql

  /**
   * generate a sql statement that updates an entity using `proposed` as a set of updates to the existing state,
   * rather than a new final state. This update should have a where-clause like `where entity.pk = ?`
   * @param proposed the updates to make to an entity
   * @param stored the version of the entity in the database
   * @return the sql statement that makes the updates specified by `proposed`
   */
  protected def updatesForEntityByPk(proposed: E, stored: E): WriteSql = updateEntityByPkSql(proposed)

  /**
   * generate a sql statement that updates an entity using `proposed` as a set of updates to the existing state,
   * rather than a new final state. This update should have a where-clause like `where entity.udk = ?`
   * @param proposed the updates to make to an entity
   * @param stored the version of the entity in the database
   * @return the sql statement that makes the updates specified by `proposed`
   */
  protected def updatesForEntityByUdk(proposed: E, stored: E): WriteSql = ???

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PUBLIC API
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final def findEntityByPk(pk: RelatePk, lockLevel: SelectLockLevel = NoLock)(implicit session: DBSession): Option[E] = {
    sql"SELECT * FROM ${this.table} WHERE ${this.column.pk} = $pk ${service.dialect.lockClause(lockLevel)}"
      .map(resultToEntity).single().apply()
  }

  final def insertEntity(entity: E)(implicit session: DBSession): RelatePk = {
    try {
      insertEntitySql(entity).updateAndReturnGeneratedKey().apply()
    } catch throwMappedException(service.dialect)
  }

  final def insertAndFetch(entity: E)(implicit session: DBSession): E = {
    try {
      val pk = insertEntity(entity)
      findEntityByPk(pk).get
    } catch throwMappedException(service.dialect)
  }

  final def updateEntity(entity: E)(implicit session: DBSession): RelatePk = {
    try {
      updateEntityByPkSql(entity).update().apply()
      entity.pk
    } catch throwMappedException(service.dialect)
  }

  /**
   * Update an entity if required
   *
   * @param proposedEntity the new state of the entity
   * @param fetchedEntity  the entity as it exists in the database
   * @return the entity as it currenty exists in the db and a flag indicating if an update was performed
   */
  final def updateEntityIfChanged(proposedEntity: E, fetchedEntity: E, update: WriteSql)(implicit session: DBSession): (E, Boolean) = {
    try {
      val willUpdate = proposedEntity.shouldUpdate(fetchedEntity)
      if (willUpdate) {
        update.update().apply()
      }
      (findEntityByPk(fetchedEntity.pk).get, willUpdate)
    } catch throwMappedException(service.dialect)
  }

  final def deleteEntity(pk: RelatePk)(implicit session: DBSession): Unit = {
    sql"DELETE FROM ${this.table} WHERE ${this.column.pk} = $pk".update().apply()
  }

  final def createTable(implicit session: DBSession): Unit = {
    lazy val tag = s"RelatePersister.createTable($parameters)"
    if (!doesExist) {
      log info s"RELATE_TABLE_CREATE $tag"
      createTableSql.bind().execute().apply()
    } else {
      log debug s"RELATE_TABLE_CREATE_ALREADY_EXIST $tag"
    }
  }

  final def dropTable(implicit session: DBSession): Unit = {
    lazy val tag = s"RelatePersister.dropTable($parameters)"
    if (doesExist) {
      log info s"RELATE_TABLE_DROP $tag"
      try {
        service.dialect match {
          case RelateDerbyDialect => sql"DROP TABLE ${this.table}".execute().apply()
          case RelateMySqlDialect => sql"DROP TABLE IF EXISTS ${this.table}".execute().apply()
        }
        log info s"RELATE_TABLE_DROP_SUCCEED $tag"
      } catch safely {
        case e: Throwable =>
      }
    } else {
      log debug s"RELATE_TABLE_DROP_NOT_EXIST $tag"
    }
  }

  final def fetchAllEntities(limit: Option[Int] = None)(implicit session: DBSession): List[E] = {
    sql"SELECT * FROM ${this.table} ${service.dialect.limitClause(limit)}".map(resultToEntity).list().apply()
  }

}
