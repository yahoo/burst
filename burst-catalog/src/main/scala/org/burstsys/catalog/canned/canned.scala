/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog

import org.burstsys.catalog.model.account.CatalogCannedAccount
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.query.CatalogCannedQuery
import org.burstsys.catalog.model.view.CatalogCannedView
import org.burstsys.catalog.persist.CatalogSqlProvider
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties._
import scalikejdbc.DBSession

import scala.language.postfixOps

package object canned extends VitalsLogger {

  trait CatalogCannedService extends VitalsService {
    def loadCannedData(sql: CatalogSqlProvider, onlyQuery: Boolean, addSecurity:Boolean)(implicit session: DBSession): Unit
  }

  /**
    * A CatalogCan typically provides only some catalog entity types
    */
  trait CatalogCan {
    def accounts: Array[CatalogCannedAccount] = Array.empty

    def domains: Array[CatalogCannedDomain] = Array.empty

    def views: Array[CatalogCannedView] = Array.empty

    def queries: Array[CatalogCannedQuery] = Array.empty
  }

  val defaultMotif: String =
    """VIEW default {
      |  INCLUDE user
      |    WHERE user.project.installTime BETWEEN
      |      CAST('2016-02-15' AS DATETIME) AND CAST('2017-02-15' AS DATETIME)
      |}
    """.stripMargin

  val cannedStoreProperties: VitalsPropertyMap = Map(
    "burst.store.name" -> "canned"
  )

  val exceptionalStoreProperties: VitalsPropertyMap = Map(
    "burst.store.name" -> "exceptional",
    "burst.store.exception.FailureLocation" -> "FailOnWorker",
    "burst.store.exception.FailureMode" -> "UncaughtException",
    "burst.store.exception.FailureRate" -> "0.5",
    "burst.store.exception.FailingContainers" -> ""
  )

  val fuseStoreProperties: VitalsPropertyMap = Map(
    "burst.store.name" -> "fuse"
  )

  val sampleStoreProperties: Map[String, String] = Map(
    "burst.store.name" -> "sample",
    "burst.samplestore.source.name" -> "mocksource",
    "burst.samplestore.source.version" -> "0.1"
  )

  val searStoreProperties: Map[String, String] = Map(
    "burst.store.name" -> "sear"
  )


}
