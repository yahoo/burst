/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.torcher


import org.burstsys.dash.application.BurstDashEndpointBase
import org.burstsys.dash.endpoints._
import org.burstsys.dash.provider.torcher.TorcherDatasetStatistics
import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType
import org.joda.time.DateTime

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 */
@Path(TorcherApiPath)
@Produces(Array(MediaType.APPLICATION_JSON))
final class BurstDashTorcherRest extends BurstDashEndpointBase {

  private val CSV_HEADERS: String = Array(
    "DomainId",
    "ProjectId + Moniker",
    "Cold Load Start Time (ms)",
    "Cold Load Start Time",
    "Loads Size (bytes)",
    "Cold Load Duration (Observed 99th -ns)",
    "Cold Load Duration (Reported 99th -ns)",
    "Cold Load Count",
    "Item Count",
    "Limit Count",
    "Overflow Count",
    "Latest Success GID",
    "Latest Failure GID"
  ).mkString(",")


  @GET
  @Path("getColdLoadStatisticsTableCSV")
  @Produces(Array("text/csv"))
  def getColdLoadStatsTable: String = {
    resultOrErrorResponse {
      val stats = torcher.getDatasetStats
      if (stats.isEmpty)
        return ""

      val csvTable: ArrayBuffer[String] = mutable.ArrayBuffer[String](CSV_HEADERS)
      csvTable ++= stats.map(rowToLine)
      val end = new DateTime(torcher.runFinishTime)
      val header = s"start=${new DateTime(torcher.runStartTime)}, end=${if (end.getMillis == 0) "running" else end.toString}"
      s"$header\n${csvTable.mkString("\n")}"
    }
  }

  private def rowToLine(dataset: TorcherDatasetStatistics): String = {
    val coldLoad = new DateTime(dataset.coldLoadAt)
    Array(
      dataset.domainId,
      dataset.description.replace(',', '.'),
      coldLoad.getMillis,
      coldLoad,
      dataset.byteCount,
      f"${dataset.firstQueryDuration}%5.4f",
      f"${dataset.coldLoadDuration}%5.4f",
      dataset.coldLoadCount,
      dataset.itemCount,
      dataset.limitCount,
      dataset.overflowCount,
      dataset.latestSuccessGuid.replace(',', '.'),
      dataset.latestFailureGuid.replace(',', '.')
    ).mkString(",")
  }

  @POST
  @Path("startTorcher")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  def startTorcher(@FormParam("source") source: String): Boolean = {
    resultOrErrorResponse {
      torcher.startTorcher(source)
    }
  }

  @GET
  @Path("stopTorcher")
  def stopTorcher(): Boolean = {
    resultOrErrorResponse {
      torcher.stopTorcher()
    }
  }
}
