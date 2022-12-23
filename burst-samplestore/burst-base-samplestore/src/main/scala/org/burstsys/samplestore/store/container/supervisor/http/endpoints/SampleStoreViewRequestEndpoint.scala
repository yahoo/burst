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

/*
{
  "domain": {
    "domainKey": 1614642,
    "domainProperties": {"synthetic.samplestore.press.dataset": "simple-unity"}
  },
  "view": {
    "viewKey": 1935817,
    "schemaName": "unity",
    "viewMotif": "VIEW template {\n  INCLUDE user WHERE user.application.firstUse.sessionTime >= (NOW - DAYS(30))\n  INCLUDE user.sessions.events where false\n}\n",
    "storeProperties": {
      "synthetic.samplestore.press.item.count": "5",
      "burst.store.name": "sample",
      "burst.samplestore.source.version": "0.0",
      "burst.samplestore.source.name": "synthetic-samplesource"
    },
    "viewProperties": {
      "burst.view.earliest.load.at": "1671660712459",
      "burst.view.last.slice.count": "9",
      "burst.view.last.rejected.item.count": "0",
      "burst.view.last.load.invalid": "false",
      "burst.view.last.item.variation": "0.0",
      "burst.view.last.dataset.size": "452655",
      "burst.view.next.load.stale": "86400000",
      "burst.view.last.load.took": "188",
      "burst.view.last.load.at": "1668027837400",
      "burst.view.suggested.sample.rate": "1.0",
      "burst.view.last.potential.item.count": "45",
      "burst.view.last.item.size": "10059.0",
      "burst.view.suggested.slice.count": "9"
    }
  }
}
 */
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
