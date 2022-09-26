/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.catalog

import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType
import org.burstsys.catalog.model.domain.CatalogJsonDomain
import org.burstsys.catalog.model.query.CatalogJsonQuery
import org.burstsys.catalog.model.view.CatalogJsonView
import org.burstsys.dash.application.BurstDashEndpointBase

import scala.util.{Failure, Success}

trait BurstDashCatalogUpdate extends Any {
  self: BurstDashEndpointBase =>

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Queries
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  @POST
  @Path("updateDomain")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def updateDomain(domain: CatalogJsonDomain): CatalogJsonDomain = {
    resultOrErrorResponse {
      catalog.ensureDomain(domain) match {
        case Failure(t) => throw t
        case Success(pk) => catalog.findDomainByPk(pk) match {
          case Failure(t) => throw t
          case Success(d) => d
        }
      }
    }
  }

  @POST
  @Path("updateView")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def updateView(view: CatalogJsonView): CatalogJsonView = {
    resultOrErrorResponse {
      catalog.updateView(view) match {
        case Failure(t) => throw t
        case Success(pk) => catalog.findViewByPk(pk) match {
          case Failure(t) => throw t
          case Success(v) => v
        }
      }
    }
  }

  @POST
  @Path("updateQuery")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def updateQuery(query: CatalogJsonQuery): CatalogJsonQuery = {
    resultOrErrorResponse {
      catalog updateQuery query match {
        case Failure(t) => throw t
        case Success(c) => c
      }
    }
  }

}
