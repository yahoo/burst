/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api.server

import org.burstsys.catalog.BurstMoniker
import org.burstsys.catalog.api._
import org.burstsys.catalog.model.query._
import com.twitter.util.Future

/**
  * Maps query-related thrift endpoints to the catalog service
  */
trait CatalogQueryApiReactor extends CatalogApi {

  self: CatalogApiServer =>

  final override
  def allQueries(limit: Option[Int]): Future[BurstCatalogApiQueryResponse] = {
    mapResponse(service.allQueries(limit),
      (queries: Array[CatalogQuery]) => QueryResponse(queries = Some(queries.toIndexedSeq)),
      (f: BurstCatalogApiResult) => QueryResponse(f)
    )
  }

  final override
  def searchQueries(descriptor: String, limit: Option[Int]): Future[BurstCatalogApiQueryResponse] = {
    mapResponse(service.searchQueries(descriptor, limit),
      (queries: Array[CatalogQuery]) => QueryResponse(queries = Some(queries.toIndexedSeq)),
      (f: BurstCatalogApiResult) => QueryResponse(f)
    )
  }

  final override
  def searchQueriesByLabel(label: String, value: Option[String], limit: Option[Int]): Future[BurstCatalogApiQueryResponse] = {
    mapResponse(service.searchQueriesByLabel(label, value, limit),
      (queries: Array[CatalogQuery]) => QueryResponse(queries = Some(queries.toIndexedSeq)),
      (f: BurstCatalogApiResult) => QueryResponse(f)
    )
  }

  final override
  def findQueryByPk(pk: Long): Future[BurstCatalogApiQueryResponse] = {
    mapResponse(service.findQueryByPk(pk),
      (entity: CatalogQuery) => QueryResponse(Some(entity)),
      (f: BurstCatalogApiResult) => QueryResponse(f)
    )
  }

  final override
  def findQueryByMoniker(moniker: BurstMoniker): Future[BurstCatalogApiQueryResponse] = {
    mapResponse(service.findQueryByMoniker(moniker),
      (entity: CatalogQuery) => QueryResponse(Some(entity)),
      (f: BurstCatalogApiResult) => QueryResponse(f)
    )
  }

  final override
  def insertQuery(query: BurstCatalogApiQuery): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.insertQuery(query),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def updateQuery(query: BurstCatalogApiQuery): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.updateQuery(query),
      (query: CatalogQuery) => EntityResponse(Some(query.pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }

  final override
  def deleteQuery(pk: Long): Future[BurstCatalogApiEntityPkResponse] = {
    mapResponse(service.deleteQuery(pk),
      (pk: Long) => EntityResponse(Some(pk)),
      (f: BurstCatalogApiResult) => BurstCatalogApiEntityPkResponse(f)
    )
  }
}

