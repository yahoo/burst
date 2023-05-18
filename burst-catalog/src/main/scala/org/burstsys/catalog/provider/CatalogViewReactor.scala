/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.provider


import org.burstsys.api._
import org.burstsys.catalog.CatalogExceptions.CatalogNotFoundException
import org.burstsys.catalog._
import org.burstsys.catalog.api.BurstCatalogApiViewResponse
import org.burstsys.catalog.configuration.burstCatalogGenerationStaleMsProperty
import org.burstsys.catalog.model.view._
import org.burstsys.relate.RelateExceptions.BurstUnknownPrimaryKeyException
import org.burstsys.relate.RelatePk
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.properties._
import scalikejdbc.DBSession

import scala.concurrent.Awaitable
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait CatalogViewReactor extends CatalogService {

  self: CatalogServiceContext =>

  type ViewResponse = BurstCatalogApiViewResponse

  def mapViewResponse[T](future: Awaitable[ViewResponse], transform: ViewResponse => T): Try[T] =
    mapThriftResult[ViewResponse, T](future, _.result, transform)

  final override
  def findViewByMoniker(moniker: BurstMoniker): Try[CatalogView] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.views.findEntityByMoniker(moniker) match {
              case None => Failure(VitalsException(s"view $moniker not found").fillInStackTrace())
              case Some(entity) => Success(updateGenClockAndEarliestLoadTimeIfStale(entity))
            }
        }
      } else mapViewResponse(apiClient.findViewByMoniker(moniker), _.view.get)
    }
  }

  final override
  def findViewByPk(key: RelatePk): Try[CatalogView] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.views.findEntityByPk(key) match {
              case None => Failure(VitalsException(s"view $key not found").fillInStackTrace())
              case Some(entity) => Success(updateGenClockAndEarliestLoadTimeIfStale(entity))
            }
        }
      } else mapViewResponse(apiClient.findViewByPk(key), _.view.get)
    }
  }

  final override
  def findViewByUdk(udk: String): Try[CatalogView] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.views.findEntityByUdkIn("", udk) match {
              case None => Failure(CatalogExceptions.CatalogInvalidException(s"view $udk not found by UDK"))
              case Some(entity) => Success(updateGenClockAndEarliestLoadTimeIfStale(entity))
            }
        }
      } else mapViewResponse(apiClient.findViewByUdk(udk), _.view.get)
    }
  }

  def allViewsForDomain(domainPk: RelatePk, limit: Option[Int]): Try[Array[CatalogView]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(
              sql.views.allViewsForDomain(domainPk, limit)
                .map(updateGenClockAndEarliestLoadTimeIfStale)
                .toArray
            )
        }
      } else mapViewResponse(apiClient.allViewsForDomain(domainPk), _.views.get.map(viewApiToProxy).toArray)
    }
  }

  final override
  def deleteView(pk: Long): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.views.deleteEntity(pk)
            Success(pk)
        }
      } else mapEntityResponse(apiClient.deleteView(pk))
    }
  }

  final override
  def deleteViewsForDomain(pk: Long): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            sql.views.deleteViewsForDomain(pk)
            Success(pk)
        }
      } else mapEntityResponse(apiClient.deleteViewsForDomain(pk))
    }
  }

  override
  def ensureView(view: CatalogView): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            if (view.domainFk <= 0)  {
              ???
            }
            val domain = sql.domains.findEntityByPk(view.domainFk).get
            val (fetched, _) = sql.views.upsertEntity(domain.udk.orNull, view)
            Success(fetched.pk)
        }
      } else mapEntityResponse(apiClient.ensureView(view))
    }
  }

  override def ensureViewInDomain(domainUdk: String, view: CatalogView): Try[CatalogView] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            val (fetched, _) = sql.views.upsertEntity(domainUdk, view)
            Success(fetched)
        }
      } else throw VitalsException("Method not implemented in legacy thrift")
    }
  }

  override def insertView(view: CatalogView): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            val pk = sql.views.insertEntity(view)
            Success(pk)
        }
      } else mapEntityResponse(apiClient.insertView(view))
    }
  }

  override def insertViewWithPk(view: CatalogView): Try[CatalogView] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.views.insertEntityByPk(view))
        }
      } else throw VitalsException("Cannot insert with pk from client")
    }
  }

  override
  def updateView(view: CatalogView): Try[RelatePk] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            val pk = sql.views.updateEntity(view)
            Success(pk)
        }
      } else mapEntityResponse(apiClient.updateView(view))
    }
  }

  override
  def updateViewGeneration(viewPk: RelatePk): Try[RelatePk] = {
    if (modality.isClient)
      return Failure(new RuntimeException("Cannot invoke this as a client"))

    resultOrFailure {
      sql.connection localTx {
        implicit session =>
          sql.views.updateGenClockForView(sql.views.findEntityByPk(viewPk).getOrElse {
            throw BurstUnknownPrimaryKeyException(viewPk)
          })
          Success(viewPk)
      }
    }
  }

  override
  def updateViewGenerationsForDomain(domainFk: RelatePk): Try[RelatePk] = {
    if (modality.isClient)
      return Failure(new RuntimeException("Cannot invoke this as a client"))

    resultOrFailure {
      sql.connection localTx {
        implicit session =>
          sql.views.updateGenClockForViewsInDomain(domainFk)
          Success(domainFk)
      }
    }
  }

  override
  def recordViewLoad(pk: RelatePk, updatedProperties: VitalsPropertyMap): Try[RelatePk] = {
    if (modality.isClient)
      return Failure(new RuntimeException("Cannot invoke this as a client"))

    resultOrFailure {
      sql.connection localTx {
        implicit session =>
          val view = sql.views.findEntityByPk(pk) match {
            case None => throw VitalsException(s"view $pk not found").fillInStackTrace()
            case Some(entity) => entity
          }
          sql.views.recordViewLoad(pk, view.viewProperties ++ updatedProperties)
          Success(pk)
      }
    }
  }

  final override
  def allViews(limit: Option[Int]): Try[Array[CatalogView]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.views.fetchAllEntities(limit).map(updateGenClockAndEarliestLoadTimeIfStale).toArray)
        }
      } else mapViewResponse(apiClient.allViews(limit), _.views.get.map(viewApiToProxy).toArray)
    }
  }

  final override
  def searchViews(descriptor: String, limit: Option[Int]): Try[Array[CatalogView]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.views.searchEntitiesByMoniker(descriptor, limit).toArray)
        }
      } else mapViewResponse(apiClient.searchViews(descriptor, limit), _.views.get.map(viewApiToProxy).toArray)
    }
  }

  final override
  def searchViewsByLabel(descriptor: String, value: Option[String], limit: Option[Int]): Try[Array[CatalogView]] = {
    resultOrFailure {
      if (modality.isServer) {
        sql.connection localTx {
          implicit session =>
            Success(sql.views.searchEntitiesByLabel(descriptor, value, limit).toArray)
        }
      } else mapViewResponse(apiClient.searchViewsByLabel(descriptor, value, limit), _.views.get.map(viewApiToProxy).toArray)
    }
  }

  /**
   * Checks if the generation clock if the view is stale
   *
   * @return true if stale
   */
  private
  def checkGenerationClock(generationClock: Long, genClockStaleDuration: Long): Boolean = {
    val currentTime = System.currentTimeMillis()
    generationClock + genClockStaleDuration > currentTime
  }

  /**
   * Updates the generation clock and earliest load time for the view, if it is stale.
   * Returns the new view if updated, the same one if not
   *
   * We assume that we are already in a transacton
   *
   * @return
   */
  private
  def updateGenClockAndEarliestLoadTimeIfStale(view: CatalogView)(implicit session: DBSession): CatalogView = {
    val gc = view.generationClock
    val staleDurationMs: Long = burstCatalogGenerationStaleMsProperty.get

    // check staleness
    if (checkGenerationClock(gc, staleDurationMs))
      view
    else
      sql.views.updateGenClockAtomically(view, gc + staleDurationMs)
  }
}
