/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api.client

import com.twitter.util.Future
import org.burstsys.api.BurstApiClient
import org.burstsys.catalog.api._
import org.burstsys.catalog.{BurstMoniker, CatalogService}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.language.postfixOps

/**
  * Catalog thrift repeater
  */
final case
class CatalogApiClient(service: CatalogService) extends BurstApiClient[BurstCatalogApiService.MethodPerEndpoint]
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
