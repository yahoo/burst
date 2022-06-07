/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.provider

import org.burstsys.api._
import org.burstsys.catalog.api.BurstCatalogApiQuery
import org.burstsys.catalog.api.BurstCatalogApiStatus.BurstCatalogApiSuccess
import org.burstsys.catalog.model.query._
import org.burstsys.catalog.{BurstMoniker, CatalogService, _}
import org.burstsys.relate.RelatePk
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import org.burstsys.vitals.logging._

trait CatalogQueryReactor extends CatalogService {

  self: CatalogServiceContext =>

  final override
  def findQueryByMoniker(moniker: BurstMoniker): Try[CatalogQuery] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          sql.queries.findEntityByMoniker(moniker) match {
            case None => Failure(VitalsException(s"query $moniker not found").fillInStackTrace())
            case Some(entity) => Success(entity)
          }
      }
    } else {
      try {
        val response = Await.result(apiClient.findQueryByMoniker(moniker), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.query.get)
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
  def findQueryByPk(key: Long): Try[CatalogQuery] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          sql.queries.findEntityByPk(key) match {
            case None => Failure(VitalsException(s"query $key not found").fillInStackTrace())
            case Some(entity) => Success(entity)
          }
      }
    } else {
      try {
        val response = Await.result(apiClient.findQueryByPk(key), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.query.get)
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
  def deleteQuery(key: Long): Try[RelatePk] = {
    if (modality.isServer) {
      sql.connection localTx {
        implicit session =>
          sql.queries.deleteEntity(key)
          Success(key)
      }
    } else {
      try {
        val response = Await.result(apiClient.deleteQuery(key), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.pk.get)
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
  def updateQuery(query: BurstCatalogApiQuery): Try[CatalogQuery] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.queries.updateEntity(query)
            Success(query)
        }
      } else {
        val response = Await.result(apiClient.updateQuery(query), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(query)
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
  def insertQuery(query: CatalogQuery): Try[RelatePk] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.queries.insertEntity(query))
        }
      } else {
        val response = Await.result(apiClient.insertQuery(query), requestTimeout)
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
  def allQueries(limit: Option[Int]): Try[Array[CatalogQuery]] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.queries.fetchAllEntities(limit).toArray)
        }
      } else {
        val response = Await.result(apiClient.allQueries(limit), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.queries.get.map(queryApiToProxy).toArray)
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
  def searchQueries(descriptor: String, limit: Option[Int]): Try[Array[CatalogQuery]] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.queries.searchEntitiesByMoniker(descriptor, limit).toArray)
        }
      } else {
        val response = Await.result(apiClient.searchQueries(descriptor, limit), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.queries.get.map(queryApiToProxy).toArray)
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
  def searchQueriesByLabel(label: String, value: Option[String], limit: Option[Int]): Try[Array[CatalogQuery]] = {
    try {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.queries.searchEntitiesByLabel(label, value, limit).toArray)
        }
      } else {
        val response = Await.result(apiClient.searchQueriesByLabel(label, value, limit), requestTimeout)
        response.result.status match {
          case BurstCatalogApiSuccess => Success(response.queries.get.map(queryApiToProxy).toArray)
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
}
