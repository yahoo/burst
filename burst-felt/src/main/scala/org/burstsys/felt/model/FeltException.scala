/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

/**
 * Exception handling/reporting for Felt syntax trees. Includes
 * hopefully accurate/helpful lexical location information.
 */
trait FeltException extends RuntimeException {

  /**
   * the location in the FELT source.
   *
   * @return
   */
  def location: FeltLocation

  /**
   * the message associated with this exception
   *
   * @return
   */
  def message: String

}

object FeltException {

  def apply(location: FeltLocation, message: String, cause: Throwable = null): FeltException = {
    FeltExceptionContext(location, message, cause)
  }

  def apply(node: FeltNode, message: String): FeltException = {
    FeltExceptionContext(node.location, message, null)
  }

}

private final case
class FeltExceptionContext(location: FeltLocation, message: String, cause: Throwable)
  extends RuntimeException(message, cause) with FeltException {

  override
  def getMessage: String = s"FELT error\n${location.contextualizedErrorMsg(message)}"

}
