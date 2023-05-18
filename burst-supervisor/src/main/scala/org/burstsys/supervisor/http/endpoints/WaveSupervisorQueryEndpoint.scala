/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.endpoints

import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.wave.execution.model.result.status.FabricInvalidResultStatus
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.time.timeZoneNameList
import org.burstsys.vitals.uid._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@Path(QueryApiPath)
final class WaveSupervisorQueryEndpoint extends WaveSupervisorEndpoint {

  @POST
  @Path("executeGroup")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def executeGroup(
                    @FormParam("source") source: String,
                    @FormParam("domain") domain: String,
                    @FormParam("view") view: String,
                    @FormParam("timezone") timezone: String,
                    @FormParam("args") args: String
                  ): FabricExecuteResult = {
    resultOrErrorResponse {
      if (timezone == null || timezone.isEmpty || !timeZoneNameList.contains(timezone))
        return FabricExecuteResult(
          FabricInvalidResultStatus,
          s"timezone=$timezone invalid or malformed"
        )

      val over = try {
        FabricOver(domain.toLong, view.toLong, timezone)
      } catch safely {
        case _: NumberFormatException =>
          return FabricExecuteResult(
            resultStatus = FabricInvalidResultStatus,
            resultMessage = s"domain=$domain, view=$view invalid or malformed"
          )
      }

      val uid = s"WEB_${newBurstUid}"
      val call = if (args == null || args.trim.isEmpty) None else Some(FabricCall(args))
      val result = Await.result(agent.execute(source, over, uid, call), 90 seconds)
      result.toJson // here is where our JSON architecture pays off
    }
  }

}
