/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http

import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.burstsys.fabric.container.http.FabricHttpExceptionMapperResponses.SimpleExceptionResponse
import org.burstsys.vitals.logging._
import org.glassfish.jersey.spi.ExtendedExceptionMapper

@Provider
class FabricHttpExceptionMapper extends ExtendedExceptionMapper[Exception] {

  /**
   * Determine if we want to handle the serialization of a particular exception
   *
   * @param throwable
   * @return
   */
  def isMappable(throwable: Exception): Boolean = {
    val isWebAppException = throwable.isInstanceOf[WebApplicationException]
    val cause = if (isWebAppException && throwable.getCause != null) throwable.getCause else throwable
    log error burstStdMsg(cause)
    // ignore these guys and let jersey handle them
    !isWebAppException
  }

  def toResponse(throwable: Exception): Response = {
    log error burstStdMsg(throwable)
    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SimpleExceptionResponse(throwable.getMessage)).build()
  }
}

// Provide response objects that conform to the dash's expectation of { success: false, error: "..." }
object FabricHttpExceptionMapperResponses {

  case class SimpleExceptionResponse(
                                      error: String,
                                      success: Boolean = false
                                    )

  case class DetailedExceptionResponse(
                                        error: String,
                                        stack: String,
                                        success: Boolean = false
                                      )
}
