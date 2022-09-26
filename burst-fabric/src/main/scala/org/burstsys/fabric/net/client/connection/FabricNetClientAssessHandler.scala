/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client.connection

import org.burstsys.fabric.container.model.metrics.{FabricAssessment, FabricLastHourMetricCollector, FabricRateMeter}
import org.burstsys.fabric.data.model.limits
import org.burstsys.fabric.net.message.assess.{FabricNetAssessReqMsg, FabricNetAssessRespMsg}
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.VitalsException
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
   * queries per second (normalized to 0-10)
   */
  private[this]
  val _qpsMeter = FabricRateMeter()
  private[this]
  val _qpsCollector: FabricLastHourMetricCollector = FabricLastHourMetricCollector().initialize

  /**
   * warm (cache) loads per second (normalized to 0-10)
   */
  private[this]
  val _wlsCollector: FabricLastHourMetricCollector = FabricLastHourMetricCollector().initialize
  private[this]
  val _wlsMeter = FabricRateMeter()

  /**
   * cold (store) loads per second (normalized to 0-10)
   */
  private[this]
  val _clsCollector: FabricLastHourMetricCollector = FabricLastHourMetricCollector().initialize
  private[this]
  val _clsMeter = FabricRateMeter()

  /**
   * number of errors per second of any sort (normalized to 0-10)
   */
  private[this]
  val _errorCollector: FabricLastHourMetricCollector = FabricLastHourMetricCollector().initialize
  private[this]
  val _errorMeter = FabricRateMeter()

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
   *
   * @param msg
   */
  def assessRequest(msg: FabricNetAssessReqMsg): Unit = {
    lazy val hdr = s"FabricNetClientAssessHandler.assessRequest"
    log debug s"$hdr $msg"
    if (msg.receiverKey != clientKey)
      throw VitalsException(s"msg.receiverKey=${msg.receiverKey} != localKey=$clientKey")

    val response = FabricNetAssessRespMsg(msg, clientKey, serverKey, git.commitId, assessment)

    transmitter transmitControlMessage response
  }

  /**
   * extract the current metrics
   *
   * @return
   */
  private
  def assessment: FabricAssessment = {
    FabricAssessment(
      _pingCollector.exportMetric, _qpsCollector.exportMetric, _wlsCollector.exportMetric, _clsCollector.exportMetric,
      _lavCollector.exportMetric, _memCollector.exportMetric, _diskCollector.exportMetric, _errorCollector.exportMetric
    )
  }


  lazy val assessorBackgroundFunction = new VitalsBackgroundFunction("fab-worker-assess", 15 seconds, 15 seconds, {
    // TODO tons of assessment info
    _lavCollector sample host.loadAverage
    _memCollector sample limits.memoryPercentUsed
    _diskCollector sample limits.diskPercentUsed
  })

}
