/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api.server

import org.burstsys.catalog.BurstMoniker
import org.burstsys.catalog.api._
import org.burstsys.catalog.model.site._
import com.twitter.util.Future

/**
  * Maps site-related thrift endpoints to the catalog service
  */
trait CatalogSiteApiReactor extends CatalogApi {

  self: CatalogApiServer =>

  final override
  def allSites(limit: Option[Int]): Future[BurstCatalogApiSiteResponse] = {
    mapResponse(service.allSites(limit),
      (sites: Array[CatalogSite]) => SiteResponse(sites = Some(sites)),
      (f: BurstCatalogApiResult) => SiteResponse(f)
    )
  }

  final override
  def findSiteByPk(pk: Long): Future[BurstCatalogApiSiteResponse] = {
    mapResponse(service.findSiteByPk(pk),
      (entity: CatalogSite) => SiteResponse(Some(entity)),
      (f: BurstCatalogApiResult) => SiteResponse(f)
    )
  }

  final override
  def findSiteByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiSiteResponse] = {
    mapResponse(service.findSiteByMoniker(moniker),
      (entity: CatalogSite) => SiteResponse(Some(entity)),
      (f: BurstCatalogApiResult) => SiteResponse(f)
    )
  }

  final override
  def insertSite(site: BurstCatalogApiSite): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.insertSite(site),
      (entity: Long) => EntityResponse(Some(entity)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def updateSite(site: BurstCatalogApiSite): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.updateSite(site),
      (entity: CatalogSite) => EntityResponse(Some(entity.pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteSite(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteSite(pk),
      (entity: Long) => EntityResponse(Some(entity)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def searchSites(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiSiteResponse] = {
    mapResponse(service.searchSites(descriptor, limit),
      (sites: Array[CatalogSite]) => SiteResponse(sites = Some(sites)),
      (f: BurstCatalogApiResult) => SiteResponse(f)
    )
  }

  final override
  def searchSitesByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiSiteResponse] = {
    mapResponse(service.searchSitesByLabel(label, value, limit),
      (sites: Array[CatalogSite]) => SiteResponse(sites = Some(sites)),
      (f: BurstCatalogApiResult) => SiteResponse(f)
    )
  }
}


