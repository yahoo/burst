/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api.server

import org.burstsys.catalog.BurstMoniker
import org.burstsys.catalog.api._
import org.burstsys.catalog.model.view._
import com.twitter.util.Future

/**
  * Maps view-related thrift endpoints to the catalog service
  */
trait CatalogViewApiReactor extends CatalogApi {

  self: CatalogApiServer =>

  final override
  def allViews(limit: Option[Int]): Future[BurstCatalogApiViewResponse] = {
    mapResponse(service.allViews(limit),
      (views: Array[CatalogView]) => ViewResponse(views = Some(views.toIndexedSeq)),
      (f: BurstCatalogApiResult) => ViewResponse(f)
    )
  }

  final override
  def searchViews(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiViewResponse] = {
    mapResponse(service.searchViews(descriptor, limit),
      (views: Array[CatalogView]) => ViewResponse(views = Some(views.toIndexedSeq)),
      (f: BurstCatalogApiResult) => ViewResponse(f)
    )
  }

  final override
  def searchViewsByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiViewResponse] = {
    mapResponse(service.searchViewsByLabel(label, value, limit),
      (views: Array[CatalogView]) => ViewResponse(views = Some(views.toIndexedSeq)),
      (f: BurstCatalogApiResult) => ViewResponse(f)
    )
  }

  final override
  def findViewByPk(pk: Long): Future[BurstCatalogApiViewResponse] = {
    mapResponse(service.findViewByPk(pk),
      (entity: CatalogView) => ViewResponse(Some(entity)),
      (f: BurstCatalogApiResult) => ViewResponse(f)
    )
  }

  final override
  def findViewByUdk(udk: String): Future[BurstCatalogApiViewResponse] = {
    mapResponse(service.findViewByUdk(udk),
      (entity: CatalogView) => ViewResponse(Some(entity)),
      (f: BurstCatalogApiResult) => ViewResponse(f)
    )
  }

  final override
  def findViewByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiViewResponse] = {
    mapResponse(service.findViewByMoniker(moniker),
      (entity: CatalogView) => ViewResponse(Some(entity)),
      (f: BurstCatalogApiResult) => ViewResponse(f)
    )
  }

  final override
  def ensureView(view: BurstCatalogApiView): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.ensureView(view),
      (entity: Long) => EntityResponse(Some(entity)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  override def insertView(view: BurstCatalogApiView): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.insertView(view),
      (entity: Long) => EntityResponse(Some(entity)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  override def updateView(view: BurstCatalogApiView): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.updateView(view),
      (entity: Long) => EntityResponse(Some(entity)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteView(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteView(pk),
      (key: Long) => EntityResponse(Some(key)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteViewsForDomain(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteViewsForDomain(pk),
      (key: Long) => EntityResponse(Some(key))
    )
  }

  final override
  def allViewsForDomain(domainPk: Long, limit: Option[Int]): Future[BurstCatalogApiViewResponse] = {
    mapResponse(service.allViewsForDomain(domainPk, limit),
      (views: Array[CatalogView]) => ViewResponse(views = Some(views.toIndexedSeq)),
      (f: BurstCatalogApiResult) => ViewResponse(f)
    )
  }
}

