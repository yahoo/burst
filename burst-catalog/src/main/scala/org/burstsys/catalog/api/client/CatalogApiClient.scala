/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api.client

import org.burstsys.api.BurstApiClient
import org.burstsys.catalog.api._
import org.burstsys.catalog.{BurstMoniker, CatalogService}
import org.burstsys.vitals.errors._
import com.twitter.util.Future

import scala.language.postfixOps
import org.burstsys.vitals.logging._

/**
  * Catalog thrift repeater
  */
final case
class CatalogApiClient(service: CatalogService) extends BurstApiClient[BurstCatalogApiService.FutureIface]
  with CatalogApi {

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Accounts
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  private def doSafelyIfRunning[T](execution: => T): T = {
    try {
      ensureRunning
      execution
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Queries
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override def allQueries(limit: Option[Int]): Future[BurstCatalogApiQueryResponse] = {
    doSafelyIfRunning {
      thriftClient.allQueries(limit)
    }
  }

  override def searchQueries(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiQueryResponse] = {
    doSafelyIfRunning {
      thriftClient.searchQueries(descriptor, limit)
    }
  }

  override def searchQueriesByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiQueryResponse] = {
    doSafelyIfRunning {
      thriftClient.searchQueriesByLabel(label, value, limit)
    }
  }

  override def findQueryByPk(pk: Long): Future[BurstCatalogApiQueryResponse] = {
    doSafelyIfRunning {
      thriftClient.findQueryByPk(pk)
    }
  }

  override def findQueryByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiQueryResponse] = {
    doSafelyIfRunning {
      thriftClient.findQueryByMoniker(moniker)
    }
  }

  override def insertQuery(query: BurstCatalogApiQuery): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.insertQuery(query)
    }
  }

  override def updateQuery(query: BurstCatalogApiQuery): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.updateQuery(query)
    }
  }

  override def deleteQuery(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteQuery(pk)
    }
  }


  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Masters
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override def allMasters(limit: Option[Int]): Future[BurstCatalogApiMasterResponse] = {
    doSafelyIfRunning {
      thriftClient.allMasters(limit)
    }
  }

  override def allMastersForSite(siteFk: Long, limit: Option[Int]): Future[BurstCatalogApiMasterResponse] = {
    doSafelyIfRunning {
      thriftClient.allMastersForSite(siteFk, limit)
    }
  }

  override def allMastersForCell(cellFk: Long, limit: Option[Int]): Future[BurstCatalogApiMasterResponse] = {
    doSafelyIfRunning {
      thriftClient.allMastersForCell(cellFk, limit)
    }
  }

  override def searchMasters(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiMasterResponse] = {
    doSafelyIfRunning {
      thriftClient.searchMasters(descriptor, limit)
    }
  }

  override def searchMastersByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiMasterResponse] = {
    doSafelyIfRunning {
      thriftClient.searchMastersByLabel(label, value, limit)
    }
  }

  override def findMasterByPk(pk: Long): Future[BurstCatalogApiMasterResponse] = {
    doSafelyIfRunning {
      thriftClient.findMasterByPk(pk)
    }
  }

  override def findMasterByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiMasterResponse] = {
    doSafelyIfRunning {
      thriftClient.findMasterByMoniker(moniker)
    }
  }

  override def insertMaster(master: BurstCatalogApiMaster): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.insertMaster(master)
    }
  }

  override def updateMaster(master: BurstCatalogApiMaster): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.updateMaster(master)
    }
  }

  override def deleteMaster(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteMaster(pk)
    }
  }

  override def deleteMastersForSite(siteFk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteMastersForSite(siteFk)
    }
  }

  override def deleteMastersForCell(cellFk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteMastersForCell(cellFk)
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Workers
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override def allWorkers(limit: Option[Int]): Future[BurstCatalogApiWorkerResponse] = {
    doSafelyIfRunning {
      thriftClient.allWorkers(limit)
    }
  }

  override def allWorkersForSite(siteFk: Long, limit: Option[Int]): Future[BurstCatalogApiWorkerResponse] = {
    doSafelyIfRunning {
      thriftClient.allWorkersForSite(siteFk, limit)
    }
  }

  override def allWorkersForCell(cellFk: Long, limit: Option[Int]): Future[BurstCatalogApiWorkerResponse] = {
    doSafelyIfRunning {
      thriftClient.allWorkersForCell(cellFk, limit)
    }
  }

  override def searchWorkers(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiWorkerResponse] = {
    doSafelyIfRunning {
      thriftClient.searchWorkers(descriptor, limit)
    }
  }

  override def searchWorkersByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiWorkerResponse] = {
    doSafelyIfRunning {
      thriftClient.searchWorkersByLabel(label, value, limit)
    }
  }

  override def findWorkerByPk(pk: Long): Future[BurstCatalogApiWorkerResponse] = {
    doSafelyIfRunning {
      thriftClient.findWorkerByPk(pk)
    }
  }

  override def findWorkerByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiWorkerResponse] = {
    doSafelyIfRunning {
      thriftClient.findWorkerByMoniker(moniker)
    }
  }

  override def insertWorker(worker: BurstCatalogApiWorker): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.insertWorker(worker)
    }
  }

  override def updateWorker(worker: BurstCatalogApiWorker): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.updateWorker(worker)
    }
  }

  override def deleteWorker(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteWorker(pk)
    }
  }

  override def deleteWorkersForSite(siteFk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteWorkersForSite(siteFk)
    }
  }

  override def deleteWorkersForCell(cellFk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteWorkersForCell(cellFk)
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Sites
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override def allSites(limit: Option[Int]): Future[BurstCatalogApiSiteResponse] = {
    doSafelyIfRunning {
      thriftClient.allSites(limit)
    }
  }

  override def searchSites(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiSiteResponse] = {
    doSafelyIfRunning {
      thriftClient.searchSites(descriptor, limit)
    }
  }

  override def searchSitesByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiSiteResponse] = {
    doSafelyIfRunning {
      thriftClient.searchSitesByLabel(label, value, limit)
    }
  }

  override def findSiteByPk(pk: Long): Future[BurstCatalogApiSiteResponse] = {
    doSafelyIfRunning {
      thriftClient.findSiteByPk(pk)
    }
  }

  override def findSiteByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiSiteResponse] = {
    doSafelyIfRunning {
      thriftClient.findSiteByMoniker(moniker)
    }
  }

  override def insertSite(site: BurstCatalogApiSite): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.insertSite(site)
    }
  }

  override def updateSite(site: BurstCatalogApiSite): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.updateSite(site)
    }
  }

  override def deleteSite(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteSite(pk)
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Cells
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override def allCells(limit: Option[Int]): Future[BurstCatalogApiCellResponse] = {
    doSafelyIfRunning {
      thriftClient.allCells(limit)
    }
  }

  override def allCellsForSite(siteFk: Long, limit: Option[Int]): Future[BurstCatalogApiCellResponse] = {
    doSafelyIfRunning {
      thriftClient.allCellsForSite(siteFk, limit)
    }
  }

  override def searchCells(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiCellResponse] = {
    doSafelyIfRunning {
      thriftClient.searchCells(descriptor, limit)
    }
  }

  override def searchCellsByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiCellResponse] = {
    doSafelyIfRunning {
      thriftClient.searchCellsByLabel(label, value, limit)
    }
  }

  override def findCellByPk(pk: Long): Future[BurstCatalogApiCellResponse] = {
    doSafelyIfRunning {
      thriftClient.findCellByPk(pk)
    }
  }

  override def findCellByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiCellResponse] = {
    doSafelyIfRunning {
      thriftClient.findCellByMoniker(moniker)
    }
  }

  override def insertCell(cell: BurstCatalogApiCell): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.insertCell(cell)
    }
  }

  override def updateCell(cell: BurstCatalogApiCell): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.updateCell(cell)
    }
  }

  override def deleteCell(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteCell(pk)
    }
  }

  override def deleteCellsForSite(siteFk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteCellsForSite(siteFk)
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Domains
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override def allDomains(limit: Option[Int]): Future[BurstCatalogApiDomainResponse] = {
    doSafelyIfRunning {
      thriftClient.allDomains(limit)
    }
  }

  override def searchDomains(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiDomainResponse] = {
    doSafelyIfRunning {
      thriftClient.searchDomains(descriptor, limit)
    }
  }

  override def searchDomainsByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiDomainResponse] = {
    doSafelyIfRunning {
      thriftClient.searchDomainsByLabel(label, value, limit)
    }
  }

  override def findDomainByPk(pk: Long): Future[BurstCatalogApiDomainResponse] = {
    doSafelyIfRunning {
      thriftClient.findDomainByPk(pk)
    }
  }

  override def findDomainByUdk(udk: String): Future[BurstCatalogApiDomainResponse] = {
    doSafelyIfRunning {
      thriftClient.findDomainByUdk(udk)
    }
  }

  override def findDomainWithViewsByUdk(udk: String): Future[BurstCatalogApiDomainAndViewsResponse] = {
    doSafelyIfRunning {
      thriftClient.findDomainWithViewsByUdk(udk)
    }
  }

  override def findDomainByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiDomainResponse] = {
    doSafelyIfRunning {
      thriftClient.findDomainByMoniker(moniker)
    }
  }

  override def ensureDomain(domain: BurstCatalogApiDomain): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.ensureDomain(domain)
    }
  }

  override def deleteDomain(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteDomain(pk)
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // Views
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override def allViews(limit: Option[Int]): Future[BurstCatalogApiViewResponse] = {
    doSafelyIfRunning {
      thriftClient.allViews(limit)
    }
  }

  override def allViewsForDomain(domainPk: Long, limit: Option[Int]): Future[BurstCatalogApiViewResponse] = {
    doSafelyIfRunning {
      thriftClient.allViewsForDomain(domainPk, limit)
    }
  }

  override def searchViews(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiViewResponse] = {
    doSafelyIfRunning {
      thriftClient.searchViews(descriptor, limit)
    }
  }

  override def searchViewsByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiViewResponse] = {
    doSafelyIfRunning {
      thriftClient.searchViewsByLabel(label, value, limit)
    }
  }

  override def findViewByPk(pk: Long): Future[BurstCatalogApiViewResponse] = {
    doSafelyIfRunning {
      thriftClient.findViewByPk(pk)
    }
  }

  override def findViewByUdk(udk: String): Future[BurstCatalogApiViewResponse] = {
    doSafelyIfRunning {
      thriftClient.findViewByUdk(udk)
    }
  }

  override def findViewByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiViewResponse] = {
    doSafelyIfRunning {
      thriftClient.findViewByMoniker(moniker)
    }
  }

  override def ensureView(view: BurstCatalogApiView): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.ensureView(view)
    }
  }

  override def deleteView(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteView(pk)
    }
  }

  override def deleteViewsForDomain(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.deleteViewsForDomain(pk)
    }
  }

  override def insertDomain(domain: BurstCatalogApiDomain): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.insertDomain(domain)
    }
  }

  override def updateDomain(domain: BurstCatalogApiDomain): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.updateDomain(domain)
    }
  }

  override def insertView(view: BurstCatalogApiView): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.insertView(view)
    }
  }

  override def updateView(view: BurstCatalogApiView): Future[BurstCatalogApiEntityPkResponse] = {
    doSafelyIfRunning {
      thriftClient.updateView(view)
    }
  }
}
