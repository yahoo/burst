/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog

import org.burstsys.catalog.CatalogService.CatalogConfiguration
import org.burstsys.catalog.api.BurstCatalogApiStatus.BurstCatalogApiSuccess
import org.burstsys.catalog.api._
import org.burstsys.catalog.api.client.CatalogApiClient
import org.burstsys.catalog.api.server.CatalogApiServer
import org.burstsys.catalog.canned.{CatalogCannedProvider, CatalogCannedService}
import org.burstsys.catalog.configuration.{CatalogApiProperties, burstCatalogCannedImportStandaloneOnlyProperty}
import org.burstsys.catalog.model.account._
import org.burstsys.catalog.model.cell._
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.master._
import org.burstsys.catalog.model.query._
import org.burstsys.catalog.model.site._
import org.burstsys.catalog.model.view._
import org.burstsys.catalog.model.worker._
import org.burstsys.catalog.persist.CatalogSqlProvider
import org.burstsys.catalog.provider._
import org.burstsys.fabric.metadata.model.FabricMetadataLookup
import org.burstsys.relate.RelatePk
import org.burstsys.relate.dialect.{RelateDerbyDialect, RelateDialect, RelateMySqlDialect}
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandaloneServer, VitalsStandardClient, VitalsStandardServer}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.healthcheck.VitalsHealthMonitoredService
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.VitalsPropertyMap
import scalikejdbc.{DBConnection, GlobalSettings, LoggingSQLAndTimeSettings}

import scala.concurrent.{Await, Awaitable}
import scala.util.{Failure, Success, Try}

trait CatalogService extends VitalsService with CatalogApiProperties {

  /**
    * TODO
    *
    * @return
    */
  def configuration: CatalogConfiguration

  /**
    * the lookup api for general use throughout system
    *
    * @return
    */
  def metadataLookup: FabricMetadataLookup

  /////////////////////////////////////////////////////////////////////////////////////////
  // Accounts
  /////////////////////////////////////////////////////////////////////////////////////////

  def registerAccount(moniker: String, password: String): Try[RelatePk]

  def changeAccountPassword(moniker: String, password: String, newPassword: String): Try[Boolean]

  def verifyAccount(moniker: String, password: String): Try[CatalogAccount]

  /////////////////////////////////////////////////////////////////////////////////////////
  // Queries
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
    *
    * @param key
    * @return
    */
  def findQueryByPk(key: Long): Try[CatalogQuery]

  /**
    *
    * @param moniker
    * @return
    */
  def findQueryByMoniker(moniker: BurstMoniker): Try[CatalogQuery]

  /**
    *
    * @param key
    * @return
    */
  def deleteQuery(key: Long): Try[RelatePk]

  /**
    *
    * @param query
    * @return
    */
  def updateQuery(query: BurstCatalogApiQuery): Try[CatalogQuery]

  /**
    *
    * @param query
    * @return
    */
  def insertQuery(query: CatalogQuery): Try[RelatePk]

  /**
    *
    * @return
    */
  def allQueries(limit: Option[Int] = None): Try[Array[CatalogQuery]]

  /**
    *
    * @param descriptor
    * @return
    */
  def searchQueries(descriptor: String, limit: Option[Int] = None): Try[Array[CatalogQuery]]

  /**
    *
    * @param label
    * @return
    */
  def searchQueriesByLabel(label: String, value: Option[String] = None, limit: Option[Int] = None): Try[Array[CatalogQuery]]

  /////////////////////////////////////////////////////////////////////////////////////////
  // Masters
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
    *
    * @param key
    * @return
    */
  def findMasterByPk(key: Long): Try[CatalogMaster]

  /**
    *
    * @param moniker
    * @return
    */
  def findMasterByMoniker(moniker: String): Try[CatalogMaster]

  /**
    *
    * @param key
    * @return
    */
  def deleteMaster(key: Long): Try[RelatePk]

  /**
    *
    * @param siteFk
    * @return
    */
  def deleteMastersForSite(siteFk: Long): Try[RelatePk]

  /**
    *
    * @param cellFk
    * @return
    */
  def deleteMastersForCell(cellFk: Long): Try[RelatePk]

  /**
    *
    * @param master
    * @return
    */
  def insertMaster(master: CatalogMaster): Try[RelatePk]

  /**
    *
    * @param master
    * @return
    */
  def updateMaster(master: CatalogMaster): Try[CatalogMaster]

  /**
    *
    * @return
    */
  def allMasters(limit: Option[Int] = None): Try[Array[CatalogMaster]]

  /**
    *
    * @param siteFk
    * @param limit
    * @return
    */
  def allMastersForSite(siteFk: RelatePk, limit: Option[Int] = None): Try[Array[CatalogMaster]]

  /**
    *
    * @param cellFk
    * @param limit
    * @return
    */
  def allMastersForCell(cellFk: RelatePk, limit: Option[Int] = None): Try[Array[CatalogMaster]]

  /**
    *
    * @param descriptor
    * @return
    */
  def searchMasters(descriptor: String, limit: Option[Int] = None): Try[Array[CatalogMaster]]

  /**
    *
    * @param label
    * @return
    */
  def searchMastersByLabel(label: String, value: Option[String] = None, limit: Option[Int] = None): Try[Array[CatalogMaster]]

  /////////////////////////////////////////////////////////////////////////////////////////
  // Workers
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
    *
    * @param key
    * @return
    */
  def findWorkerByPk(key: Long): Try[CatalogWorker]

  /**
    *
    * @param moniker
    * @return
    */
  def findWorkerByMoniker(moniker: String): Try[CatalogWorker]

  /**
    *
    * @param key
    * @return
    */
  def deleteWorker(key: Long): Try[RelatePk]

  /**
    *
    * @param siteFk
    * @return
    */
  def deleteWorkersForSite(siteFk: Long): Try[RelatePk]

  /**
    *
    * @param cellFk
    * @return
    */
  def deleteWorkersForCell(cellFk: Long): Try[RelatePk]

  /**
    *
    * @param worker
    * @return
    */
  def insertWorker(worker: CatalogWorker): Try[RelatePk]

  /**
    *
    * @param worker
    * @return
    */
  def updateWorker(worker: CatalogWorker): Try[CatalogWorker]

  /**
    *
    * @return
    */
  def allWorkers(limit: Option[Int] = None): Try[Array[CatalogWorker]]

  /**
    *
    * @param siteFk
    * @param limit
    * @return
    */
  def allWorkersForSite(siteFk: RelatePk, limit: Option[Int] = None): Try[Array[CatalogWorker]]

  /**
    *
    * @param cellFk
    * @param limit
    * @return
    */
  def allWorkersForCell(cellFk: RelatePk, limit: Option[Int] = None): Try[Array[CatalogWorker]]

  /**
    *
    * @param descriptor
    * @return
    */
  def searchWorkers(descriptor: String, limit: Option[Int] = None): Try[Array[CatalogWorker]]

  /**
    *
    * @param label
    * @return
    */
  def searchWorkersByLabel(label: String, value: Option[String] = None, limit: Option[Int] = None): Try[Array[CatalogWorker]]

  /////////////////////////////////////////////////////////////////////////////////////////
  // Sites
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
    *
    * @param key
    * @return
    */
  def findSiteByPk(key: Long): Try[CatalogSite]

  /**
    *
    * @param moniker
    * @return
    */
  def findSiteByMoniker(moniker: String): Try[CatalogSite]

  /**
    *
    * @param key
    * @return
    */
  def deleteSite(key: Long): Try[RelatePk]

  /**
    *
    * @param site
    * @return
    */
  def insertSite(site: CatalogSite): Try[RelatePk]

  /**
    *
    * @param site
    * @return
    */
  def updateSite(site: BurstCatalogApiSite): Try[CatalogSite]

  /**
    *
    * @return
    */
  def allSites(limit: Option[Int] = None): Try[Array[CatalogSite]]

  /**
    *
    * @param descriptor
    * @return
    */
  def searchSites(descriptor: String, limit: Option[Int] = None): Try[Array[CatalogSite]]

  /**
    *
    * @param label
    * @return
    */
  def searchSitesByLabel(label: String, value: Option[String] = None, limit: Option[Int] = None): Try[Array[CatalogSite]]

  /////////////////////////////////////////////////////////////////////////////////////////
  // Cells
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
    *
    * @param key
    * @return
    */
  def findCellByPk(key: Long): Try[CatalogCell]

  /**
    *
    * @param moniker
    * @return
    */
  def findCellByMoniker(moniker: String): Try[CatalogCell]

  /**
    *
    * @param key
    * @return
    */
  def deleteCell(key: Long): Try[RelatePk]

  /**
    *
    * @param siteFk
    * @return
    */
  def deleteCellsForSite(siteFk: Long): Try[RelatePk]

  /**
    *
    * @param cell
    * @return
    */
  def insertCell(cell: CatalogCell): Try[RelatePk]

  /**
    *
    * @param cell
    * @return
    */
  def updateCell(cell: BurstCatalogApiCell): Try[CatalogCell]

  /**
    *
    * @return
    */
  def allCells(limit: Option[Int] = None): Try[Array[CatalogCell]]

  /**
    *
    * @return
    */
  def allCellsForSite(siteFk: RelatePk, limit: Option[Int] = None): Try[Array[CatalogCell]]

  /**
    *
    * @param descriptor
    * @return
    */
  def searchCells(descriptor: String, limit: Option[Int] = None): Try[Array[CatalogCell]]

  /**
    *
    * @param label
    * @return
    */
  def searchCellsByLabel(label: String, value: Option[String] = None, limit: Option[Int] = None): Try[Array[CatalogCell]]

  /////////////////////////////////////////////////////////////////////////////////////////
  // Domains
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
    *
    * @param key
    * @return
    */
  def findDomainByPk(key: Long): Try[CatalogDomain]

  /**
    * Returns the domain identified by a user-defined key.
    *
    * @param udk a user-defined key for retrieving a domain
    * @return the domain, or None if not found
    */
  def findDomainByUdk(udk: String): Try[CatalogDomain]

  /**
    * Returns the domain identified by a user-defined key.
    *
    * @param udk a user-defined key for retrieving a domain
    * @return the domain, or None if not found
    */
  def findDomainWithViewsByUdk(udk: String): Try[(CatalogDomain, Seq[CatalogView])]

  /**
    *
    * @param moniker
    * @return
    */
  def findDomainByMoniker(moniker: String): Try[CatalogDomain]

  /**
    *
    * @param key
    * @return
    */
  def deleteDomain(key: Long): Try[RelatePk]

  /**
    * Create a domain, or if the specified domain exists update it.
    * Domain existence is will be verified by UDK if provided or else by pk
    *
    * @param domain the desired state of the domain
    * @return the pk of the created/updated domain
    */
  def ensureDomain(domain: CatalogDomain): Try[RelatePk]

  def insertDomain(domain: CatalogDomain): Try[RelatePk]

  def updateDomain(domain: CatalogDomain): Try[RelatePk]

  /**
    *
    * @return
    */
  def allDomains(limit: Option[Int] = None): Try[Array[CatalogDomain]]

  /**
    *
    * @param domainDescriptor
    * @return
    */
  def searchDomains(domainDescriptor: String, limit: Option[Int] = None): Try[Array[CatalogDomain]]

  /**
    *
    * @param label
    * @return
    */
  def searchDomainsByLabel(label: String, value: Option[String] = None, limit: Option[Int] = None): Try[Array[CatalogDomain]]

  /////////////////////////////////////////////////////////////////////////////////////////
  // Views
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
    *
    * @param key
    * @return
    */
  def findViewByPk(key: RelatePk): Try[CatalogView]

  /**
    *
    * @param moniker
    * @return
    */
  def findViewByMoniker(moniker: String): Try[CatalogView]

  /**
    * Returns the view identified by a user-defined key.
    *
    * @param udk a user-defined key for retrieving a view
    * @return the view, or None if not found
    */
  def findViewByUdk(udk: String): Try[CatalogView]

  /**
    *
    * @return
    */
  def allViewsForDomain(domainPk: RelatePk, limit: Option[Int] = None): Try[Array[CatalogView]]

  /**
    *
    * @param key
    * @return
    */
  def deleteView(key: Long): Try[RelatePk]

  /**
    *
    * @param key
    * @return
    */
  def deleteViewsForDomain(key: Long): Try[RelatePk]

  /**
    *
    */
  def ensureView(view: CatalogView): Try[RelatePk]

  def ensureViewInDomain(domainUdk: String, view: CatalogView): Try[CatalogView]

  def insertView(view: CatalogView): Try[RelatePk]

  def updateView(view: CatalogView): Try[RelatePk]

  /**
    *
    * @param viewPk
    */
  def updateViewGeneration(viewPk: RelatePk): Try[RelatePk]

  /**
    *
    * @param domainFk
    */
  def updateViewGenerationsForDomain(domainFk: RelatePk): Try[RelatePk]

  /**
    * Add the updated properties to the view
    *
    * @param pk
    * @param updatedProperties
    * @return
    */
  def recordViewLoad(pk: RelatePk, updatedProperties: VitalsPropertyMap): Try[RelatePk]

  /**
    *
    * @return
    */
  def allViews(limit: Option[Int] = None): Try[Array[CatalogView]]

  /**
    *
    * @param descriptor
    * @return
    */
  def searchViews(descriptor: String, limit: Option[Int] = None): Try[Array[CatalogView]]

  /**
    *
    * @param label
    * @return
    */
  def searchViewsByLabel(label: String, value: Option[String] = None, limit: Option[Int] = None): Try[Array[CatalogView]]


  /////////////////////////////////////////////////////////////////////////////////////////
  // Other
  /////////////////////////////////////////////////////////////////////////////////////////
  def searchCatalog(domainPkOrMoniker: Option[String], viewPkOrMoniker: Option[String], limit: Option[Int]): Try[Array[Map[String, String]]]
}

object CatalogService {

  sealed case
  class CatalogConfiguration(
                              modality: VitalsServiceModality,
                              dialect: RelateDialect,
                              executeDDL: Boolean,
                              loadAllCans: Boolean,
                              loadOnlyQueryCans: Boolean,
                              dropExistingTables: Boolean
                            ) {

    override def toString: BurstMoniker = getClass.getSimpleName.stripPrefix("Catalog").stripSuffix("Config$")

  }

  /**
    * Bare bones Unit test Client
    */
  object CatalogUnitTestClientConfig extends CatalogConfiguration(
    modality = VitalsStandardClient,
    dialect = RelateDerbyDialect,
    executeDDL = false,
    loadAllCans = false,
    loadOnlyQueryCans = false,
    dropExistingTables = false
  )

  /**
    * Bare bones Unit test Server
    */
  object CatalogUnitTestServerConfig extends CatalogConfiguration(
    modality = VitalsStandaloneServer,
//    dialect = RelateMySqlDialect,
    dialect = RelateDerbyDialect,
    executeDDL = true,
    loadAllCans = true,
    loadOnlyQueryCans = false,
    dropExistingTables = true
  )

  /**
    * A catalog server residing in Master JVM
    */
  object CatalogMasterConfig extends CatalogConfiguration(
    modality = VitalsStandardServer,
    dialect = RelateMySqlDialect,
    executeDDL = false,
    loadAllCans = false,
    loadOnlyQueryCans = true,
    dropExistingTables = false
  )

  /**
    * A catalog server residing in the Worker JVM
    */
  object CatalogWorkerConfig extends CatalogConfiguration(
    modality = VitalsStandardServer,
    dialect = RelateMySqlDialect,
    executeDDL = false,
    loadAllCans = false,
    loadOnlyQueryCans = false,
    dropExistingTables = false
  )

  /**
    * no frills remote client
    */
  object CatalogRemoteClientConfig extends CatalogConfiguration(
    modality = VitalsStandardClient,
    dialect = RelateMySqlDialect,
    executeDDL = false,
    loadAllCans = false,
    loadOnlyQueryCans = false,
    dropExistingTables = false
  )

  /**
    * specialized worker config for when master and worker
    * are in the same JVM
    */
  object CatalogUnitTestWorkerConfig extends CatalogConfiguration(
    modality = VitalsStandardClient,
    dialect = RelateDerbyDialect,
    executeDDL = false,
    loadAllCans = false,
    loadOnlyQueryCans = false,
    dropExistingTables = false
  )

  object CatalogPlaygroundServerConfig extends CatalogConfiguration(
    modality = VitalsStandaloneServer,
    dialect = RelateDerbyDialect,
    executeDDL = true,
    loadAllCans = false,
    loadOnlyQueryCans = true,
    dropExistingTables = true
  )


  def apply(mode: CatalogConfiguration): CatalogService = CatalogServiceContext(mode)

  // def getCatalogUtil(mode: CatalogConfiguration): CatalogUtilService = CatalogUtilService(mode)

}

private[catalog] final case
class CatalogServiceContext(configuration: CatalogConfiguration) extends AnyRef with CatalogService
  with CatalogAccountReactor with CatalogCellReactor with CatalogDomainReactor with CatalogSiteReactor
  with CatalogQueryReactor with CatalogSearchReactor with CatalogViewReactor with CatalogWorkerReactor
  with CatalogMasterReactor with CatalogSqlConsumer  with VitalsHealthMonitoredService {

  override val serviceName: String = s"$cellName-catalog($configuration)"

  override val modality: VitalsServiceModality = configuration.modality

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _sql: CatalogSqlProvider = CatalogSqlProvider(this)

  private[this]
  val _apiServer: CatalogApiServer = CatalogApiServer(this)

  private[this]
  val _apiClient: CatalogApiClient = CatalogApiClient(this)

  private[this]
  val _cannedService: CatalogCannedService = CatalogCannedProvider(modality)

  private[this]
  lazy val defaultSitePk: Long = checkForSite()

  private[this]
  lazy val defaultCellPk: Long = checkForCell()

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////

  override lazy val metadataLookup: FabricMetadataLookup = CatalogMetadataLookup(this)

  override def dialect: RelateDialect = configuration.dialect

  override def executeDDL: Boolean = configuration.executeDDL

  ///////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    if (modality.isServer) {

      GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
        enabled = true,
        singleLineMode = false,
        printUnprocessedStackTrace = false,
        stackTraceDepth = 15,
        logLevel = 'debug,
        warningEnabled = false,
        warningThresholdMillis = 3000L,
        warningLogLevel = 'warn
      )
      _sql.start
      _apiServer.start
      _cannedService.start

      if (configuration.executeDDL) {
        // check the catalog
        connection localTx {
          implicit session => _sql.executeDdl(dropIfExists = configuration.dropExistingTables)
        }
      }

      // load the canned data
      if (burstCatalogCannedImportStandaloneOnlyProperty.get.isDefined) {
        loadCannedData(queryOnly = true, addSecurity = true)
      } else if (configuration.loadAllCans || configuration.loadOnlyQueryCans) {
        loadCannedData(queryOnly = configuration.loadOnlyQueryCans)
      }

      // force lazy initialization of default cell after we load canned data
      val _ = defaultCellPk

    } else {
      _apiClient.start
    }
    log info startedMessage
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    if (modality.isServer) {
      _sql.stop
      _cannedService.stop
      _apiServer.stop
    } else {
      _apiClient.stop
    }
    markNotRunning
    this
  }

  ///////////////////////////////////////////////////////////
  // Internal
  ///////////////////////////////////////////////////////////

  private def connection: DBConnection = _sql.connection

  protected def sql: CatalogSqlProvider = _sql

  protected def apiServer: CatalogApiServer = _apiServer

  protected def apiClient: CatalogApiClient = _apiClient

  private def cannedService: CatalogCannedService = _cannedService

  protected
  def loadCannedData(queryOnly: Boolean, addSecurity: Boolean = false): Unit = {
    log info s"CATALOG_CANNED_DATA_LOAD ${if (queryOnly) "(queries-only)" else ""} $serviceName"
    connection localTx {
      implicit session => sql.deleteLabeledData(cannedDataLabel, None)

    }
    connection localTx {
      implicit session => cannedService.loadCannedData(sql, queryOnly, addSecurity)
    }
  }

  private
  def checkForCell(): Long = {
    if (modality.isClient) return 0
    // find or insert the cell
    this.findCellByMoniker(cellName) match {
      case Failure(_) =>
        // default site - just for unit tests
        val sitePk = defaultSitePk
        log info s"Creating cell entry for $cellName"
        val cCell = CatalogCell(0, cellName, sitePk)
        this.insertCell(cCell) match {
          case Success(cellPk) =>
            cellPk
          case Failure(ex) =>
            // really bad if we can't create a cell entry for our cell
            log error burstStdMsg(s"Unable to create cell entry for $cellName", ex)
            throw VitalsException(s"Unable to create default cell entry for $cellName", ex)
        }
      case Success(cell) =>
        cell.pk
    }
  }

  // Used only for unit tests
  private
  def checkForSite(): Long = {
    if (modality.isClient) return 0
    // find or insert the site
    this.findSiteByMoniker(siteName) match {
      case Failure(_) =>
        log info s"Creating site entry for $siteName"
        val cSite = CatalogSite(0, siteName)
        this.insertSite(cSite) match {
          case Success(sitePk) =>
            sitePk
          case Failure(ex) =>
            // really bad if we can't create a cell entry for our cell
            log error burstStdMsg(s"Unable to create site entry for $siteName", ex)
            throw VitalsException(s"Unable to create default site entry for $siteName", ex)
        }
      case Success(site) =>
        site.pk
    }
  }

  type EntityResponse = BurstCatalogApiEntityPkResponse

  protected
  def mapEntityResponse(future: Awaitable[EntityResponse]): Try[RelatePk] =
    mapThriftResult[EntityResponse, RelatePk](future, _.result, _.pk.get)

  protected
  def mapThriftResult[F, R](future: Awaitable[F], status: F => BurstCatalogApiResult, transform: F => R): Try[R] = {
    val response = Await.result(future, requestTimeout)
    val result = status(response)
    result.status match {
      case BurstCatalogApiSuccess => Success(transform(response))
      case _ => Failure(VitalsException(s"${result.status} ${result.message}").fillInStackTrace())
    }
  }

}

