/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http.endpoints

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.burstsys.vitals
import org.burstsys.vitals.reporter.instrument.prettyTimeFromMillis
import org.burstsys.vitals.sysinfo.{SystemInfo, SystemInfoComponent, SystemInfoService}

import scala.annotation.unused

@Path("/system")
@Produces(Array(MediaType.APPLICATION_JSON))
class FabricHttpSystemInfoEndpoint {

  @Inject
  private var systemInfo: SystemInfo = _

  @GET
  def systemStatus(): Response = {
    Response.ok(
      SystemInfoStatus(
        systemInfo.systemStatus(),
        systemInfo.components.map(f => f.name -> f.status).toMap
      )
    ).build()
  }
  case class SystemInfoStatus(systemStatus: Object, components: Map[String, Object])

}
