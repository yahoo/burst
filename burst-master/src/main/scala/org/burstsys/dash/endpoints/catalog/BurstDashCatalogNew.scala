/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.catalog

import org.burstsys.catalog.api.BurstCatalogApiQueryLanguageType
import org.burstsys.catalog.model.cell._
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.master._
import org.burstsys.catalog.model.query._
import org.burstsys.catalog.model.site._
import org.burstsys.catalog.model.view._
import org.burstsys.catalog.model.worker._
import org.burstsys.dash.application.BurstDashEndpointBase
import org.burstsys.fabric
import org.burstsys.fabric.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.metadata
import org.burstsys.fabric.topology.model.node.UnknownFabricNodePort
import org.burstsys.motif.Metadata
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceVersionProperty
import org.burstsys.vitals.errors.VitalsException

import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success

/**
 * Endpoints for creating new catalog object
 */
trait BurstDashCatalogNew {
  self: BurstDashEndpointBase =>

  private val metadata = Metadata.build()

  @POST
  @Path("newQuery")
  def newQuery(@FormParam("moniker") moniker: String, @FormParam("language") language: String,
               @FormParam("source") source: String): CatalogJsonQuery = {
    resultOrErrorResponse {
      BurstCatalogApiQueryLanguageType valueOf language match {
        case None =>
          throw new WebApplicationException(VitalsException(s"Unknown language $language"), Response.Status.BAD_REQUEST)
        case Some(lt) =>
          val q = BurstCatalogQuery(0, moniker, lt, source)
          catalog insertQuery q match {
            case Failure(t) =>
              throw new WebApplicationException(t, Response.Status.BAD_REQUEST)
            case Success(c) =>
              CatalogJsonQuery(c, moniker, Map.empty, lt.name, source)
          }
      }
    }
  }

  @POST
  @Path("newView")
  def newView(
               @FormParam("moniker") moniker: String,
               @FormParam("domainPk") domainPk: Long,
               @FormParam("schemaName") schemaName: String
             ): CatalogJsonView = {
    resultOrErrorResponse {
      if (metadata.getSchema(schemaName) == null)
        throw new WebApplicationException(s"Unknown schema name '$schemaName''")
      // set some defaults
      val storeProperties = defaultPropertiesForStore
      val viewProperties = defaultPropertiesForView
      val viewMotif = defaultMotifForSchema(schemaName)
      val v = CatalogView(0, moniker, domainPk, schemaName, storeProperties = storeProperties, viewMotif = viewMotif, viewProperties = viewProperties)
      catalog.insertView(v) match {
        case Failure(t) => throw new WebApplicationException(t, Response.Status.BAD_REQUEST)
        case Success(pk) =>
          CatalogJsonView(pk, moniker, v.viewProperties.toMap, domainPk, generationClock = 0, schemaName, v.storeProperties.toMap, v.viewMotif,
            v.labels.getOrElse(Map()).toMap, v.udk.orNull)
      }
    }
  }

  def defaultPropertiesForStore: Map[String, String] = Map(
    FabricStoreNameProperty -> "sample",
    SampleStoreSourceNameProperty -> "AppEventsBrio",
    SampleStoreSourceVersionProperty -> "0.0"
  )

  def defaultPropertiesForView: Map[String, String] = Map(
    fabric.metadata.ViewNextLoadStaleMsProperty -> (1 day).toMillis.toString
  )

  def defaultMotifForSchema(schema: String): String = {
    schema.trim.toLowerCase match {
      case "unity" =>
        """VIEW template {
          |  INCLUDE user WHERE user.application.firstUse.sessionTime >= (NOW - DAYS(30))
          |  INCLUDE user.sessions.events where false
          |}
        """.stripMargin
      case "quo" =>
        """VIEW template {
          |  INCLUDE user WHERE user.project.installTime >= (NOW - DAYS(30))
          |  INCLUDE user.sessions.events where false
          |}
        """.stripMargin
      case _ => ""
    }
  }

  @POST
  @Path("newDomain")
  def newDomain(@FormParam("moniker") moniker: String): CatalogJsonDomain = {
    resultOrErrorResponse {
      val d = CatalogDomain(0, moniker)
      catalog.ensureDomain(d) match {
        case Failure(t) => throw new WebApplicationException(t, Response.Status.BAD_REQUEST)
        case Success(pk) => CatalogJsonDomain(pk, moniker, Map.empty, null, Map.empty)
      }
    }
  }

  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Path("domain")
  def domain(json: CatalogJsonDomain): CatalogJsonDomain = {
    resultOrErrorResponse {
      catalog ensureDomain json match {
        case Failure(t) => respondWith(Response.Status.BAD_REQUEST, "error" -> t.getLocalizedMessage)
        case Success(pk) => json.copy(pk = pk)
      }
    }
  }

}
