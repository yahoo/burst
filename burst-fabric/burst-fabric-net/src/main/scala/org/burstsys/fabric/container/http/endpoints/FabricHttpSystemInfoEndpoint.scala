/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http.endpoints

import jakarta.inject.Inject
import jakarta.ws.rs.core.{MediaType, Response}
import jakarta.ws.rs.{DefaultValue, GET, Path, Produces, QueryParam}
import org.burstsys.fabric.container.http.endpoints.params.IntParam
import org.burstsys.vitals.sysinfo.SystemInfo

@Path("/system")
@Produces(Array(MediaType.APPLICATION_JSON))
class FabricHttpSystemInfoEndpoint {

  @Inject
  private var systemInfo: SystemInfo = _

  @GET
  def systemStatus(
                  @DefaultValue("0") @QueryParam("level") level: IntParam
                  ): Response = {
    Response.ok(
      SystemInfoStatus(
        level.value.getOrElse(0),
        systemInfo.systemStatus(),
        systemInfo.components.map(f => f.name -> f.status(level.value.getOrElse(0))).toMap
      )
    ).build()
  }
  case class SystemInfoStatus(level: Int, systemStatus: Object, components: Map[String, Object])

}
