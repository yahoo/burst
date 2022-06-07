/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.provider

import org.burstsys.api._
import org.burstsys.catalog.api.BurstCatalogApiCell
import org.burstsys.catalog.api.BurstCatalogApiStatus.BurstCatalogApiSuccess
import org.burstsys.catalog.model.cell._
import org.burstsys.catalog.{CatalogService, _}
import org.burstsys.relate.RelatePk
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import org.burstsys.vitals.logging._

trait CatalogCellReactor extends CatalogService {

  self: CatalogServiceContext =>

  final override
  def findCellByMoniker(moniker: String): Try[CatalogCell] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          sql.cells.findEntityByMoniker(moniker) match {
            case None => Failure(VitalsException(s"cell moniker=$moniker not found").fillInStackTrace())
            case Some(entity) => Success(entity)
          }
      }
    } else {
      try {
        val response = Await.result(apiClient.findCellByMoniker(moniker), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.cell.get)
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
  def findCellByPk(key: Long): Try[CatalogCell] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          sql.cells.findEntityByPk(key) match {
            case None => Failure(VitalsException(s"cell $key not found").fillInStackTrace())
            case Some(entity) => Success(entity)
          }
      }
    } else {
      try {
        val response = Await.result(apiClient.findCellByPk(key), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.cell.get)
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
  def insertCell(cell: CatalogCell): Try[RelatePk] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.cells.insertEntity(cell))
        }
      } else {
        val response = Await.result(apiClient.insertCell(cell), requestTimeout)
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
  def deleteCell(key: Long): Try[RelatePk] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            // cascading delete
            deleteWorkersForCell(key)
            deleteMastersForCell(key)
            sql.cells.deleteEntity(key)
            Success(key)
        }
      } else {
        val response = Await.result(apiClient.deleteCell(key), requestTimeout)
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
  def deleteCellsForSite(siteFk: RelatePk): Try[RelatePk] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.cells.deleteCellsForSite(siteFk)
            Success(siteFk)
        }
      } else {
        val response = Await.result(apiClient.deleteCellsForSite(siteFk), requestTimeout)
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
  def updateCell(cell: BurstCatalogApiCell): Try[CatalogCell] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.cells.updateEntity(cell)
            Success(cell)
        }
      } else {
        val response = Await.result(apiClient.updateCell(cell), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(cell)
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
  def allCells(limit: Option[Int]): Try[Array[CatalogCell]] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.cells.fetchAllEntities(limit).toArray)
        }
      } else {
        val response = Await.result(apiClient.allCells(limit), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.cells.get.map(cellApiToProxy).toArray)
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
  def allCellsForSite(siteFk: RelatePk, limit: Option[Int]): Try[Array[CatalogCell]] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.cells.allCellsForSite(siteFk, limit).toArray)
        }
      } else {
        val response = Await.result(apiClient.allCellsForSite(siteFk, limit), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.cells.get.map(cellApiToProxy).toArray)
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
  def searchCells(descriptor: String, limit: Option[Int]): Try[Array[CatalogCell]] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.cells.searchEntitiesByMoniker(descriptor, limit).toArray)
        }
      } else {
        val response = Await.result(apiClient.searchCells(descriptor, limit), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.cells.get.map(cellApiToProxy).toArray)
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
  def searchCellsByLabel(label: String, value: Option[String], limit: Option[Int]): Try[Array[CatalogCell]] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.cells.searchEntitiesByLabel(label, value, limit).toArray)
        }
      } else {
        val response = Await.result(apiClient.searchCellsByLabel(label, value, limit), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.cells.get.map(cellApiToProxy).toArray)
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
