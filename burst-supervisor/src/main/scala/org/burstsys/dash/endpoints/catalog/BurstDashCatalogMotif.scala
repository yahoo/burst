/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.catalog

import org.burstsys.motif.schema.model.MotifSchema
import org.burstsys.motif.Motif
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import jakarta.ws.rs._
import jakarta.ws.rs.core.{MediaType, Response}

trait BurstDashCatalogMotif extends AnyRef {

  case class BurstCatalogMotifValidation(success: Boolean = true, message: String = "valid!")

  val motif: Motif = Motif.build()

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Queries
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  @POST
  @Path("validateMotif")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def validateMotif(@FormParam("schemaName") schemaName: String,
                    @FormParam("motif") motifSource: String): BurstCatalogMotifValidation = {
    try {
      try {
        if (!motif.isSchemaRegistered(schemaName)) {
          throw new WebApplicationException(
            new RuntimeException(s"schema $schemaName is unrecognized"),
            Response.Status.INTERNAL_SERVER_ERROR)
        }
        motif.parseView(schemaName, motifSource)
      } catch safely {
        case t: Throwable =>
          return BurstCatalogMotifValidation(success = false, t.getMessage)
      }
      BurstCatalogMotifValidation()
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR)
    }
  }

}
