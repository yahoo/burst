/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.provider

import org.burstsys.api._
import org.burstsys.catalog.api.BurstCatalogApiWorkerResponse
import org.burstsys.catalog.model.worker._
import org.burstsys.catalog.{CatalogService, _}
import org.burstsys.relate.RelatePk
import org.burstsys.vitals.errors.VitalsException

import scala.concurrent.Awaitable
import scala.util.{Failure, Success, Try}

trait CatalogWorkerReactor extends CatalogService {

  self: CatalogServiceContext =>

  type WorkerResponse = BurstCatalogApiWorkerResponse

  def mapWorkerResponse[T](future: Awaitable[WorkerResponse], transform: WorkerResponse => T): Try[T] =
    mapThriftResult[WorkerResponse, T](future, _.result, transform)


  final override
  def findWorkerByMoniker(moniker: String): Try[CatalogWorker] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.workers.findEntityByMoniker(moniker) match {
              case None => Failure(VitalsException(s"worker $moniker not found").fillInStackTrace())
              case Some(entity) => Success(entity)
            }
        }
      } else mapWorkerResponse(apiClient.findWorkerByMoniker(moniker), _.worker.get)
    }
  }


  final override
  def findWorkerByPk(key: Long): Try[CatalogWorker] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.workers.findEntityByPk(key) match {
              case None => Failure(VitalsException(s"worker $key not found").fillInStackTrace())
              case Some(entity) => Success(entity)
            }
        }
      } else mapWorkerResponse(apiClient.findWorkerByPk(key), _.worker.get)
    }
  }

  final override
  def insertWorker(worker: CatalogWorker): Try[RelatePk] = resultOrFailure {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          Success(sql.workers.insertEntity(worker))
      }
    } else mapEntityResponse(apiClient.insertWorker(worker))
  }

  final override
  def deleteWorker(key: Long): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.workers.deleteEntity(key)
            Success(key)
        }
      } else mapEntityResponse(apiClient.deleteWorker(key))
    }
  }

  final override
  def deleteWorkersForSite(siteFk: RelatePk): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.workers.deleteWorkersForSite(siteFk)
            Success(siteFk)
        }
      } else mapEntityResponse(apiClient.deleteWorkersForSite(siteFk))
    }
  }

  final override
  def deleteWorkersForCell(cellFk: RelatePk): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.workers.deleteWorkersForCell(cellFk)
            Success(cellFk)
        }
      } else mapEntityResponse(apiClient.deleteWorkersForCell(cellFk))
    }
  }

  final override
  def updateWorker(worker: CatalogWorker): Try[CatalogWorker] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.workers.updateEntity(worker)
            Success(worker)
        }
      } else mapEntityResponse(apiClient.updateWorker(worker)) match {
        case Success(_) => Success(worker)
        case Failure(x) => Failure(x)
      }
    }
  }

  final override
  def allWorkers(limit: Option[Int]): Try[Array[CatalogWorker]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.workers.fetchAllEntities(limit).toArray)
        }
      } else mapWorkerResponse(apiClient.allWorkers(limit), _.workers.get.map(workerApiToProxy).toArray)
    }
  }

  final override
  def allWorkersForSite(siteFk: RelatePk, limit: Option[Int]): Try[Array[CatalogWorker]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.workers.allWorkersForSite(siteFk, limit).toArray)
        }
      } else mapWorkerResponse(apiClient.allWorkersForSite(siteFk, limit), _.workers.get.map(workerApiToProxy).toArray)
    }
  }

  final override
  def allWorkersForCell(cellFk: RelatePk, limit: Option[Int]): Try[Array[CatalogWorker]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.workers.allWorkersForCell(cellFk, limit).toArray)
        }
      } else mapWorkerResponse(apiClient.allWorkersForCell(cellFk, limit), _.workers.get.map(workerApiToProxy).toArray)
    }
  }

  final override
  def searchWorkers(descriptor: String, limit: Option[Int]): Try[Array[CatalogWorker]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.workers.searchEntitiesByMoniker(descriptor, limit).toArray)
        }
      } else mapWorkerResponse(apiClient.searchWorkers(descriptor, limit), _.workers.get.map(workerApiToProxy).toArray)
    }
  }

  final override
  def searchWorkersByLabel(label: String, value: Option[String], limit: Option[Int]): Try[Array[CatalogWorker]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.workers.searchEntitiesByLabel(label, value, limit).toArray)
        }
      } else mapWorkerResponse(apiClient.searchWorkersByLabel(label, value, limit), _.workers.get.map(workerApiToProxy).toArray)
    }
  }
}
