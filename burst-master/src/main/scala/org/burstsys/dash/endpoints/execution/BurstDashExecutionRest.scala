/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.execution

import org.burstsys.dash.application.{BoolParam, BurstDashEndpointBase, LongParam}
import org.burstsys.dash.endpoints._
import org.burstsys.dash.service.execution.requests
import org.burstsys.dash.websocket.FabricRequestJson
import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response.Status

import scala.collection.mutable

@Path(ExecutionApiPath)
@Produces(Array(MediaType.APPLICATION_JSON))
final class BurstDashExecutionRest extends BurstDashEndpointBase {

  @GET
  @Path("queries")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def allQueries(
                  @DefaultValue("_") @QueryParam("onlyFailed") onlyFailed: BoolParam,
                  @DefaultValue("_") @QueryParam("earliestBeginMillis") startedAfterMillis: LongParam,
                  @DefaultValue("_") @QueryParam("latestBeginMillis") startedBeforeMillis: LongParam,
                  @DefaultValue("_") @QueryParam("earliestEndMillis") endedAfterMillis: LongParam,
                  @DefaultValue("_") @QueryParam("latestEndMillis") endedBeforeMillis: LongParam
                ): Array[FabricRequestJson] = {
    resultOrErrorResponse {
      val now = System.currentTimeMillis
      val excludeSuccess = onlyFailed.value.getOrElse(false)

      val startWindowBegin: Long = startedAfterMillis.value.getOrElse(0)
      val startWindowEnd: Long = startedBeforeMillis.value.getOrElse(now)

      val endWindowBegin: Long = endedAfterMillis.value.getOrElse(0)
      val endWindowEnd: Long = endedBeforeMillis.value.getOrElse(now)

      var all = mutable.ArrayBuffer[FabricRequestJson]()
      requests.foreach { req =>
        if (
          (req.startTime >= startWindowBegin && req.endTime <= startWindowEnd) &&
            (req.startTime >= endWindowBegin && req.endTime <= endWindowEnd) &&
            (!excludeSuccess || !req.state.isSuccess)) {
          all += FabricRequestJson(req, shallow = false)
        }
      }
      all.sortWith((l, r) => l.beginMillis < r.beginMillis).toArray
    }
  }

  @GET
  @Path("queries/{requestId}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def singleQuery(@DefaultValue("") @PathParam("requestId") requestId: String): FabricRequestJson = {
    resultOrErrorResponse {
      requests.get(requestId) match {
        case Some(req) => FabricRequestJson(req, shallow = false)
        case None => respondWith(Status.NOT_FOUND, "message" -> s"Request id '$requestId' not found")
      }
    }
  }
}
