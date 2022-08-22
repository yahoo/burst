/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api

import org.burstsys.catalog.api.BurstCatalogApiStatus.BurstCatalogApiSuccess
import org.burstsys.relate.RelatePk
import org.burstsys.vitals.logging._

package object server extends VitalsLogger {

  object EntityResponse {
    def apply(pk: Option[RelatePk] = None): BurstCatalogApiEntityPkResponse = BurstCatalogApiEntityPkResponse(Result(), pk)
  }

  object DomainResponse {
    def apply(result: BurstCatalogApiResult): BurstCatalogApiDomainResponse = BurstCatalogApiDomainResponse(result)

    def apply(domain: Option[BurstCatalogApiDomain] = None,
              domains: Option[Seq[BurstCatalogApiDomain]] = None): BurstCatalogApiDomainResponse
    = BurstCatalogApiDomainResponse(Result(), domain, domains)
  }

  object DomainViewResponse {
    def apply(domain: Option[BurstCatalogApiDomain] = None,
              views: Option[Seq[BurstCatalogApiView]] = None): BurstCatalogApiDomainAndViewsResponse
    = BurstCatalogApiDomainAndViewsResponse(Result(), domain, views)
  }

  object QueryResponse {
    def apply(result: BurstCatalogApiResult): BurstCatalogApiQueryResponse = BurstCatalogApiQueryResponse(result)

    def apply(query: Option[BurstCatalogApiQuery] = None,
              queries: Option[Seq[BurstCatalogApiQuery]] = None): BurstCatalogApiQueryResponse
    = BurstCatalogApiQueryResponse(Result(), query, queries)
  }

  object ViewResponse {
    def apply(result: BurstCatalogApiResult): BurstCatalogApiViewResponse = BurstCatalogApiViewResponse(result)

    def apply(view: Option[BurstCatalogApiView] = None,
              views: Option[Seq[BurstCatalogApiView]] = None): BurstCatalogApiViewResponse
    = BurstCatalogApiViewResponse(Result(), view, views)
  }

  object Result {
    def apply(status: BurstCatalogApiStatus = BurstCatalogApiSuccess): BurstCatalogApiResult = BurstCatalogApiResult(status)
  }

}
