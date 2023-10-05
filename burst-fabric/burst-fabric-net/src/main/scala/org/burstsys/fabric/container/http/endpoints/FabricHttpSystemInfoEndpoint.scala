/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http.endpoints

import jakarta.inject.Inject
import jakarta.ws.rs.core.{MediaType, Response}
import jakarta.ws.rs.{Consumes, DefaultValue, GET, POST, Path, Produces, QueryParam}
import org.burstsys.fabric.container.http.endpoints.params.{GenericParam, IntParam}
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.sysinfo.SystemInfo

/**
 * System info endpoint
 *
 * generic way to get system info for debugging an analysis
 *
 * curl -k https://localhost:8085/system  -u burst -H 'Content-Type: application/json' \
 *    --data-raw '{"samplestore-supervisor-container" : { "testattribute": "foobar" }}'
 */

@Path("/system")
@Consumes(Array(MediaType.APPLICATION_JSON))
@Produces(Array(MediaType.APPLICATION_JSON))
class FabricHttpSystemInfoEndpoint {

  @Inject
  private var systemInfo: SystemInfo = _

  @POST
  def systemStatus(
                  config: Map[String, VitalsPropertyMap],
                  @DefaultValue("0") @QueryParam("level") level: IntParam
                  ): Response = {

    try {
      val res = SystemInfoStatus(
        level.value.getOrElse(0),
        systemInfo.systemStatus(),
        systemInfo.components.map(f => f.name -> f.status(level.value.getOrElse(0), config.getOrElse(f.name, {Map.empty}))).toMap
      )
      Response.ok(res).build()
    } catch {
      case e: Exception =>
        Response.serverError().entity(e.getMessage).build()
    }
  }
  case class SystemInfoStatus(level: Int, systemStatus: Object, components: Map[String, Object])

}
