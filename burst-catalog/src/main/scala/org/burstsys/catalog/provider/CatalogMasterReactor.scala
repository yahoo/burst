/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.provider

import org.burstsys.api._
import org.burstsys.catalog.api.BurstCatalogApiMasterResponse
import org.burstsys.catalog.model.master._
import org.burstsys.catalog.{CatalogService, _}
import org.burstsys.relate.RelatePk
import org.burstsys.vitals.errors.VitalsException

import scala.concurrent.Awaitable
import scala.util.{Failure, Success, Try}

trait CatalogMasterReactor extends CatalogService {

  self: CatalogServiceContext =>

  type MasterResponse = BurstCatalogApiMasterResponse

  def mapMasterResponse[T](future: Awaitable[MasterResponse], transform: MasterResponse => T): Try[T] =
    mapThriftResult[MasterResponse, T](future, _.result, transform)


  final override
  def findMasterByMoniker(moniker: String): Try[CatalogMaster] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.masters.findEntityByMoniker(moniker) match {
              case None => Failure(VitalsException(s"worker $moniker not found").fillInStackTrace())
              case Some(entity) => Success(entity)
            }
        }
      } else mapMasterResponse(apiClient.findMasterByMoniker(moniker), _.master.get)
    }
  }


  final override
  def findMasterByPk(key: Long): Try[CatalogMaster] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.masters.findEntityByPk(key) match {
              case None => Failure(VitalsException(s"master $key not found").fillInStackTrace())
              case Some(entity) => Success(entity)
            }
        }
      } else mapMasterResponse(apiClient.findMasterByPk(key), _.master.get)
    }
  }

  final override
  def insertMaster(master: CatalogMaster): Try[RelatePk] = resultOrFailure {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          Success(sql.masters.insertEntity(master))
      }
    } else mapEntityResponse(apiClient.insertMaster(master))
  }

  final override
  def deleteMaster(key: Long): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.masters.deleteEntity(key)
            Success(key)
        }
      } else mapEntityResponse(apiClient.deleteMaster(key))
    }
  }

  final override
  def deleteMastersForSite(siteFk: RelatePk): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.masters.deleteMastersForSite(siteFk)
            Success(siteFk)
        }
      } else mapEntityResponse(apiClient.deleteMastersForSite(siteFk))
    }
  }

  final override
  def deleteMastersForCell(cellFk: RelatePk): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.masters.deleteMastersForCell(cellFk)
            Success(cellFk)
        }
      } else mapEntityResponse(apiClient.deleteMastersForCell(cellFk))
    }
  }

  final override
  def updateMaster(master: CatalogMaster): Try[CatalogMaster] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.masters.updateEntity(master)
            Success(master)
        }
      } else mapEntityResponse(apiClient.updateMaster(master)) match {
        case Success(_) => Success(master)
        case Failure(x) => Failure(x)
      }
    }
  }

  final override
  def allMasters(limit: Option[Int]): Try[Array[CatalogMaster]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.masters.fetchAllEntities(limit).toArray)
        }
      } else mapMasterResponse(apiClient.allMasters(limit), _.masters.get.map(masterApiToProxy).toArray)
    }
  }

  final override
  def allMastersForSite(siteFk: RelatePk, limit: Option[Int]): Try[Array[CatalogMaster]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.masters.allMastersForSite(siteFk, limit).toArray)
        }
      } else mapMasterResponse(apiClient.allMastersForSite(siteFk, limit), _.masters.get.map(masterApiToProxy).toArray)
    }
  }

  final override
  def allMastersForCell(cellFk: RelatePk, limit: Option[Int]): Try[Array[CatalogMaster]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.masters.allMastersForCell(cellFk, limit).toArray)
        }
      } else mapMasterResponse(apiClient.allMastersForCell(cellFk, limit), _.masters.get.map(masterApiToProxy).toArray)
    }
  }

  final override
  def searchMasters(descriptor: String, limit: Option[Int]): Try[Array[CatalogMaster]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.masters.searchEntitiesByMoniker(descriptor, limit).toArray)
        }
      } else mapMasterResponse(apiClient.searchMasters(descriptor, limit), _.masters.get.map(masterApiToProxy).toArray)
    }
  }

  final override
  def searchMastersByLabel(label: String, value: Option[String], limit: Option[Int]): Try[Array[CatalogMaster]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.masters.searchEntitiesByLabel(label, value, limit).toArray)
        }
      } else mapMasterResponse(apiClient.searchMastersByLabel(label, value, limit), _.masters.get.map(masterApiToProxy).toArray)
    }
  }
}
