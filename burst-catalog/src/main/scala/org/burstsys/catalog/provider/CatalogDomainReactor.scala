/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.provider

import org.burstsys.api._
import org.burstsys.catalog.api.{BurstCatalogApiDomainAndViewsResponse, BurstCatalogApiDomainResponse}
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.view._
import org.burstsys.catalog.{CatalogService, _}
import org.burstsys.relate.RelatePk
import org.burstsys.vitals.errors.VitalsException

import scala.concurrent.Awaitable
import scala.util.{Failure, Success, Try}

trait CatalogDomainReactor extends CatalogService {

  self: CatalogServiceContext =>

  type DomainResponse = BurstCatalogApiDomainResponse
  type DomainViewResponse = BurstCatalogApiDomainAndViewsResponse

  def mapDomainResponse[T](future: Awaitable[DomainResponse], transform: DomainResponse => T): Try[T] =
    mapThriftResult[DomainResponse, T](future, _.result, transform)

  def mapDomainViewResponse[T](future: Awaitable[DomainViewResponse], transform: DomainViewResponse => T): Try[T] =
    mapThriftResult[DomainViewResponse, T](future, _.result, transform)

  final override
  def findDomainByMoniker(moniker: String): Try[CatalogDomain] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          sql.domains.findEntityByMoniker(moniker) match {
            case None => Failure(VitalsException(s"domain $moniker not found").fillInStackTrace())
            case Some(entity) => Success(entity)
          }
      }
    } else resultOrFailure(mapDomainResponse(apiClient.findDomainByMoniker(moniker), _.domain.get))
  }

  final override
  def findDomainByPk(pk: Long): Try[CatalogDomain] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          sql.domains.findEntityByPk(pk) match {
            case None => Failure(VitalsException(s"domain $pk not found").fillInStackTrace())
            case Some(entity) => Success(entity)
          }
      }
    } else resultOrFailure(mapDomainResponse(apiClient.findDomainByPk(pk), _.domain.get))
  }

  final override
  def findDomainByUdk(udk: String): Try[CatalogDomain] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          sql.domains.findEntityByUdk(udk) match {
            case None => Failure(CatalogExceptions.CatalogNotFoundException(s"domain $udk not found by UDK"))
            case Some(entity) => Success(entity)
          }
      }
    } else resultOrFailure(mapDomainResponse(apiClient.findDomainByUdk(udk), _.domain.get))
  }

  final override
  def findDomainWithViewsByUdk(udk: String): Try[(CatalogDomain, Seq[CatalogView])] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session => {
          sql.domains.findEntityByUdk(udk) match {
            case None => Failure(CatalogExceptions.CatalogNotFoundException(s"domain $udk not found by UDK"))
            case Some(domain) => Success((domain, sql.views.allViewsForDomain(domain.pk, limit = None)))
          }
        }
      }
    } else {
      resultOrFailure {
        mapDomainViewResponse(apiClient.findDomainWithViewsByUdk(udk),
          response => (response.domain.get, response.views.get.map(_.asInstanceOf[CatalogView]).toSeq)
        )
      }
    }
  }

  final override
  def ensureDomain(domain: CatalogDomain): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            val (updated, didUpdate) = sql.domains.upsertEntity(domain)
            if (didUpdate) {
              updateViewGenerationsForDomain(updated.pk)
            }
            Success(updated.pk)
        }
      } else mapEntityResponse(apiClient.ensureDomain(domain))
    }
  }

  final override
  def insertDomain(domain: CatalogDomain): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            val pk = sql.domains.insertEntity(domain)
            Success(pk)
        }
      } else mapEntityResponse(apiClient.insertDomain(domain))
    }
  }

  final override
  def updateDomain(domain: CatalogDomain): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            val pk = sql.domains.updateEntity(domain)
            updateViewGenerationsForDomain(pk)
            Success(pk)
        }
      } else mapEntityResponse(apiClient.updateDomain(domain))
    }
  }


  final override
  def deleteDomain(pk: Long): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            // first delete all the dependent views
            deleteViewsForDomain(pk)
            sql.domains.deleteEntity(pk)
            Success(pk)
        }
      } else mapEntityResponse(apiClient.deleteDomain(pk))
    }
  }

  final override
  def allDomains(limit: Option[Int]): Try[Array[CatalogDomain]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.domains.fetchAllEntities(limit).toArray)
        }
      } else mapDomainResponse(apiClient.allDomains(limit), _.domains.get.map(domainApiToProxy).toArray)
    }
  }

  final override
  def searchDomains(domainDescriptor: String, limit: Option[Int]): Try[Array[CatalogDomain]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.domains.searchEntitiesByMoniker(domainDescriptor, limit).toArray)
        }
      } else mapDomainResponse(apiClient.searchDomains(domainDescriptor, limit), _.domains.get.map(domainApiToProxy).toArray)
    }
  }

  final override
  def searchDomainsByLabel(label: String, value: Option[String], limit: Option[Int]): Try[Array[CatalogDomain]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.domains.searchEntitiesByLabel(label, value, limit).toArray)
        }
      } else mapDomainResponse(apiClient.searchDomainsByLabel(label, value, limit), _.domains.get.map(domainApiToProxy).toArray)
    }
  }

}
