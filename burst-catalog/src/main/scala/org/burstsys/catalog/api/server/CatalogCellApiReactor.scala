/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api.server

import org.burstsys.catalog.BurstMoniker
import org.burstsys.catalog.api.BurstCatalogApiStatus.BurstCatalogApiSuccess
import org.burstsys.catalog.api._
import org.burstsys.catalog.model.cell._
import com.twitter.util.Future

/**
  * Maps cell-related thrift endpoints to the catalog service
  */
trait CatalogCellApiReactor {

  self: CatalogApiServer =>

  final override
  def allCells(limit: Option[Int]): Future[BurstCatalogApiCellResponse] = {
    mapResponse(service.allCells(limit),
      (cells: Array[CatalogCell]) => CellResponse(cells = Some(cells)),
      (f: BurstCatalogApiResult) => CellResponse(f)
    )
  }

  final override
  def allCellsForSite(siteFk: Long, limit: Option[Int]): Future[BurstCatalogApiCellResponse] = {
    mapResponse(service.allCellsForSite(siteFk, limit),
      (cells: Array[CatalogCell]) => CellResponse(cells = Some(cells)),
      (f: BurstCatalogApiResult) => CellResponse(f)
    )
  }

  final override
  def findCellByPk(pk: Long): Future[BurstCatalogApiCellResponse] = {
    mapResponse(service.findCellByPk(pk),
      (cell: CatalogCell) => CellResponse(Some(cell)),
      (f: BurstCatalogApiResult) => CellResponse(f)
    )
  }

  final override
  def findCellByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiCellResponse] = {
    mapResponse(service.findCellByMoniker(moniker),
      (cell: CatalogCell) => CellResponse(Some(cell)),
      (f: BurstCatalogApiResult) => CellResponse(f)
    )
  }

  final override
  def insertCell(cell: BurstCatalogApiCell): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.insertCell(cell),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def updateCell(cell: BurstCatalogApiCell): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.updateCell(cell),
      (cell: CatalogCell) => EntityResponse(Some(cell.pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteCell(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteCell(pk),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteCellsForSite(siteFk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteCellsForSite(siteFk),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def searchCells(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiCellResponse] = {
    mapResponse(service.searchCells(descriptor, limit),
      (cells: Array[CatalogCell]) => CellResponse(cells = Some(cells)),
      (f: BurstCatalogApiResult) => CellResponse(f)
    )
  }

  final override
  def searchCellsByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiCellResponse] = {
    mapResponse(service.searchCellsByLabel(label, value, limit),
      (cells: Array[CatalogCell]) => CellResponse(cells = Some(cells)),
      (f: BurstCatalogApiResult) => CellResponse(f)
    )
  }
}

