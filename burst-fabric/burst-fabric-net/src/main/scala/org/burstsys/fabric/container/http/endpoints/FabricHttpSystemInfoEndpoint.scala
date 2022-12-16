/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http.endpoints

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.burstsys.vitals
import org.burstsys.vitals.reporter.instrument.prettyTimeFromMillis

@Path("/system")
@Produces(Array(MediaType.APPLICATION_JSON))
class FabricHttpSystemInfoEndpoint {

  case class SystemStatus(
                           branch: String = vitals.git.branch,
                           commit: String = vitals.git.commitId,
                           build: String = vitals.git.buildVersion,
                           uptime: String = prettyTimeFromMillis(vitals.host.uptime),
                         )

  @GET
  def systemStatus(): Response = {
    Response.ok(SystemStatus())
      .build()
  }

}
