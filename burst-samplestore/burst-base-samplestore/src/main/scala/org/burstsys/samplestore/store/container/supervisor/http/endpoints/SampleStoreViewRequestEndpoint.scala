/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor.http.endpoints

import jakarta.inject.{Inject, Named}
import jakarta.ws.rs.core.Response.Status
import jakarta.ws.rs.core.{MediaType, Response}
import jakarta.ws.rs._
import org.burstsys.samplesource.handler.SampleSourceHandlerRegistry
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, BurstSampleStoreDomain, BurstSampleStoreView, SampleStoreSourceNameProperty}
import org.burstsys.samplestore.store.container.supervisor.SampleStoreFabricSupervisorContainer
import org.burstsys.samplestore.store.container.supervisor.http.SampleStoreHttpBinder
import org.burstsys.samplestore.store.container.supervisor.http.endpoints.ViewRequestTypes.SampleStoreDataSource
import org.burstsys.samplestore.store.container.supervisor.http.services.ViewGenerationRequestLog
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


@Path("/api/view-request")
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
class SampleStoreViewRequestEndpoint {

  @Inject
  private var container: SampleStoreFabricSupervisorContainer = _

  @Inject
  private var requestLog: ViewGenerationRequestLog = _

  @Inject
  @Named("storeListenerProperties") // SampleStoreHttpBinder.storeListenerProperties
  private var storeListenerProperties: VitalsPropertyMap = _

  @POST
  def newViewGenerationRequest(json: SampleStoreDataSource): Response = {
    json.view.storeProperties.get(SampleStoreSourceNameProperty) match {
      case Some(sourceName) =>
        val supervisor = SampleSourceHandlerRegistry.getSupervisor(sourceName)
        val domain = json.domain
        val view = json.view
        val dataSource = BurstSampleStoreDataSource(
          BurstSampleStoreDomain(domain.domainKey, domain.domainProperties),
          BurstSampleStoreView(view.viewKey, view.schemaName, view.viewMotif, view.storeProperties, view.viewProperties)
        )
        try {
          val generation = Await.result(supervisor.getViewGenerator("x-debug", dataSource, container.getTopology, storeListenerProperties), 20 seconds)
          Response.ok(generation).build()
        } catch safely {
          case t =>
            Response.serverError()
              .entity(Map(
                "reason" -> t.getLocalizedMessage
              ))
              .build()
        }

      case None =>
        Response.status(Status.BAD_REQUEST)
          .entity(Map(
            "reason" -> s"Unable to determine source: view.storeProperties[$SampleStoreSourceNameProperty] not found"
          ))
          .build()
    }

  }

  @GET
  @Path("/{guid}")
  def showViewGenerationRequest(@PathParam("guid") guid: String): Response = {
    requestLog.responseFor(guid) match {
      case Some(response) =>
        Response.ok(response).build()
      case None =>
        Response.status(Status.NOT_FOUND).build()
    }

  }
}

object ViewRequestTypes {
  case class SampleStoreDataSource(
                                    domain: SampleStoreDomain,
                                    view: SampleStoreView,
                                  )

  case class SampleStoreDomain(
                                domainKey: Int,
                                domainProperties: Map[String, String],
                              )

  case class SampleStoreView(
                              viewKey: Int,
                              schemaName: String,
                              viewMotif: String,
                              storeProperties: Map[String, String],
                              viewProperties: Map[String, String],
                            )
}
