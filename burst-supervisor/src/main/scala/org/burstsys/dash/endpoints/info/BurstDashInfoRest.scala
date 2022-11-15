/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.info


import org.burstsys.dash.application.BurstDashEndpointBase
import org.burstsys.dash.configuration
import org.burstsys.dash.endpoints._
import org.burstsys.vitals
import org.burstsys.vitals.instrument.{prettyByteSizeString, prettyTimeFromMillis}
import org.burstsys.vitals.json.VitalsJsonRepresentable
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net._
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.vitals.{git, host}
import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType

import scala.language.implicitConversions

@Path(InfoApiPath)
@Produces(Array(MediaType.APPLICATION_JSON))
final class BurstDashInfoRest extends BurstDashEndpointBase {

  @GET
  @Path("timezones")
  def timezones: Array[String] = {
    vitals.time.timeZoneNameList
  }

  @GET
  @Path("buildInfo")
  def buildInfo: BurstBuildInfoJson = {
    BurstBuildInfoJson()
  }

  @GET
  @Path("hostInfo")
  def hostInfo: BurstHostInfoJson = {
    resultOrErrorResponse {
      BurstHostInfoJson()
    }
  }

  @GET
  @Path("configInfo")
  def configInfo: Map[String, BurstSettingInfoJson] = {
    resultOrErrorResponse {
      VitalsPropertyRegistry.allProperties.values.map(property => {
        val value: Any = property.asOption.orNull
        property.key -> BurstSettingInfoJson(if (property.sensitive) "REDACTED" else value, property.typeName, property.description, property.source)
      }).toMap
    }
  }
}
