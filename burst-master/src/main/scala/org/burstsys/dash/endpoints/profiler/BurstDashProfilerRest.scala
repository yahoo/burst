/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.profiler

import org.burstsys.dash.application.BurstDashEndpointBase
import org.burstsys.dash.endpoints._
import org.burstsys.dash.provider.profiler.{ProfilerRunResponse, ProfilerStopResponse}
import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType

/**
  * profiler ''javax.ws.rs'' REST API
  */
@Path(ProfilerApiPath)
@Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
@Produces(Array(MediaType.APPLICATION_JSON))
final class BurstDashProfilerRest extends BurstDashEndpointBase {

  @POST
  @Path("run")
  def runProfiler(
                   @FormParam("source") source: String,
                   @FormParam("domain") domain: Long,
                   @FormParam("view") view: Long,
                   @FormParam("timezone") timezone: String,
                   @FormParam("concurrency") concurrency: Int,
                   @FormParam("executions") executions: Int,
                   @FormParam("reload") reload: Int
                 ): ProfilerRunResponse = {
    resultOrErrorResponse {
      profiler.startProfiler(source, domain, view, timezone, concurrency, executions, reload)
    }
  }

  @POST
  @Path("stop")
  def stopProfiler(): ProfilerStopResponse = {
    resultOrErrorResponse {
      profiler.stopProfiler
    }
  }

}
