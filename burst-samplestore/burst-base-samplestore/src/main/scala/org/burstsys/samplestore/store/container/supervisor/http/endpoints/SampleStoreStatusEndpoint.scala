/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor.http.endpoints

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
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
        "workerCount" -> topology.healthyWorkers.length,
        "workers" -> topology.healthyWorkers.map(_.nodeMoniker),
        "requestCount" -> requestLog.requests.length,
        "stores" -> SampleSourceHandlerRegistry.getSources.map(StoreInfo),
      )
    ).build
  }

  @GET
  @Path("/workers")
  def workers(): Array[FabricWorkerNode] = {
    topology.healthyWorkers.map(_.forExport)
  }

  @GET
  @Path("/requests")
  def requests: Array[ViewGenerationRequestLog.ViewGenerationRequest] = {
    requestLog.requests
  }
}

object StatusResponseObjects {
  case class StoreInfo(name: String) {
    val vars: MetadataParameters = SampleSourceHandlerRegistry.getSupervisor(name).getBroadcastVars
  }
}
