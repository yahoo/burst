/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api.server

import org.burstsys.catalog.BurstMoniker
import org.burstsys.catalog.api._
import org.burstsys.catalog.model.worker._
import com.twitter.util.Future

/**
  * Maps worker-related thrift endpoints to the catalog service
  */
trait CatalogWorkerApiReactor extends CatalogApi {

  self: CatalogApiServer =>

  final override
  def allWorkers(limit: Option[Int]): Future[BurstCatalogApiWorkerResponse] = {
    mapResponse(service allWorkers limit,
      (workers: Array[CatalogWorker]) => WorkerResponse(workers = Some(workers)),
      (f: BurstCatalogApiResult) => WorkerResponse(f)
    )
  }

  final override
  def allWorkersForSite(siteFk: Long, limit: Option[Int]): Future[BurstCatalogApiWorkerResponse] = {
    mapResponse(service.allWorkersForSite(siteFk, limit),
      (workers: Array[CatalogWorker]) => WorkerResponse(workers = Some(workers)),
      (f: BurstCatalogApiResult) => WorkerResponse(f)
    )
  }

  final override
  def allWorkersForCell(cellFk: Long, limit: Option[Int]): Future[BurstCatalogApiWorkerResponse] = {
    mapResponse(service.allWorkersForCell(cellFk, limit),
      (workers: Array[CatalogWorker]) => WorkerResponse(workers = Some(workers)),
      (f: BurstCatalogApiResult) => WorkerResponse(f)
    )
  }

  final override
  def findWorkerByPk(pk: Long): Future[BurstCatalogApiWorkerResponse] = {
    mapResponse(service.findWorkerByPk(pk),
      (entity: CatalogWorker) => WorkerResponse(Some(entity)),
      (f: BurstCatalogApiResult) => WorkerResponse(f)
    )
  }

  final override
  def findWorkerByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiWorkerResponse] = {
    mapResponse(service.findWorkerByMoniker(moniker),
      (entity: CatalogWorker) => WorkerResponse(Some(entity)),
      (f: BurstCatalogApiResult) => WorkerResponse(f)
    )
  }

  final override
  def insertWorker(Worker: BurstCatalogApiWorker): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.insertWorker(Worker),
      (entity: Long) => EntityResponse(Some(entity)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def updateWorker(Worker: BurstCatalogApiWorker): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.updateWorker(Worker),
      (entity: CatalogWorker) => EntityResponse(Some(entity.pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteWorker(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteWorker(pk),
      (key: Long) => EntityResponse(Some(key)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteWorkersForSite(siteFk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteWorkersForSite(siteFk),
      (key: Long) => EntityResponse(Some(key)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteWorkersForCell(cellFk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteWorkersForCell(cellFk),
      (key: Long) => EntityResponse(Some(key)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def searchWorkers(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiWorkerResponse] = {
    mapResponse(service.searchWorkers(descriptor, limit),
      (workers: Array[CatalogWorker]) => WorkerResponse(workers = Some(workers)),
      (f: BurstCatalogApiResult) => WorkerResponse(f)
    )
  }

  final override
  def searchWorkersByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiWorkerResponse] = {
    mapResponse(service.searchWorkersByLabel(label, value, limit),
      (workers: Array[CatalogWorker]) => WorkerResponse(workers = Some(workers)),
      (f: BurstCatalogApiResult) => WorkerResponse(f)
    )
  }
}

