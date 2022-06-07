/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate

import java.sql.SQLException

/**
  * Exceptions indicating error conditions calling for different handling in upper layers.
  */
object RelateExceptions {

  trait BurstSqlException extends RuntimeException

  /** A catalog request got a response, but the response indicates the request was invalid. */
  case class BurstDuplicateKeyException(explanation: String = "Duplicate key", cause: SQLException) extends RuntimeException(explanation, cause) with BurstSqlException

  case class BurstUnknownPrimaryKeyException(pk: RelatePk) extends RuntimeException(s"Referenced primary key not found [$pk]") with BurstSqlException

  case class BurstUnknownMonikerException(moniker: String) extends RuntimeException(s"Moniker not found [$moniker]") with BurstSqlException

}
