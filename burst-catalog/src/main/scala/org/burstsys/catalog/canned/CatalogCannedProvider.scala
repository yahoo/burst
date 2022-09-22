/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.canned

import org.burstsys.catalog.api._
import org.burstsys.catalog.model.account.CatalogCannedAccount
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.query._
import org.burstsys.catalog.model.view._
import org.burstsys.catalog.persist.CatalogSqlProvider
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reflection._
import scalikejdbc.DBSession

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
 *
 */
final case
class CatalogCannedProvider(modality: VitalsServiceModality) extends CatalogCannedService {

  override def serviceName: String = s"catalog-canned"

  var cans: Array[CatalogCan] = _

  def cannedAccounts: Array[CatalogCannedAccount] = cans.flatMap(_.accounts)

  def cannedDomains: Array[CatalogCannedDomain] = cans.flatMap(_.domains)

  def cannedViews: Array[CatalogCannedView] = cans.flatMap(_.views)

  def cannedQueries: Array[CatalogCannedQuery] = cans.flatMap(_.queries)

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    log info startingMessage
    cans = getSubTypesOf(classOf[CatalogCan]).toList.sortBy(clazz => clazz.getCanonicalName)
      .map(_.getDeclaredConstructor().newInstance()).toArray
    markRunning
    this
  }

  override
  def stop: this.type = {
    log info stoppingMessage
    markNotRunning
    this
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // Api
  ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Load the store with canned data for unit tests
   */
  override
  def loadCannedData(sql: CatalogSqlProvider, onlyQuery: Boolean, addSecurity: Boolean)(implicit session: DBSession): Unit = {
    loadEach(cannedQueries, (query: CatalogCannedQuery) => {
      sql.queries.insertEntity(BurstCatalogApiQuery(0, query.moniker, query.languageType, query.source, query.labels))
    })

    if (!onlyQuery || addSecurity) {
      loadEach(cannedAccounts, (account: CatalogCannedAccount) => {
        sql.accounts.insertAccount(account.username, account.password)
      })
    }

    if (onlyQuery) return

    loadEach(cannedDomains, (domain: CatalogCannedDomain) => {
      sql.domains.insertEntity(BurstCatalogApiDomain(0, domain.moniker, domain.domainProperties, domain.udk, domain.labels))
    })

    loadEach(cannedViews, (view: CatalogCannedView) => {
      val domainPk = sql.domains.findEntityByMoniker(view.domainMoniker).map(_.pk)
        .getOrElse(throw VitalsException(s"unable to find domain '${view.domainMoniker}' in table ${sql.domains.sqlTableName}"))
      sql.views.insertEntity(BurstCatalogApiView(0, view.moniker, domainPk, view.generationClock, view.storeProperties, view.viewMotif, view.viewProperties, view.labels, view.schemaName, udk = view.udk))
    })
  }

  private def loadEach[T](entities: Array[T], doInsert: T => Unit): Unit = {
    entities.foreach(e => {
      try {
        doInsert(e)
      } catch safely {
        case ex: Throwable =>
          log error burstStdMsg(s"failed to insert $e", ex)
      }
    })
  }
}
