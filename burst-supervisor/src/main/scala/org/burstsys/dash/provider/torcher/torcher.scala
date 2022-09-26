/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.provider

import org.burstsys.dash.endpoints.ClientJsonObject
import org.apache.logging.log4j.Level

package object torcher {

  final case
  class TorcherStatus(
                       running: Boolean,
                       counter: Long,
                       schema: String,
                       concurrency: Long,
                       startTimeMs: Long,
                       endTimeMs: Long,
                       elapsedTime: Long,
                       duration: String,
                       datasetCount: Long,
                       currentDatasetIndex: Long,
                       summary: Option[TorcherSummaryStats]
                     )

  final case
  class TorcherSummaryStats(
                             byteCount: Long,
                             itemCount: Long,
                             objectCount: Long,
                             reportedColdLoad5Rate: Double,
                             reportedColdLoad15Rate: Double,
                             reportedColdLoadMeanRate: Double,
                             reportedColdLoad99Duration: Double,
                             reportedColdLoadMaxDuration: Double,
                             observedColdLoad5Rate: Double,
                             observedColdLoad15Rate: Double,
                             observedColdLoadMeanRate: Double,
                             observedColdLoad99Duration: Double,
                             observedColdLoadMaxDuration: Double,
                             firstQueryFailuresCount: Long,
                             firstQueryNoDataFailuresCount: Long,
                             queryFailuresCount: Long,
                             limitCount: Long,
                             overflowCount: Long
                           )

  final case
  class TorcherDatasetStatistics(
                                  domainId: Long,
                                  viewId: Long,
                                  description: String,
                                  coldLoadAt: Long,
                                  byteCount: Long,
                                  firstQueryDuration: Double,
                                  coldLoadDuration: Double,
                                  coldLoadCount: Long,
                                  itemCount: Long,
                                  limitCount: Long,
                                  overflowCount: Long,
                                  latestFailureGuid: String,
                                  latestSuccessGuid: String
                                )

  final case class TorcherLogMessage(level: Level, message: String)

  /**
   * A service that can start/stop/report on torcher
   */
  trait TorcherEventListener {
    def torcherStarted(source: String): Unit = {}

    def torcherMessage(message: TorcherLogMessage): Unit = {}

    def torcherStopped(): Unit = {}
  }


  trait BurstDashTorcherService {
    def config: Option[String]

    def messages: Array[TorcherLogMessage]

    def getDatasetStats: Array[TorcherDatasetStatistics]

    def status: TorcherStatus

    def runStartTime: Long

    def runFinishTime: Long

    def startTorcher(source: String): Boolean

    def stopTorcher(): Boolean

    def talksTo(listener: TorcherEventListener): this.type
  }

  object TorcherMessage {
    def apply(source: String): ClientJsonObject = TorcherConfigWsMessage(source)

    def apply(status: TorcherStatus): ClientJsonObject = TorcherStatusWsMessage(status)

    def apply(message: TorcherLogMessage): ClientJsonObject = TorcherLogWsMessage(message.level.toString, message.message)
  }

  private abstract class TorcherWsMessage(val op: String) extends ClientJsonObject

  private case class TorcherLogWsMessage(level: String, data: String)
    extends TorcherWsMessage("TORCHER_MESSAGE")

  private case class TorcherStatusWsMessage(status: TorcherStatus)
    extends TorcherWsMessage("TORCHER_STATUS")

  private case class TorcherConfigWsMessage(source: String)
    extends TorcherWsMessage("TORCHER_SOURCE")


}
