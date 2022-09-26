/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.catalog

import org.burstsys.dash.application.{BurstDashEndpointBase, IntParam}
import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response.Status

import scala.collection.mutable
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait BurstDashCatalogSearch extends Any {
  self: BurstDashEndpointBase =>

  @POST
  @Path("search")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def search(
              @FormParam("domain") domain: String,
              @FormParam("view") view: String,
              @DefaultValue("_") @FormParam("limit") limit: IntParam
            ): Array[CatalogTreeNodeJson] = {
    resultOrErrorResponse {
      val domainIdentifier = if (domain == null || domain.isEmpty) None else Some(domain)
      val viewIdentifier = if (view == null || view.isEmpty) None else Some(view)

      catalog.searchCatalog(domainIdentifier, viewIdentifier, limit.value) match {
        case Failure(e) => respondWith(Status.INTERNAL_SERVER_ERROR, "error" -> e.getLocalizedMessage)
        case Success(rows) =>
          val tree = mutable.Map[Long, CatalogTreeNodeJson]()
          for (r <- rows) {
            val pk = r("domain_pk").toLong
            val domain = tree.getOrElseUpdate(pk, CatalogTreeNodeJson(pk, r("domain_moniker"), r("domain_udk")))
            if (r("view_pk") != null) {
              domain.children = domain.children ++ Array(CatalogTreeNodeJson(r("view_pk").toLong, r("view_moniker"), r("view_udk")))
            }
          }
          tree.values.toArray
      }
    }
  }
}
