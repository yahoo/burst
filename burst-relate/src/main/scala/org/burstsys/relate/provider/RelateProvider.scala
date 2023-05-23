/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.provider

import org.burstsys.relate._
import org.burstsys.relate.configuration.burstRelateDebugProperty
import org.burstsys.relate.dialect.{RelateDerbyDialect, RelateMySqlDialect}
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._
import scalikejdbc.{ConnectionPool, ConnectionPoolFactoryRepository, ConnectionPoolSettings, DBConnection, DBSession, IsolationLevel, NamedDB}
import scalikejdbc.interpolation.Implicits._
import scalikejdbc.interpolation.SQLSyntax

import java.sql.SQLException
import scala.collection.mutable.ArrayBuffer

/**
  * base class provider for relate services
  */
abstract class RelateProvider extends AnyRef with RelateService with RelateScriptExecutor {

  override def serviceName: String = s"relate($dialect, ${dialect.jdbcUrl(dbHost, dbPort, dbName)})"

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATE
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  final val persisters = new ArrayBuffer[RelatePersister[_]]()

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def connection: DBConnection = {
    val conn = NamedDB(dbName)
    conn.isolationLevel(IsolationLevel.ReadCommitted) // this is required for upsert to work
    conn
  }

  final override
  def registerPersister(persister: RelatePersister[_]): Unit = {
    persisters += persister
  }

  final override
  def executeDdl(dropIfExists: Boolean = false)(implicit session: DBSession): this.type = {
    log info s"RELATE_DDL_EXECUTE database='$dbName' '$serviceName'"
    try {
      // make a schema if it doesn't already exist
      val schema = SQLSyntax.createUnsafely(dbName)
      if (!session.connection.getMetaData.getSchemas(session.connection.getCatalog, schema).next) {
        val createStatement = dialect match {
          case RelateMySqlDialect => sql"CREATE SCHEMA IF NOT EXISTS $schema"
          case RelateDerbyDialect => sql"CREATE SCHEMA $schema"
        }
        createStatement.executeUpdate()
      }
    } catch safely {
      case s: SQLException =>
        if (s.getMessage.indexOf("exists") >= 0) {
          // it's ok if the schema exists already
        } else {
          log error burstStdMsg(s)
          throw VitalsException(s)
        }
      case t: Throwable =>
        log error burstStdMsg(t)
        throw VitalsException(t)
    }

    // drop the tables if told to
    if (dropIfExists)
      persisters foreach (_.dropTable)
    // create the table if they don't exist already
    persisters foreach (_.createTable)
    log info s"RELATE_DB_INIT database='$dbName'"
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    lazy val tag = s"RelateProvider.start"
    ensureNotRunning
    // add our custom factory that sets some eviction and timeout items.
    ConnectionPoolFactoryRepository.add(RelateCustomDbcp2Pool.kind, org.burstsys.relate.pool.RelateCommons2ConnectionPoolFactory)
    if (isRunning)
      return this
    log info s"RELATE_PROVIDER_START '$serviceName' @ '${dialect.jdbcUrl(dbHost, dbPort, dbName)}' $tag"

    if (burstRelateDebugProperty.get && dialect == RelateDerbyDialect) {
      System.setProperty("derby.stream.error.method", "java.sql.DriverManager.getLogStream")
      //      System.setProperty("derby.language.logQueryPlan", "true")
      System.setProperty("derby.language.logStatementText", "true")
    }

    Class.forName(dialect.jdbcDriver.getName) // find JDBC driver
    // first get a system connection to test if the database exists
    val settings = ConnectionPoolSettings(connectionPoolFactoryName = dialect.pool.kind, maxSize = dbConnections)
    if (executeDDL) {
      ConnectionPool.add(dbName, dialect.jdbcSystemUrl(dbHost, dbPort), dbUser, dbPassword, settings)
      // try to switch to the database
      try {
        connection.conn.setCatalog(dbName)
        connection.showTables()
        log debug s"Catalog $dbName found"
      } catch safely {
        case e: SQLException =>
          log warn burstStdMsg(s"Catalog $dbName not found, creating it  $tag")
          connection localTx { implicit session =>
            sql"CREATE DATABASE ${SQLSyntax.createUnsafely(dbName)}".execute()

          }
      }
      // remove the system connect and re-add the connection to the existing database
      try {
        ConnectionPool.close(dbName)
      } catch safely {
        case e: Throwable =>
          log error burstStdMsg(s"Problem closing initial database connection '$e': ${e.getStackTrace.mkString("\n")}  $tag")
          log warn s"Supressing error"
      }
    }

    ConnectionPool.add(dbName, dialect.jdbcUrl(dbHost, dbPort, dbName), dbUser, dbPassword, settings)
    markRunning
    this
  }

  final override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    ConnectionPool.close(dbName)
    dialect.shutdown(dbName)
    markNotRunning
    this
  }


}
