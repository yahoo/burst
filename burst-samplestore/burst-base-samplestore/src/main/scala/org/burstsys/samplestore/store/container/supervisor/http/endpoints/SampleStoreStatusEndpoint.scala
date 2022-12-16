/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor.http.endpoints

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.burstsys.fabric.topology.supervisor.FabricSupervisorTopology
import org.burstsys.samplesource.handler.SampleSourceHandlerRegistry
import org.burstsys.samplesource.service.MetadataParameters
import org.burstsys.samplestore.store.container.supervisor.http.endpoints.StatusResponseObjects.StoreInfo
import org.burstsys.samplestore.store.container.supervisor.http.services.ViewGenerationRequestLog

@Path("/")
@Produces(Array(MediaType.APPLICATION_JSON))
class SampleStoreStatusEndpoint {

  @Inject
  private var topology: FabricSupervisorTopology = _

  @Inject
  private var requestLog: ViewGenerationRequestLog = _

  @GET
  def status(): Response = {
    Response.ok(
      Map(
        "workers" -> topology.allWorkers.map(_.forExport),
        "requests" -> requestLog.requests,
        "stores" -> SampleSourceHandlerRegistry.getSources.map(StoreInfo),
      )
    ).build
  }
}

object StatusResponseObjects {
  case class StoreInfo(name: String) {
    val vars: MetadataParameters = SampleSourceHandlerRegistry.getSupervisor(name).getBroadcastVars
  }
}
