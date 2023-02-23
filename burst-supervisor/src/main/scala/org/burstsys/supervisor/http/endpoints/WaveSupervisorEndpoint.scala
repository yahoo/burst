/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.endpoints

import jakarta.inject.Inject
import jakarta.servlet.ServletContext
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core._
import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.supervisor.http.service.provider.BurstWaveSupervisorProfilerService
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import sourcecode.{Enclosing, Line}

import scala.collection.mutable

/**
  * The main base class for JSON Rest endpoints
  */
abstract class WaveSupervisorEndpoint {

  @Context
  var servlet: ServletContext = _

  @Context
  var uriInfo: UriInfo = _

  @Context
  var request: Request = _

  @Context
  var security: SecurityContext = _

  @Inject
  var catalog: CatalogService = _

  @Inject
  var agent: AgentService = _

  @Inject
  var supervisor: FabricWaveSupervisorContainer = _

  final def resultOrErrorResponse[T](work: => T)(implicit site: Enclosing, line: Line): T = {
    val location = s"[${site.value}:${line.value}]"
    try {
      work
    } catch safely {
      case wae: WebApplicationException =>
        log error burstStdMsg(wae)
        if (wae.getCause !=  null) respondWith(wae.getResponse.getStatusInfo, "error" -> wae.getCause.getLocalizedMessage, "location" -> location)
        else throw wae
      case t: Throwable =>
        log error(burstStdMsg(s"Failed from $location"), t)
        respondWith(Response.Status.INTERNAL_SERVER_ERROR, "error" -> t.getLocalizedMessage, "location" -> location)
    }
  }

  final def errorResponse(status: Response.StatusType, body: (String, Any)*): Throwable = {
    val entity = mutable.Map[String, Any](body:_*)
    entity += "success" -> false
    new WebApplicationException(
      Response.status(status).entity(entity).build()
    )
  }

  final
  def respondWith[T](status: Response.StatusType, body: (String, Any)*): T = {
    val entity = mutable.Map[String, Any](body:_*)
    entity += "success" -> false
    throw errorResponse(status, body:_*)
  }
}
