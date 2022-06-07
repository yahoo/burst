/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints

import org.burstsys.dash.application.BurstDashEndpointBase
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.{GET, Path, Produces}

@Produces(Array(MediaType.APPLICATION_JSON))
@Path("/")
final class ServiceStatus extends BurstDashEndpointBase {
  @GET
  @Path("status.html")
  def status: BurstGenericJsonResult = BurstGenericJsonResult()

  @GET
  @Path("akamai")
  def akamai: BurstGenericJsonResult = BurstGenericJsonResult()
}
