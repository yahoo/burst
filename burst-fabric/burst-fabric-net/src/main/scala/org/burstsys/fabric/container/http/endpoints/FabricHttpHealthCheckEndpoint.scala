/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http.endpoints

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.burstsys.vitals.healthcheck.VitalsSystemHealthService

@Path("/status")
@Produces(Array(MediaType.APPLICATION_JSON))
class FabricHttpHealthCheckEndpoint {

  @Inject
  private var heathService: VitalsSystemHealthService = _

  @GET
  def status(): Response = {
    val systemHealth = heathService.systemStatus

    val components = systemHealth.components.map(e => (e._1, HttpSubsystemStatus(e._2.health.statusCode, e._2.message)))
    Response.status(systemHealth.status.statusCode)
      .entity(HttpSystemStatus(heathService.system, systemHealth.status.toString, systemHealth.message, components))
      .build()
  }
}

case class HttpSubsystemStatus(status: Int, message: String)

case class HttpSystemStatus(system: String, health: String, message: String, subsystems: Map[String, HttpSubsystemStatus])
