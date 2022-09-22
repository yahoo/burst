/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.test

import org.burstsys.catalog.CatalogSqlConsumer
import org.burstsys.catalog.model.domain.CatalogDomain
import org.burstsys.catalog.model.query.BurstCatalogQuery
import org.burstsys.catalog.model.view.CatalogView
import org.burstsys.catalog.persist.CatalogSqlProvider
import org.burstsys.relate.dialect.RelateDialect
import org.burstsys.relate.dialect.RelateMySqlDialect
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsStandardClient
import org.burstsys.vitals.logging.VitalsLog

object CreateBootstrapSQL {
  def main(args: Array[String]): Unit = {
    VitalsLog.configureLogging("bootstrap-sql", consoleOnly = true)

    val sql = CatalogSqlProvider(ConsoleSqlConsumer()).start

    val schema = sql.persisters.map(_.createSchemaText).mkString("\n")
    val insertDomain = sql.domains.insertEntityStatement(
      CatalogDomain(1, "Synthetic Data", Map.empty, "synthetic_data", Map.empty)
    )
    val insertView = sql.views.insertEntityStatement(
      CatalogView(pk = 1, udk = "synthetic_view_small", moniker = "Small Synthetic View", domainFk = 1, generationClock = 0,
        storeProperties = Map("" -> ""), viewMotif = "", viewProperties = Map("" -> ""), labels = Map.empty, schemaName = "Unity")
    )
    val insertCountQuery = sql.queries.insertEntityStatement(BurstCatalogQuery(1, "Count users, sessions, events", "eql",
      """select count(user) as users, count(user.sessions) as sessionCount, count(user.sessions.events) as eventCount
        |from schema unity
        |""".stripMargin))
    val insertUserIdQuery = sql.queries.insertEntityStatement(BurstCatalogQuery(1, "Fetch user ids", "eql",
      """select user.id
        |from schema unity
        |""".stripMargin)
    )

    log info
      s"""
         |$schema
         |$insertDomain
         |$insertView
         |$insertCountQuery
         |$insertUserIdQuery
         |""".stripMargin
  }
}

case class ConsoleSqlConsumer(dialect: RelateDialect = RelateMySqlDialect) extends CatalogSqlConsumer {
  override def modality: VitalsService.VitalsServiceModality = VitalsStandardClient

  override def executeDDL: Boolean = false
}
