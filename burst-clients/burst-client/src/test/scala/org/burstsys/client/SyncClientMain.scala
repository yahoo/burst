/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client

import org.burstsys.client.client.BurstSyncClient
import org.burstsys.client.client.model.BDomain
import org.burstsys.client.client.model.BParameter
import org.burstsys.client.client.model.BView
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.logging.VitalsLogger

import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

object SyncClientMain extends VitalsLogger {

  def main(args: Array[String]): Unit = {
    VitalsLog.configureLogging("client-main-test", consoleOnly = true)

    val parallelism = 1
    //        val queryCount = 500
    val queryCount = 10
    val runners = (1 to parallelism) map { r =>
      Future {
        Thread.currentThread().setName(s"Runner-${r}")
        val allMethods = false
//        val client = BurstSyncClient.httpsClient("burst-c2.flurry.com", 4443)
            val client = BurstSyncClient.httpsClient("localhost", 4443)
//        val (domainUdk, viewUdk, schema) = ("app:185933", "production:default_full:unity", "unity")
            val (domainUdk, viewUdk, schema) = ("udk01", "vudk01", "quo")


        if (allMethods) {
          val newView = BView.withUdk("foo_domain", "foo_view")
            .withMoniker("From Java Client")
            .withSchemaName("unity")
            .withViewMotif("view v { include user where false }").build()
          val newDomain = BDomain.withUdk("foo_domain")
            .withMoniker("From Java Client")
            .withLabels(Map("source" -> "java_client", "kind" -> "testing2").asJava)
            .withViews(Array(newView).toList.asJava).build()
          try {
            val ensuredDomain = client.ensureDomain(newDomain)
            log info (s"ensured domain: $ensuredDomain (creation: ${ensuredDomain.getCreateTimestamp})")
          } catch safely {
            case t => log error("error in ensureDomain", t)
          }

          val newView2 = BView.withUdk("foo_domain", "foo_view_2")
            .withMoniker("From Java Client 2")
            .withSchemaName("unity")
            .withViewMotif("view v { include user where false }").build()
          try {
            val ensuredView = client.ensureDomainContainsView(newDomain.getUdk, newView2)
            log info (s"ensured view: $ensuredView (creation: ${ensuredView.getCreateTimestamp})")
          } catch safely {
            case t => log error("error in ensureDomainContainsView", t)
          }

          try {
            val domain = client.findDomain("app:1008749").orElse(null)
          } catch safely {
            case t => log error("error in findDomain", t)
          }

          try {
            client.listViewsInDomain(newDomain.getUdk)
          } catch safely {
            case t => log error("error in listViewsInDomain", t)
          }
        }

        var failedQueries = 0
        for (i <- 1 to queryCount) {
          val guid = s"J${UUID.randomUUID().toString.replace("-", "")}"
          val query =
            s"""select(minSessions: long) as allUsers count(user) as users, max(count(user.sessions)) as mostSessions
               |  beside select as heavyUsers_${r}_${i * 2} count(user) where count(user.sessions) > ($$minSessions + $i)
               |from schema $schema""".stripMargin
          val timezone = "etc/utc"
          val params = Array[BParameter](BParameter.longVal("minSessions", i)).toList.asJava
          try {
            val response = client.executeQuery(guid, domainUdk, viewUdk, query, timezone, params)
            log.info("")
            log.info(s"Results for $guid:")
            response.resultSets.asScala.foreach { entry =>
              val (name, results) = entry
              val firstRow = results.rows.get(0).asScala.zipWithIndex.map(c => (results.columnNames.get(c._2), c._1.datum.value()))
              log.info(s"$name: ${firstRow.map(c => s"${c._1}=${c._2}").mkString(",")}")
            }
          } catch safely {
            case e =>
              failedQueries += 1
              log.error(s"Failed to execute query $i")
          }
        }
        failedQueries
      }
    }
    var totalFailed = 0
    for (runner <- runners) {
      totalFailed += Await.result(runner, Duration.Inf)
    }
    log.info(s"Queries attempted: ${parallelism * queryCount}. Queries failed: $totalFailed")
  }

}
