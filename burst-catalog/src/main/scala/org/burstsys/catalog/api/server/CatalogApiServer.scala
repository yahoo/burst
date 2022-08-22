/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.api.server

import com.twitter.util.Future
import org.burstsys.api.{ApiTwitterRequestFuture, BurstApiServer}
import org.burstsys.catalog.CatalogExceptions.{CatalogInvalidException, CatalogNotFoundException}
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.api.BurstCatalogApiStatus.{BurstCatalogApiException, BurstCatalogApiInvalid, BurstCatalogApiNotFound}
import org.burstsys.catalog.api._

import scala.util.{Failure, Success, Try}

/**
  * Listens for incoming thrift requests and maps them to the correct methods in the [[CatalogService]]
  */
final case
class CatalogApiServer(service: CatalogService) extends BurstApiServer
  with CatalogApi with  CatalogDomainApiReactor with CatalogViewApiReactor
  with CatalogQueryApiReactor {

  def mapResponse[U, R](data: => Try[U], success: U => R, failure: BurstCatalogApiResult => R = null): Future[R] = {
    ensureRunning
    ApiTwitterRequestFuture {
      data match {
        case Failure(t) =>
          if (failure == null)
            throw t
          else failure(statusFromException(t))
        case Success(result) => success(result)
      }
    }
  }

  /** Returns a context with a request state matching the kind of exception caught. */
  private def statusFromException(t : Throwable): BurstCatalogApiResult = {
    t match {
      case ci: CatalogInvalidException => BurstCatalogApiResult(BurstCatalogApiInvalid, ci.toString)
      case cnf: CatalogNotFoundException => BurstCatalogApiResult(BurstCatalogApiNotFound, cnf.toString)
      case _ => BurstCatalogApiResult(BurstCatalogApiException, t.toString)
    }
  }

}
