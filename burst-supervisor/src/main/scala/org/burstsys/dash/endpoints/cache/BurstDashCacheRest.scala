/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.cache

import org.burstsys.dash.application.BurstDashEndpointBase
import org.burstsys.dash.endpoints._
import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.ops.{FabricCacheEvict, FabricCacheFlush, FabricCacheSearch}
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid._
import jakarta.ws.rs._
import jakarta.ws.rs.core.{MediaType, Response}

import scala.jdk.CollectionConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * Endpoints for viewing/manipulating the data cached in the system.
 */
@Path(CacheApiPath)
@Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
@Produces(Array(MediaType.APPLICATION_JSON))
final class BurstDashCacheRest extends BurstDashEndpointBase {

  @GET
  @Path("generations")
  def generations(
                   @DefaultValue("_") @QueryParam("gen") generationSpec: GenerationKeyParam,
                   @DefaultValue("_") @QueryParam("params") cacheParameters: java.util.List[CacheOperationParameterParam]
                 ): Array[FabricGeneration] = {
    resultOrErrorResponse {
      val present = cacheParameters.asScala.collect { case param if param.value.isDefined => param.value.get }
      val params = if (present.isEmpty) None else Some(present.toSeq)
      val parameters = generationSpec.value.getOrElse(FabricGenerationKey())
      val guid = newBurstUid
      val promise = Promise[Array[FabricGeneration]]()
      supervisor.data.cacheGenerationOp(guid, FabricCacheSearch, parameters, params) onComplete {
        case Failure(t) => promise.failure(t)
        case Success(r) => promise.success(r.map(g => g.toJsonLite).toArray)
      }
      Await.result(promise.future, 1 minutes)
    }
  }

  @GET
  @Path("generations/{generationSpec}")
  def slices(
              @DefaultValue("") @PathParam("generationSpec") generationSpec: GenerationKeyParam
            ): FabricGeneration = {
    resultOrErrorResponse {
      val promise = Promise[FabricGeneration]()
      generationSpec.value match {
        case None =>
          val err = errorResponse(Response.Status.BAD_REQUEST, "error" -> "Generation not specified")
          promise.failure(err)
        case Some(spec) =>
          if (spec.domainKey == -1 || spec.viewKey == -1 || spec.generationClock == -1) {
            promise.failure(errorResponse(Response.Status.BAD_REQUEST, "error" -> "Generation not specified"))
          } else {
            val guid = newBurstUid
            supervisor.data.cacheGenerationOp(guid, FabricCacheSearch, spec, None) onComplete {
              case Failure(t) =>
                promise.failure(errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "error" -> s"An unknown exception occurred: ${burstStdMsg(t)}"))
              case Success(g) =>
                if (g.nonEmpty) {
                  promise.success(g.head.toJson)
                } else {
                  promise.failure(errorResponse(Response.Status.NOT_FOUND, "error" -> "Generation not found"))
                }
            }
          }
      }
      Await.result(promise.future, 10 minutes)
    }
  }

  @POST
  @Path("generations/manage")
  def manageGenerations(
                         @FormParam("action") action: String,
                         @FormParam("generation") generationSpec: GenerationKeyParam
                       ): Array[FabricGeneration] = {
    resultOrErrorResponse {
      val promise = Promise[Array[FabricGeneration]]()
      val cacheOperation = action match {
        case "evict" => FabricCacheEvict
        case "flush" => FabricCacheFlush
        case _ => respondWith(Response.Status.BAD_REQUEST, "error" -> s"Unknown action: $action")
      }
      generationSpec.value match {
        case None => respondWith(Response.Status.BAD_REQUEST, "error" -> s"Generation not specified. Saw '${generationSpec.raw}'")
        case Some(g) =>
          val guid = newBurstUid
          supervisor.data.cacheGenerationOp(guid, cacheOperation, g, None) onComplete {
            case Failure(t) => promise.failure(t)
            case Success(r) => promise.success(r.map(g => g.toJsonLite).toArray)
          }
      }
      Await.result(promise.future, 10 minutes)
    }
  }
}
