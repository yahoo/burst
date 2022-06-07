/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.dialect

import java.sql.SQLException

import org.burstsys.relate.dialect.SelectLockLevel.{NoLock, SharedLock, UpdateLock}
import org.burstsys.relate.{RelateCustomDbcp2Pool, RelateExceptions, RelatePool, configuration}
import scalikejdbc._

object RelateMySqlDialect extends RelateDialect  {
  val code = 2

  val DuplicateKeyErrNos: Set[Int] = Set(1022, 1062, 1557, 1569, 1586)

  /** Best-effort mapping of MySQL vendor codes to distinguished semantic failings. */
  override def mappedSqlException(e: SQLException): Throwable = {
    if (DuplicateKeyErrNos contains e.getErrorCode) RelateExceptions.BurstDuplicateKeyException(e.getMessage, cause = e)
    else e
  }

  override def lockClause(level: SelectLockLevel): SQLSyntax = level match {
    case SharedLock => sqls"LOCK IN SHARE MODE"
    case UpdateLock => sqls"FOR UPDATE"
    case NoLock => sqls""
  }


  override def limitClause(limit: Option[Int]): SQLSyntax = limit.collect({
    case l if l > 0 => sqls"LIMIT $l"
  }).getOrElse(sqls"")

  def pool: RelatePool = RelateCustomDbcp2Pool

  def shutdown(databaseName: String): Unit = {}

  final def jdbcDriver: Class[_] = classOf[com.mysql.cj.jdbc.Driver]

  final def jdbcUrl(jdbcHost: String, jdbcPort: Int, databaseName: String): String =
    s"jdbc:mysql://$jdbcHost:$jdbcPort/$databaseName?${configuration.burstRelateMysqlConnectionOpts.getOrThrow}"

  final def jdbcSystemUrl(jdbcHost: String, jdbcPort: Int): String =
    s"jdbc:mysql://$jdbcHost:$jdbcPort?${configuration.burstRelateMysqlConnectionOpts.getOrThrow}"

}
