/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.endpoints

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status
import org.burstsys.catalog.api.BurstCatalogApiQueryLanguageType
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.query._
import org.burstsys.catalog.model.view._
import org.burstsys.fabric
import org.burstsys.fabric.container.http.endpoints.params.IntParam
import org.burstsys.fabric.container.http.endpoints.params.LongArrayParam
import org.burstsys.fabric.container.http.endpoints.params.LongParam
import org.burstsys.fabric.wave.data.model.store.FabricStoreNameProperty
import org.burstsys.motif.Motif
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceVersionProperty
import org.burstsys.supervisor.http.endpoints.CatalogMessages.BurstCatalogDeleteSuccess
import org.burstsys.supervisor.http.endpoints.CatalogMessages.BurstCatalogMotifValidation
import org.burstsys.supervisor.http.endpoints.CatalogMessages.CatalogTreeNodeJson
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.json.VitalsJsonSanatizers.Values

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success

/**
 * The rest endpoint for performing catalog operations.
 *
 * The actual operations are implemented in the mixed-in traits.
 */
@Path(CatalogApiPath)
@Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
@Produces(Array(MediaType.APPLICATION_JSON))
final class WaveSupervisorCatalogEndpoint extends WaveSupervisorEndpoint {

  private val motif = Motif.build()

  ///////////////////////////////////////////////////////////////////////////
  // Domains and views
  ///////////////////////////////////////////////////////////////////////////

  @POST
  @Path("search")
  def search(
              @FormParam("domain") domain: String,
              @FormParam("view") view: String,
              @DefaultValue("_") @FormParam("limit") limit: IntParam
            ): Array[CatalogTreeNodeJson] = {
    resultOrErrorResponse {
      val domainIdentifier = if (domain == null || domain.isEmpty) None else Some(domain)
      val viewIdentifier = if (view == null || view.isEmpty) None else Some(view)

      catalog.searchCatalog(domainIdentifier, viewIdentifier, limit.value) match {
        case Failure(e) => respondWith(Status.INTERNAL_SERVER_ERROR, "error" -> e.getLocalizedMessage)
        case Success(rows) =>
          val tree = mutable.Map[Long, CatalogTreeNodeJson]()
          for (r <- rows) {
            val pk = r("domain_pk").toLong
            val domain = tree.getOrElseUpdate(pk, CatalogTreeNodeJson(pk, r("domain_moniker"), r("domain_udk")))
            if (r("view_pk") != null) {
              domain.children = domain.children ++ Array(CatalogTreeNodeJson(r("view_pk").toLong, r("view_moniker"), r("view_udk")))
            }
          }
          tree.values.toArray
      }
    }
  }

  @POST
  @Path("domainByPk")
  def domainByPk(@FormParam("pk") pk: Long): CatalogJsonDomain = {
    resultOrErrorResponse {
      catalog findDomainByPk pk match {
        case Failure(t) => throw t
        case Success(c) => c
      }
    }
  }

  @POST
  @Path("viewByPk")
  def viewByPk(@FormParam("pk") pk: Long): CatalogJsonView = {
    resultOrErrorResponse {
      catalog findViewByPk pk match {
        case Failure(t) => throw t
        case Success(c) => c
      }
    }
  }

  @GET
  @Path("views/{view}")
  def view(@PathParam("view") view: LongParam): CatalogJsonView = {
    resultOrErrorResponse {
      view.value match {
        case None => respondWith(Status.BAD_REQUEST, "message" -> s"Unknown view '${view.raw}'")
        case Some(pk) =>
          catalog.findViewByPk(pk) match {
            case Failure(t) => throw t
            case Success(v) => v
          }
      }
    }
  }

  /**
   * Look up a domain by pk if param is numeric, or by udk otherwise
   */
  private def domainFor(id: LongParam): CatalogDomain = id.value match {
    case None =>
      catalog.findDomainByUdk(id.raw) match {
        case Failure(_) => respondWith(Status.BAD_REQUEST, "message" -> s"Unknown domain '${id.raw}'")
        case Success(d) => d
      }
    case Some(pk) =>
      catalog.findDomainByPk(pk) match {
        case Failure(t) => throw t
        case Success(d) => d
      }
  }

  @GET
  @Path("domains")
  // For whatever reason @DefaultValue("") is not respected
  def domains(
               @DefaultValue("_") @QueryParam("limit") limit: IntParam,
               @DefaultValue("_") @QueryParam("pks") pks: LongArrayParam
             ): Array[CatalogJsonDomain] = {
    resultOrErrorResponse {
      catalog.allDomains(limit.value) match {
        case Failure(t) => throw t
        case Success(domains) =>
          pks.value
            .map(pks => domains.filter(d => pks.contains(d.pk)))
            .getOrElse(domains)
            .map(d => d: CatalogJsonDomain)
      }
    }
  }

  @GET
  @Path("domains/{domain}")
  def domain(@PathParam("domain") domainParam: LongParam): CatalogJsonDomain = {
    resultOrErrorResponse {
      domainFor(domainParam)
    }
  }

  @GET
  @Path("domains/{domain}/{view}")
  def viewInDomain(@PathParam("domain") domainParam: LongParam, @PathParam("view") view: LongParam): CatalogJsonView = {
    resultOrErrorResponse {
      val domain = domainFor(domainParam)
      catalog.allViewsForDomain(domain.pk) match {
        case Success(views) =>
          views.find(v => view.value.contains(v.pk) || v.udk.contains(view.raw)).getOrElse(
            respondWith(Status.NOT_FOUND, "message" -> s"Could not find view ${view.raw} in domain pk=${domain.pk} udk=${domain.udk.orNull}")
          )
        case Failure(exception) => throw exception
      }
    }
  }

  @GET
  @Path("domains/{domain}/views")
  def viewsByDomain(@PathParam("domain") domainParam: LongParam): Array[CatalogJsonView] = {
    resultOrErrorResponse {
      val domain = domainFor(domainParam)
      catalog.allViewsForDomain(domain.pk) match {
        case Failure(t) => throw t
        case Success(views) => views.map(v => v: CatalogJsonView)
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
      if (motif.getSchema(schemaName) == null)
        throw new WebApplicationException(s"Unknown schema name '$schemaName''")
      // set some defaults
      val storeProperties = defaultStoreProperties
      val viewProperties = defaultViewProperties
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

  def defaultStoreProperties: Map[String, String] = Map(
    FabricStoreNameProperty -> "sample",
    SampleStoreSourceNameProperty -> "AppEventsBrio",
    SampleStoreSourceVersionProperty -> "0.0"
  )

  def defaultViewProperties: Map[String, String] = Map(
    fabric.wave.metadata.ViewNextLoadStaleMsProperty -> (1 day).toMillis.toString
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

  @POST
  @Path("updateDomain")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def updateDomain(domain: CatalogJsonDomain): CatalogJsonDomain = {
    resultOrErrorResponse {
      catalog.ensureDomain(domain) match {
        case Failure(t) => throw t
        case Success(pk) => catalog.findDomainByPk(pk) match {
          case Failure(t) => throw t
          case Success(d) => d
        }
      }
    }
  }

  @POST
  @Path("updateView")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def updateView(view: CatalogJsonView): CatalogJsonView = {
    resultOrErrorResponse {
      catalog.updateView(view) match {
        case Failure(t) => throw t
        case Success(pk) => catalog.findViewByPk(pk) match {
          case Failure(t) => throw t
          case Success(v) => v
        }
      }
    }
  }

  @POST
  @Path("deleteView")
  def deleteView(@FormParam("pk") pk: Long): BurstCatalogDeleteSuccess = {
    resultOrErrorResponse {
      catalog deleteView pk match {
        case Failure(t) =>
          throw new WebApplicationException(t, Response.Status.BAD_REQUEST)
        case Success(_) =>
          BurstCatalogDeleteSuccess(pk)
      }
    }
  }

  @POST
  @Path("deleteDomain")
  def deleteDomain(@FormParam("pk") pk: Long): BurstCatalogDeleteSuccess = {
    resultOrErrorResponse {
      catalog deleteDomain pk match {
        case Failure(t) =>
          throw new WebApplicationException(t, Response.Status.BAD_REQUEST)
        case Success(_) =>
          BurstCatalogDeleteSuccess(pk)
      }
    }
  }

  @POST
  @Path("validateMotif")
  def validateMotif(@FormParam("schemaName") schemaName: String,
                    @FormParam("motif") motifSource: String): BurstCatalogMotifValidation = {
    resultOrErrorResponse {
      try {
        if (!motif.isSchemaRegistered(schemaName)) {
          throw new WebApplicationException(
            VitalsException(s"schema $schemaName is unrecognized"), Response.Status.BAD_REQUEST)
        }
        motif.parseView(schemaName, motifSource)
        BurstCatalogMotifValidation()
      } catch safely {
        case t: Throwable =>
          BurstCatalogMotifValidation(success = false, t.getMessage)
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // Queries
  ///////////////////////////////////////////////////////////////////////////

  @POST
  @Path("allQueries")
  def allQueries(@FormParam("limit") limit: String): Array[CatalogJsonQuery] = {
    resultOrErrorResponse {
      val limitValue = if (limit == null || limit.isEmpty) None else Some(limit.toInt)
      catalog allQueries limitValue match {
        case Failure(t) => throw t
        case Success(querys) =>
          querys.map(c => c: CatalogJsonQuery)
      }
    }
  }

  @POST
  @Path("queryByPk")
  def queryByPk(@FormParam("pk") pk: Long): CatalogJsonQuery = {
    resultOrErrorResponse {
      catalog findQueryByPk pk match {
        case Failure(t) => throw t
        case Success(c) => c
      }
    }
  }

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
  @Path("updateQuery")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def updateQuery(query: CatalogJsonQuery): CatalogJsonQuery = {
    resultOrErrorResponse {
      catalog.updateQuery(query) match {
        case Failure(t) => throw t
        case Success(c) => c
      }
    }
  }

  @POST
  @Path("deleteQuery")
  def deleteQuery(@FormParam("pk") pk: Long): BurstCatalogDeleteSuccess = {
    resultOrErrorResponse {
      catalog deleteQuery pk match {
        case Failure(t) =>
          throw new WebApplicationException(t, Response.Status.BAD_REQUEST)
        case Success(_) =>
          BurstCatalogDeleteSuccess(pk)
      }
    }
  }
}

object CatalogMessages {

  case class BurstCatalogMotifValidation(success: Boolean = true, message: String = "valid!")

  case class BurstCatalogDeleteSuccess(pk: Long, success: Boolean = true, message: String = "valid!")

  case class CatalogTreeNodeJson(pk: Long,
                                 @JsonSerialize(using = classOf[Values]) moniker: String,
                                 @JsonSerialize(using = classOf[Values]) udk: String,
                                 var children: Array[CatalogTreeNodeJson] = Array.empty
                                ) extends ClientJsonObject

}
