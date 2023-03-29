package org.burstsys.supervisor.http.endpoints

import jakarta.ws.rs.{Consumes, POST, Path, Produces}
import jakarta.ws.rs.core.{MediaType, Response}
import org.burstsys.supervisor.http.endpoints.BurnInMessages.BurnInResponse
import org.burstsys.supervisor.http.service.provider.BurnInConfig
import org.burstsys.vitals.errors.safely

@Path(BurnInApiPath)
@Consumes(Array(MediaType.APPLICATION_JSON))
@Produces(Array(MediaType.APPLICATION_JSON))
final class WaveSupervisorBurnInEndpoint extends WaveSupervisorEndpoint {

  @POST
  @Path("start")
  def startBurnIn(config: BurnInConfig): Response = {
    resultOrErrorResponse {
      val running = burnIn.startBurnIn(config)
      Response.ok(BurnInResponse(running)).build()
    }
  }

  @POST
  @Path("stop")
  def stopBurnIn(): Response = {
    resultOrErrorResponse {
      val didStop = burnIn.stopBurnIn()
      Response.ok(BurnInResponse(running = !didStop)).build()
    }
  }

}

object BurnInMessages {
  case class BurnInResponse(running: Boolean)
}
