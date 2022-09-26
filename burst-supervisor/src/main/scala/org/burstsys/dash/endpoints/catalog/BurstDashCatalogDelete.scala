/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.catalog

import jakarta.ws.rs._
import jakarta.ws.rs.core.{MediaType, Response}

import org.burstsys.catalog._
import org.burstsys.vitals.errors._

import scala.util.{Failure, Success}
import org.burstsys.vitals.logging._

trait BurstDashCatalogDelete extends AnyRef {

  def catalog: CatalogService

  case class BurstCatalogDeleteSuccess(pk: Long, nuccess: Boolean = true, message: String = "valid!")

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Queries
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  @POST
  @Path("deleteQuery")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def deleteQuery(@FormParam("pk") pk: Long): BurstCatalogDeleteSuccess = {
    try {
      catalog deleteQuery pk match {
        case Failure(t) =>
          throw new WebApplicationException(t, Response.Status.BAD_REQUEST)
        case Success(c) =>
          BurstCatalogDeleteSuccess(pk)
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR)
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Views
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  @POST
  @Path("deleteView")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def deleteView(@FormParam("pk") pk: Long): BurstCatalogDeleteSuccess = {
    try {
      catalog deleteView pk match {
        case Failure(t) =>
          throw new WebApplicationException(t, Response.Status.BAD_REQUEST)
        case Success(c) =>
          BurstCatalogDeleteSuccess(pk)
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR)
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Domains
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  @POST
  @Path("deleteDomain")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def deleteDomain(@FormParam("pk") pk: Long): BurstCatalogDeleteSuccess = {
    try {
      catalog deleteDomain pk match {
        case Failure(t) =>
          throw new WebApplicationException(t, Response.Status.BAD_REQUEST)
        case Success(c) =>
          BurstCatalogDeleteSuccess(pk)
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR)
    }
  }

}
