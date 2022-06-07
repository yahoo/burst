/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.application

import org.burstsys.vitals.logging._
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.glassfish.jersey.spi.ExtendedExceptionMapper

@Provider
class BurstDashExceptionMapper extends ExtendedExceptionMapper[Exception] {

  def isMappable(throwable: Exception): Boolean = {
    val isWebAppException = throwable.isInstanceOf[WebApplicationException]
    val cause = if (isWebAppException && throwable.getCause != null) throwable.getCause else throwable
    log error burstStdMsg(cause)
    // ignore these guys and let jersey handle them
    !isWebAppException
  }

  def toResponse(throwable: Exception): Response = {
    log error burstStdMsg(throwable)
    Response.status(Response.Status.INTERNAL_SERVER_ERROR).build()
  }
}
