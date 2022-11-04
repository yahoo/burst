/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client.connection

import org.burstsys.fabric.container.model.metrics.{FabricAssessment, FabricLastHourMetricCollector}
import org.burstsys.fabric.net.message.AccessParameters
import org.burstsys.fabric.net.message.assess.{FabricNetAssessReqMsg, FabricNetAssessRespMsg}
import org.burstsys.tesla.offheap
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.instrument.prettyByteSizeString
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.{git, host}

import scala.concurrent.duration._
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

  /**
   *
   * incoming assessment request from remote supervisor
   */
  def assessRequest(msg: FabricNetAssessReqMsg, parameters: AccessParameters): Unit = {
    lazy val hdr = s"FabricNetClientAssessHandler.assessRequest"
    log debug s"$hdr $msg"
    if (msg.receiverKey != clientKey)
      throw VitalsException(s"msg.receiverKey=${msg.receiverKey} != localKey=$clientKey")

    val response = FabricNetAssessRespMsg(msg, clientKey, serverKey, git.commitId, assessment(parameters))

    transmitter transmitControlMessage response
  }

  /**
   * extract the current metrics
   *
   * @return
   */
  private
  def assessment(parameters: AccessParameters): FabricAssessment = {
    FabricAssessment(
      _pingCollector.exportMetric, _lavCollector.exportMetric, _memCollector.exportMetric, _diskCollector.exportMetric,
      parameters
    )
  }

  def memoryPercentUsed: Double = percentUsed("memory", offheap.nativeMemoryMax, used = host.mappedMemoryUsed)

  lazy val assessorBackgroundFunction = new VitalsBackgroundFunction("fab-worker-assess", 15 seconds, 15 seconds, {
    _lavCollector sample host.loadAverage
    _memCollector sample memoryPercentUsed

  })

  private
  def percentUsed(name: String, total: Long, usable: Long = -1, used: Long = -1): Double = {
    val free = if (usable > -1) usable else total - used
    val percentUsed = ((total - free).toDouble / total.toDouble) * 100.0
    log debug burstStdMsg(f"resource '$name' percentUsed=$percentUsed%.2f (free=${
      prettyByteSizeString(free)
    }, total=${
      prettyByteSizeString(total)
    })")
    percentUsed
  }

}
