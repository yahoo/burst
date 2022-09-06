/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api.server

import org.burstsys.catalog.BurstMoniker
import org.burstsys.catalog.api._
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.view._
import com.twitter.util.Future

/**
  * Maps domain-related thrift endpoints to the catalog service
  */
trait CatalogDomainApiReactor extends CatalogApi {

  self: CatalogApiServer =>

  final override
  def allDomains(limit: Option[Int]): Future[BurstCatalogApiDomainResponse] = {
    mapResponse(service.allDomains(limit),
      (domains: Array[CatalogDomain]) => DomainResponse(domains = Some(domains.toIndexedSeq)),
      (f: BurstCatalogApiResult) => DomainResponse(f)
    )
  }

  final override
  def searchDomains(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiDomainResponse] = {
    mapResponse(service.searchDomains(descriptor, limit),
      (domains: Array[CatalogDomain]) => DomainResponse(domains = Some(domains.toIndexedSeq)),
      (f: BurstCatalogApiResult) => DomainResponse(f)
    )
  }

  final override
  def searchDomainsByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiDomainResponse] = {
    mapResponse(service.searchDomainsByLabel(label, value, limit),
      (domains: Array[CatalogDomain]) => DomainResponse(domains = Some(domains.toIndexedSeq)),
      (f: BurstCatalogApiResult) => DomainResponse(f)
    )
  }

  final override
  def findDomainByPk(pk: Long): Future[BurstCatalogApiDomainResponse] = {
    mapResponse(service.findDomainByPk(pk),
      (entity: CatalogDomain) => DomainResponse(Some(entity)),
      (f: BurstCatalogApiResult) => DomainResponse(f)
    )
  }

  final override
  def findDomainByUdk(udk: String): Future[BurstCatalogApiDomainResponse] = {
    mapResponse(service.findDomainByUdk(udk),
      (entity: CatalogDomain) => DomainResponse(Some(entity)),
      (f: BurstCatalogApiResult) => DomainResponse(f)
    )
  }

  /** Returns the domain identified by UDK, with all its views. */
  final override
  def findDomainWithViewsByUdk(udk: String): Future[BurstCatalogApiDomainAndViewsResponse] = {
    mapResponse(service.findDomainWithViewsByUdk(udk),
      (dvPair: (CatalogDomain, Seq[CatalogView])) => DomainViewResponse(Some(dvPair._1), Some(dvPair._2)),
      (f: BurstCatalogApiResult) => BurstCatalogApiDomainAndViewsResponse(f)
    )
  }

  final override
  def findDomainByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiDomainResponse] = {
    mapResponse(service.findDomainByMoniker(moniker),
      (entity: CatalogDomain) => DomainResponse(Some(entity)),
      (f: BurstCatalogApiResult) => DomainResponse(f)
    )
  }

  final override
  def ensureDomain(domain: BurstCatalogApiDomain): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.ensureDomain(domain),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  override def insertDomain(domain: BurstCatalogApiDomain): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.insertDomain(domain),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  override def updateDomain(domain: BurstCatalogApiDomain): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.updateDomain(domain),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteDomain(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteDomain(pk),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f))
  }

}
