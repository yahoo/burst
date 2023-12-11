/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor.http.endpoints

import jakarta.inject.Inject
import jakarta.ws.rs._
import jakarta.ws.rs.core.Response.Status
import jakarta.ws.rs.core.{MediaType, Response}
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, BurstSampleStoreDomain, BurstSampleStoreView, SampleStoreApiServerDelegate}
import org.burstsys.samplestore.store.container.supervisor.http.endpoints.ViewRequestTypes.SampleStoreDataSource
import org.burstsys.samplestore.store.container.supervisor.http.services.ViewGenerationRequestLog
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


@Path("/api/view-request")
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
class SampleStoreViewRequestEndpoint {

  @Inject
  private var requestLog: ViewGenerationRequestLog = _

  @Inject
  private var sampleStoreDelegate: SampleStoreApiServerDelegate = _

  @POST
  def newViewGenerationRequest(json: SampleStoreDataSource): Response = {
    val domain = json.domain
    val view = json.view
    val dataSource = BurstSampleStoreDataSource(
      BurstSampleStoreDomain(domain.domainKey, domain.domainProperties),
      BurstSampleStoreView(view.viewKey, view.schemaName, view.viewMotif, view.storeProperties, view.viewProperties)
    )
    val viewResponse = sampleStoreDelegate.getViewGenerator(vitals.uid.newBurstUid, dataSource)
      .map(generation => Response.ok(generation).build())
      .recover({ case t =>
        Response.serverError()
          .entity(Map(
            "reason" -> t.getLocalizedMessage
          )).build()
      })
    Await.result(viewResponse, 20 seconds)
  }

  @GET
  @Path("/{guid}")
  def showViewGenerationRequest(@PathParam("guid") guid: String): Response = {
    requestLog.responseFor(guid) match {
      case Some(response) =>
        Response.ok(response).build()
      case None =>
        Response.status(Status.NOT_FOUND)
          .entity(Map(
            "reason" -> s"Unrecognized request $guid"
          )).build()
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
