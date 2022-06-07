/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api.server

import org.burstsys.catalog.BurstMoniker
import org.burstsys.catalog.api._
import org.burstsys.catalog.model.master._
import com.twitter.util.Future

/**
  * Maps master-related thrift endpoints to the catalog service
  */
trait CatalogMasterApiReactor extends CatalogApi {

  self: CatalogApiServer =>

  final override
  def allMasters(limit: Option[Int]): Future[BurstCatalogApiMasterResponse] = {
    mapResponse(service.allMasters(limit),
      (masters: Array[CatalogMaster]) => MasterResponse(masters = Some(masters)),
      (f: BurstCatalogApiResult) => MasterResponse(f)
    )
  }

  final override
  def allMastersForSite(siteFk: Long, limit: Option[Int]): Future[BurstCatalogApiMasterResponse] = {
    mapResponse(service.allMastersForSite(siteFk, limit),
      (masters: Array[CatalogMaster]) => MasterResponse(masters = Some(masters)),
      (f: BurstCatalogApiResult) => MasterResponse(f)
    )
  }

  final override
  def allMastersForCell(cellFk: Long, limit: Option[Int]): Future[BurstCatalogApiMasterResponse] = {
    mapResponse(service.allMastersForCell(cellFk, limit),
      (masters: Array[CatalogMaster]) => MasterResponse(masters = Some(masters)),
      (f: BurstCatalogApiResult) => MasterResponse(f)
    )
  }

  final override
  def findMasterByPk(pk: Long): Future[BurstCatalogApiMasterResponse] = {
    mapResponse(service.findMasterByPk(pk),
      (entity: CatalogMaster) => MasterResponse(Some(entity)),
      (f: BurstCatalogApiResult) => MasterResponse(f)
    )
  }

  final override
  def findMasterByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiMasterResponse] = {
    mapResponse(service.findMasterByMoniker(moniker),
      (entity: CatalogMaster) => MasterResponse(Some(entity)),
      (f: BurstCatalogApiResult) => MasterResponse(f)
    )
  }

  final override
  def insertMaster(master: BurstCatalogApiMaster): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.insertMaster(master),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def updateMaster(master: BurstCatalogApiMaster): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.updateMaster(master),
      (entity: CatalogMaster) => EntityResponse(Some(entity.pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteMaster(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteMaster(pk),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteMastersForSite(siteFk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteMastersForSite(siteFk),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteMastersForCell(cellFk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteMastersForCell(cellFk),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def searchMasters(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiMasterResponse] = {
    mapResponse(service.searchMasters(descriptor, limit),
      (masters: Array[CatalogMaster]) => MasterResponse(masters = Some(masters)),
      (f: BurstCatalogApiResult) => MasterResponse(f)
    )
  }

  final override
  def searchMastersByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiMasterResponse] = {
    mapResponse(service.searchMastersByLabel(label, value, limit),
      (masters: Array[CatalogMaster]) => MasterResponse(masters = Some(masters)),
      (f: BurstCatalogApiResult) => MasterResponse(f)
    )
  }
}

