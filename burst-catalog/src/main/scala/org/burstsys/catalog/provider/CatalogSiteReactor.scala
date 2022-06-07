/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.provider

import org.burstsys.api._
import org.burstsys.catalog.api.BurstCatalogApiSite
import org.burstsys.catalog.api.BurstCatalogApiStatus.BurstCatalogApiSuccess
import org.burstsys.catalog.model.site._
import org.burstsys.catalog.{CatalogService, _}
import org.burstsys.relate.RelatePk
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import org.burstsys.vitals.logging._

trait CatalogSiteReactor extends CatalogService {

  self: CatalogServiceContext =>

  final override
  def findSiteByMoniker(moniker: String): Try[CatalogSite] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          sql.sites.findEntityByMoniker(moniker) match {
            case None => Failure(VitalsException(s"site moniker=$moniker not found").fillInStackTrace())
            case Some(entity) => Success(entity)
          }
      }
    } else {
      try {
        val response = Await.result(apiClient.findSiteByMoniker(moniker), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.site.get)
          case _ =>
            Failure(VitalsException(s"${response.result.status} ${response.result.message}").fillInStackTrace())
        }
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(t)
          Failure(t)
      }
    }
  }

  final override
  def findSiteByPk(key: Long): Try[CatalogSite] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          sql.sites.findEntityByPk(key) match {
            case None => Failure(VitalsException(s"site $key not found").fillInStackTrace())
            case Some(entity) => Success(entity)
          }
      }
    } else {
      try {
        val response = Await.result(apiClient.findSiteByPk(key), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.site.get)
          case _ =>
            Failure(VitalsException(s"${response.result.status} ${response.result.message}").fillInStackTrace())
        }
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(t)
          Failure(t)
      }
    }
  }

  final override
  def insertSite(site: CatalogSite): Try[RelatePk] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.sites.insertEntity(site))
        }
      } else {
        val response = Await.result(apiClient.insertSite(site), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.pk.get)
          case _ =>
            Failure(VitalsException(s"${response.result.status} ${response.result.message}").fillInStackTrace())
        }
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        Failure(t)
    }
  }

  final override
  def deleteSite(key: Long): Try[RelatePk] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            // cascading delete
            deleteWorkersForSite(key)
            deleteMastersForSite(key)
            deleteCellsForSite(key)
            sql.sites.deleteEntity(key)
            Success(key)
        }
      } else {
        val response = Await.result(apiClient.deleteSite(key), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.pk.get)
          case _ =>
            Failure(VitalsException(s"${response.result.status} ${response.result.message}").fillInStackTrace())
        }
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        Failure(t)
    }
  }

  final override
  def updateSite(site: BurstCatalogApiSite): Try[CatalogSite] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.sites.updateEntity(site)
            Success(site)
        }
      } else {
        val response = Await.result(apiClient.updateSite(site), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(site)
          case _ =>
            Failure(VitalsException(s"${response.result.status} ${response.result.message}").fillInStackTrace())
        }
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        Failure(t)
    }
  }

  final override
  def allSites(limit: Option[Int]): Try[Array[CatalogSite]] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.sites.fetchAllEntities(limit).toArray)
        }
      } else {
        val response = Await.result(apiClient.allSites(limit), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.sites.get.map(siteApiToProxy).toArray)
          case _ =>
            Failure(VitalsException(s"${response.result.status} ${response.result.message}").fillInStackTrace())
        }
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        Failure(t)
    }
  }

  final override
  def searchSites(descriptor: String, limit: Option[Int]): Try[Array[CatalogSite]] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.sites.searchEntitiesByMoniker(descriptor, limit).toArray)
        }
      } else {
        val response = Await.result(apiClient.searchSites(descriptor, limit), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.sites.get.map(siteApiToProxy).toArray)
          case _ => Failure(VitalsException(s"${response.result.status} ${response.result.message}").fillInStackTrace())
        }
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        Failure(t)
    }
  }

  final override
  def searchSitesByLabel(label: String, value: Option[String], limit: Option[Int]): Try[Array[CatalogSite]] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.sites.searchEntitiesByLabel(label, value, limit).toArray)
        }
      } else {
        val response = Await.result(apiClient.searchSitesByLabel(label, value, limit), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.sites.get.map(siteApiToProxy).toArray)
          case _ => Failure(VitalsException(s"${response.result.status} ${response.result.message}").fillInStackTrace())
        }
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        Failure(t)
    }
  }
}
