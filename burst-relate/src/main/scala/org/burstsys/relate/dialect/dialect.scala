/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate

import java.sql.SQLException

import scalikejdbc._

package object dialect {

  sealed trait SelectLockLevel
  object SelectLockLevel {
    case object NoLock extends SelectLockLevel
    case object SharedLock extends SelectLockLevel
    case object UpdateLock extends SelectLockLevel
  }

  /**
    * Configuration/customization for a specific type/version of RDBMS needed
    */
  trait RelateDialect extends Any {

    /**
      * a unique code for this dialect
      *
      * @return
      */
    def code: Int

    /**
      * specialized exception mapping for SQL exceptions thrown by this specific RDBMS
      */
    def mappedSqlException(e: SQLException): Throwable

    /**
      * return a sql string that will lock a record
      */
    def lockClause(level: SelectLockLevel): SQLSyntax = sqls""

    /**
      * Return a sql string that can be used to limit select clauses
      */
    def limitClause(limit: Option[Int]): SQLSyntax = sqls""

    /**
      * JDBC Driver for this dialect
      *
      * @return
      */
    def jdbcDriver: Class[_]

    /**
      * the specialized URL format for this dialect
      *
      * @param jdbcHost
      * @param jdbcPort
      * @param databaseName
      * @return
      */
    def jdbcUrl(jdbcHost: String, jdbcPort: Int, databaseName: String): String

    /**
      * the specialized ''system'' URL format for this dialect
      *
      * @param jdbcHost
      * @param jdbcPort
      * @return
      */
    def jdbcSystemUrl(jdbcHost: String, jdbcPort: Int): String

    /**
      *
      * @return
      */
    def pool: RelatePool

    /**
      * specialized shutdown operations for this dialect
      *
      * @param databaseName
      */
    def shutdown(databaseName: String): Unit

    override
    def toString: String = getClass.getSimpleName.stripPrefix("Relate").stripSuffix("$")

  }

}
