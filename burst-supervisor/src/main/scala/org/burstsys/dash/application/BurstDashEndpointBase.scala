/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.application

import jakarta.inject.Inject
import jakarta.servlet.ServletContext
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core._
import org.apache.logging.log4j.Logger
import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.dash.endpoints
import org.burstsys.dash.provider.profiler.BurstDashProfilerService
import org.burstsys.dash.provider.torcher.BurstDashTorcherService
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.fabric.wave.container.supervisorContainer
import org.burstsys.supervisor.server.container.BurstWaveSupervisorContainer
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import sourcecode.Enclosing
import sourcecode.Line

import scala.collection.mutable

/**
  * The main base class for JSON Rest endpoints
  */
abstract class BurstDashEndpointBase {

  @Context
  var servlet: ServletContext = _

  @Context
  var uriInfo: UriInfo = _

  @Context
  var request: Request = _

  @Context
  var security: SecurityContext = _

  final var container: BurstWaveSupervisorContainer = supervisorContainer.asInstanceOf[BurstWaveSupervisorContainer]

  @Inject
  var catalog: CatalogService = _

  @Inject
  var agent: AgentService = _

  @Inject
  var supervisor: FabricWaveSupervisorContainer = _

  @Inject
  var torcher: BurstDashTorcherService = _

  @Inject
  var profiler: BurstDashProfilerService = _

  final def log: Logger = endpoints.log

  final def resultOrErrorResponse[T](work: => T)(implicit site: Enclosing, line: Line): T = {
    try {
      work
    } catch safely {
      case wae: WebApplicationException =>
        log error burstStdMsg(wae)
        if (wae.getCause !=  null) respondWith(wae.getResponse.getStatusInfo, "error" -> wae.getCause.getLocalizedMessage)
        else throw wae
      case t: Throwable =>
        val location = s"[${site.value}:${line.value}]"
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
