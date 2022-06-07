/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.catalog

import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.query.CatalogJsonQuery
import org.burstsys.catalog.model.view.CatalogJsonView
import org.burstsys.dash.application.BurstDashEndpointBase
import org.burstsys.dash.application.IntParam
import org.burstsys.dash.application.LongArrayParam
import org.burstsys.dash.application.LongParam

import jakarta.ws.rs._
import jakarta.ws.rs.core.Response.Status
import scala.util.Failure
import scala.util.Success

/**
 * Endpoints for querying the catalog
 */
trait BurstDashCatalogLookup extends Any {
  self: BurstDashEndpointBase =>

  @POST
  @Path("queryByPk")
  def queryByPk(@FormParam("pk") pk: Long): CatalogJsonQuery = {
    resultOrErrorResponse {
      catalog findQueryByPk pk match {
        case Failure(t) => throw t
        case Success(c) => c
      }
    }
  }

  @POST
  @Path("allQueries")
  def allQueries(@FormParam("limit") limit: String): Array[CatalogJsonQuery] = {
    resultOrErrorResponse {
      val limitValue = if (limit == null || limit.isEmpty) None else Some(limit.toInt)
      catalog allQueries limitValue match {
        case Failure(t) => throw t
        case Success(querys) =>
          querys.map(c => c: CatalogJsonQuery)
      }
    }
  }

  @POST
  @Path("domainByPk")
  def domainByPk(@FormParam("pk") pk: Long): CatalogJsonDomain = {
    resultOrErrorResponse {
      catalog findDomainByPk pk match {
        case Failure(t) => throw t
        case Success(c) => c
      }
    }
  }

  @POST
  @Path("viewByPk")
  def viewByPk(@FormParam("pk") pk: Long): CatalogJsonView = {
    resultOrErrorResponse {
      catalog findViewByPk pk match {
        case Failure(t) => throw t
        case Success(c) => c
      }
    }
  }

  @GET
  @Path("views/{view}")
  def view(@PathParam("view") view: LongParam): CatalogJsonView = {
    resultOrErrorResponse {
      view.value match {
        case None => respondWith(Status.BAD_REQUEST, "message" -> s"Unknown view '${view.raw}'")
        case Some(pk) =>
          catalog.findViewByPk(pk) match {
            case Failure(t) => throw t
            case Success(v) => v
          }
      }
    }
  }

  /**
   * Look up a domain by pk if param is numeric, or by udk otherwise
   * @param id the identifer of the domain
   * @return
   */
  private def domainFor(id: LongParam): CatalogDomain = id.value match {
    case None =>
      catalog.findDomainByUdk(id.raw) match {
        case Failure(_) => respondWith(Status.BAD_REQUEST, "message" -> s"Unknown domain '${id.raw}'")
        case Success(d) => d
      }
    case Some(pk) =>
      catalog.findDomainByPk(pk) match {
        case Failure(t) => throw t
        case Success(d) => d
      }
  }

  @GET
  @Path("domains")
  // For whatever reason @DefaultValue("") is not respected
  def domains(
               @DefaultValue("_") @QueryParam("limit") limit: IntParam,
               @DefaultValue("_") @QueryParam("pks") pks: LongArrayParam
             ): Array[CatalogJsonDomain] = {
    resultOrErrorResponse {
      catalog.allDomains(limit.value) match {
        case Failure(t) => throw t
        case Success(domains) =>
          pks.value
            .map(pks => domains.filter(d => pks.contains(d.pk)))
            .getOrElse(domains)
            .map(d => d: CatalogJsonDomain)
      }
    }
  }

  @GET
  @Path("domains/{domain}")
  def domain(@PathParam("domain") domainParam: LongParam): CatalogJsonDomain = {
    resultOrErrorResponse {
      domainFor(domainParam)
    }
  }

  @GET
  @Path("domains/{domain}/{view}")
  def viewInDomain(@PathParam("domain") domainParam: LongParam, @PathParam("view") view: LongParam): CatalogJsonView = {
    resultOrErrorResponse {
      val domain = domainFor(domainParam)
      catalog.allViewsForDomain(domain.pk) match {
        case Success(views) =>
          views.find(v => view.value.contains(v.pk) || v.udk.contains(view.raw)).getOrElse(
            respondWith(Status.NOT_FOUND, "message" -> s"Could not find view ${view.raw} in domain pk=${domain.pk} udk=${domain.udk.orNull}")
          )
        case Failure(exception) => throw exception
      }
    }
  }


  @GET
  @Path("domains/{domain}/views")
  def viewsByDomain(@PathParam("domain") domainParam: LongParam): Array[CatalogJsonView] = {
    resultOrErrorResponse {
      val domain = domainFor(domainParam)
      catalog.allViewsForDomain(domain.pk) match {
        case Failure(t) => throw t
        case Success(views) => views.map(v => v: CatalogJsonView)
      }
    }
  }
}
