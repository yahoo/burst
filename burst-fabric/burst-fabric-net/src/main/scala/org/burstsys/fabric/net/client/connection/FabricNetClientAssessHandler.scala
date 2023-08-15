/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client.connection

import org.burstsys.fabric
import org.burstsys.fabric.container.metrics.FabricAssessment
import org.burstsys.fabric.container.model.metrics.FabricLastHourMetricCollector
import org.burstsys.fabric.net.message.assess.{FabricNetAssessReqMsg, FabricNetAssessRespMsg}
import org.burstsys.tesla.offheap
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.reporter.instrument.prettyByteSizeString
import org.burstsys.vitals.{git, host}

import scala.language.postfixOps

/**
 * client side management of assess requests
 */
trait FabricNetClientAssessHandler {

  self: FabricNetClientConnectionContext =>

  /**
   * This is actually done on the supervisor (normalized to 0-10)
   */
  private[this]
  val _pingCollector: FabricLastHourMetricCollector = FabricLastHourMetricCollector().initialize

  /**
   * unix process load average (normalized to a percentage from 0-10)
   */
  private[this]
  val _lavCollector: FabricLastHourMetricCollector = FabricLastHourMetricCollector().initialize

  /**
   * free memory (normalized to a percentage from 0-10)
   */
  private[this]
  val _memCollector: FabricLastHourMetricCollector = FabricLastHourMetricCollector().initialize

  /**
   * free disk (normalized to a percentage from 0-10)
   */
  private[this]
  val _diskCollector: FabricLastHourMetricCollector = FabricLastHourMetricCollector().initialize

  def getAssessMessage(msg: FabricNetAssessReqMsg): FabricNetAssessRespMsg = {
    FabricNetAssessRespMsg(msg, clientKey, serverKey, git.commitId, FabricAssessment(
      _pingCollector.exportMetric, _lavCollector.exportMetric, _memCollector.exportMetric, _diskCollector.exportMetric
    ))
  }

  def memoryPercentUsed: Double = percentUsed("memory", offheap.nativeMemoryMax, used = host.mappedMemoryUsed)

  lazy val assessorBackgroundFunction = new VitalsBackgroundFunction("fab-worker-assess",
    fabric.configuration.burstFabricTopologyAssessmentPeriodMs.get,
    fabric.configuration.burstFabricTopologyAssessmentPeriodMs.get,
    {
      _lavCollector sample host.loadAverage
      _memCollector sample memoryPercentUsed
    })

  private def percentUsed(name: String, total: Long, usable: Long = -1, used: Long = -1): Double = {
    val free = if (usable > -1) usable else total - used
    val percentUsed = ((total - free).toDouble / total.toDouble) * 100.0
    val freeStr = prettyByteSizeString(free)
    val totalStr = prettyByteSizeString(total)
    log debug burstStdMsg(f"resource '$name' percentUsed=$percentUsed%.2f (free=$freeStr, total=$totalStr)")
    percentUsed
  }

}
