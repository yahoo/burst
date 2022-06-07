/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog

/**
  * Exceptions indicating error conditions calling for different handling in upper layers.
  */
object CatalogExceptions {

  /** A catalog request got a response, but the response indicates the request was invalid. */
  case class CatalogInvalidException(explanation: String) extends RuntimeException(explanation)

  /** A catalog request got a response, but the response indicates the request returned no result. */
  case class CatalogNotFoundException(explanation: String) extends RuntimeException(explanation)

}
