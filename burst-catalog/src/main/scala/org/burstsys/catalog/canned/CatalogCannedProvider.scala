/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.canned

import org.burstsys.catalog.api._
import org.burstsys.catalog.model.account.CatalogCannedAccount
import org.burstsys.catalog.model.cell._
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.master._
import org.burstsys.catalog.model.query._
import org.burstsys.catalog.model.site._
import org.burstsys.catalog.model.view._
import org.burstsys.catalog.model.worker._
import org.burstsys.catalog.persist.CatalogSqlProvider
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reflection._
import scalikejdbc.DBSession

import scala.collection.JavaConverters._
import scala.language.postfixOps

/**
 *
 */
final case
class CatalogCannedProvider(modality: VitalsServiceModality) extends CatalogCannedService {

  override def serviceName: String = s"catalog-canned"

  var cans: Array[CatalogCan] = _

  def cannedAccounts: Array[CatalogCannedAccount] = cans.flatMap(_.accounts)

  def cannedSites: Array[CatalogCannedSite] = cans.flatMap(_.sites)

  def cannedCells: Array[CatalogCannedCell] = cans.flatMap(_.cells)

  def cannedDomains: Array[CatalogCannedDomain] = cans.flatMap(_.domains)

  def cannedViews: Array[CatalogCannedView] = cans.flatMap(_.views)

  def cannedQueries: Array[CatalogCannedQuery] = cans.flatMap(_.queries)

  def cannedMasters: Array[CatalogCannedMaster] = cans.flatMap(_.masters)

  def cannedWorkers: Array[CatalogCannedWorker] = cans.flatMap(_.workers)

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    log info startingMessage
    cans = getSubTypesOf(classOf[CatalogCan]).asScala.toList.sortBy(clazz => clazz.getCanonicalName)
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
    implicit val s: CatalogSqlProvider = sql

    loadEach(cannedQueries, (query: CatalogCannedQuery) => {
      sql.queries.insertEntity(BurstCatalogApiQuery(0, query.moniker, query.languageType, query.source, query.labels))
    })

    if (!onlyQuery || addSecurity) {
      loadEach(cannedAccounts, (account: CatalogCannedAccount) => {
        sql.accounts.insertAccount(account.username, account.password)
      })
    }

    if (onlyQuery) return

    loadEach(cannedSites, (site: CatalogCannedSite) => {
      sql.sites.insertEntity(BurstCatalogApiSite(0, site.moniker, site.siteProperties, site.labels))
    })

    loadEach(cannedCells, (cell: CatalogCannedCell) => {
      val sitePk = sql.sites.findEntityByMoniker(cell.siteMoniker).map(_.pk)
        .getOrElse(throw VitalsException(s"unable to find site '${cell.siteMoniker}' in table ${sql.sites.sqlTableName}"))
      sql.cells.insertEntity(BurstCatalogApiCell(1, cell.moniker, sitePk, cell.cellProperties, cell.labels))
    })

    loadEach(cannedDomains, (domain: CatalogCannedDomain) => {
      sql.domains.insertEntity(BurstCatalogApiDomain(0, domain.moniker, domain.domainProperties, domain.udk, domain.labels))
    })

    loadEach(cannedViews, (view: CatalogCannedView) => {
      val domainPk = sql.domains.findEntityByMoniker(view.domainMoniker).map(_.pk)
        .getOrElse(throw VitalsException(s"unable to find domain '${view.domainMoniker}' in table ${sql.domains.sqlTableName}"))
      sql.views.insertEntity(BurstCatalogApiView(0, view.moniker, domainPk, view.generationClock, view.storeProperties, view.viewMotif, view.viewProperties, view.labels, view.schemaName, udk = view.udk))
    })

    def getSiteAndCell(cellMoniker: Option[String], siteMoniker: String): (Long, Option[Long]) = {
      if (cellMoniker.isDefined) {
        sql.cells.findEntityByMoniker(cellMoniker.get).map(c => (c.siteFk, Some(c.pk)))
          .getOrElse(throw VitalsException(s"unable to find cell '$cellMoniker' in table ${sql.cells.sqlTableName}"))
      } else {
        sql.sites.findEntityByMoniker(siteMoniker).map(s => (s.pk, None))
          .getOrElse(throw VitalsException(s"unable to find site '$siteMoniker' in table ${sql.sites.sqlTableName}"))
      }
    }

    loadEach(cannedMasters, (master: CatalogCannedMaster) => {
      val (sitePk, cellPk) = getSiteAndCell(master.cellMoniker, master.siteMoniker)
      sql.masters.insertEntity(
        BurstCatalogApiMaster(pk = 0, master.moniker, master.nodeName, master.nodeAddress, master.masterPort,
          sitePk, cellPk, master.masterProperties, master.labels)
      )
    })

    loadEach(cannedWorkers, (worker: CatalogCannedWorker) => {
      val (sitePk, cellPk) = getSiteAndCell(worker.cellMoniker, worker.siteMoniker)
      sql.workers.insertEntity(BurstCatalogApiWorker(0, worker.moniker, worker.nodeName, worker.nodeAddress,
        sitePk, cellPk, worker.workerProperties, worker.labels))
    })
  }

  private def loadEach[T](entities: Array[T], doInsert: T => Unit): Unit = {
    entities.foreach(e => {
      try {
        doInsert(e)
      } catch safely {
        case ex: Throwable =>
          log error burstStdMsg(s"failed to insert $e", ex)
          throw ex
      }
    })
  }

}
