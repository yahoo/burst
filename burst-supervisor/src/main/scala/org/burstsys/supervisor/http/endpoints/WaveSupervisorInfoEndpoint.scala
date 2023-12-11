/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.endpoints

import jakarta.ws.rs._
import jakarta.ws.rs.core.{MediaType, Response}
import org.burstsys.fabric
import org.burstsys.supervisor.configuration
import org.burstsys.supervisor.http.endpoints.InfoMessages.BurstKedaStatusJson
import org.burstsys.supervisor.http.endpoints.InfoMessages.{BurstBuildInfoJson, BurstHostInfoJson, BurstSettingInfoJson, BurstSettingUpdateJson}
import org.burstsys.vitals
import org.burstsys.vitals.git
import org.burstsys.vitals.host
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.net._
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.vitals.reporter.instrument.prettyByteSizeString
import org.burstsys.vitals.reporter.instrument.prettyTimeFromMillis

import scala.language.implicitConversions

@Path(InfoApiPath)
@Consumes(Array(MediaType.APPLICATION_JSON))
@Produces(Array(MediaType.APPLICATION_JSON))
final class WaveSupervisorInfoEndpoint extends WaveSupervisorEndpoint {

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
  @Path("settings")
  def configInfo: Map[String, BurstSettingInfoJson] = {
    resultOrErrorResponse {
      VitalsPropertyRegistry.allProperties.values.map(property => {
        val value: Any = property.asOption.orNull
        property.key -> BurstSettingInfoJson(if (property.sensitive) "REDACTED" else value, property.typeName, property.description, property.source)
      }).toMap
    }
  }

  @POST
  @Path("settings")
  def updateSetting(update: BurstSettingUpdateJson): Response = {
    resultOrErrorResponse {
      VitalsPropertyRegistry.allProperties(update.key).setString(update.value)
      Response.noContent().build()
    }
  }

  @GET
  @Path("keda")
  def kedaConfig: BurstKedaStatusJson = {
    resultOrErrorResponse {
      BurstKedaStatusJson(
        configuration.kedaScaleDownInterval.get.toString, configuration.kedaScaleUpWorkerCount.get.toString,
        supervisor.workersActive, supervisor.currentWorkerCount, supervisor.desiredWorkerCount,
        supervisor.scalingInfo,
      )
    }
  }
}

object InfoMessages {
  final case class BurstBuildInfoJson(
                                       branch: String = git.branch.toUpperCase,
                                       build: String = git.buildVersion,
                                       commitId: String = git.commitId.toUpperCase
                                     ) extends VitalsJsonObject

  final case class BurstHostInfoJson(
                                      hostName: String = getPublicHostName,
                                      hostAddress: String = getPublicHostAddress,
                                      restPort: Int = fabric.configuration.burstHttpPortProperty.get,
                                      osName: String = host.osName,
                                      osArchitecture: String = host.osArch,
                                      osVersion: String = host.osVersion,
                                      loadAverage: String = f"${host.loadAverage}%.1f",
                                      uptime: String = prettyTimeFromMillis(host.uptime),
                                      cores: String = host.cores,
                                      currentThreads: String = host.threadsCurrent.toString,
                                      peakThreads: String = host.threadsPeak.toString,
                                      usedHeap: String = prettyByteSizeString(host.heapUsed),
                                      committedHeap: String = prettyByteSizeString(host.heapCommitted),
                                      maxHeap: String = prettyByteSizeString(host.heapMax),
                                      gc: String = host.gcReadout
                                    ) extends VitalsJsonObject

  final case class BurstSettingInfoJson(
                                         value: Any,
                                         dataType: String,
                                         description: String,
                                         source: String
                                       ) extends VitalsJsonObject

  final case class BurstSettingUpdateJson(
                                           value: String,
                                           key: String
                                         ) extends VitalsJsonObject

  final case class BurstKedaStatusJson(
                                        scaleDownInterval: String,
                                        scaleUpWorkerCount: String,
                                        isActive: Boolean,
                                        currentWorkerCount: Int,
                                        desiredWorkerCount: Int,
                                        scalingInfo: Any,
                                      ) extends VitalsJsonObject
}
