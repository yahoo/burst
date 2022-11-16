/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import java.util.concurrent.atomic.LongAdder

import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterFixedValueMetric}

import scala.language.postfixOps

private[fabric]
object FabricNetReporter extends VitalsReporter {

  final val dName: String = "fab-net"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _messageTransmitRate = VitalsReporterByteOpMetric("fab_net_msg_xmit")
  this += _messageTransmitRate

  private[this]
  val _messageReceiveRate = VitalsReporterByteOpMetric("fab_net_msg_rcv")
  this += _messageReceiveRate

  private[this]
  val _connectOpenMetric = VitalsReporterByteOpMetric("fab_net_open")
  this += _connectOpenMetric

  private[this]
  val _connectCloseMetric = VitalsReporterByteOpMetric("fab_net_close")
  this += _connectCloseMetric

  private[this]
  val _pingMetric = VitalsReporterFixedValueMetric("fab_net_ping_ns")
  this += _pingMetric

  private[this]
  val _connectOpenTally = new LongAdder()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: FabricNetMessageId): Unit = {
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def onMessageXmit(bytes: Long): Unit = {
    newSample()
    _messageTransmitRate.recordOpWithSize(bytes)
  }

  final
  def onMessageRecv(bytes: Long): Unit = {
    newSample()
    _messageReceiveRate.recordOpWithSize(bytes)
  }

  final
  def recordConnectOpen(): Unit = {
    newSample()
    _connectOpenTally.increment()
    _connectOpenMetric.recordOp()
  }

  final
  def recordConnectClose(): Unit = {
    newSample()
    _connectOpenTally.decrement()
    _connectCloseMetric.recordOp()
  }

  final
  def recordPing(pingNs: Long): Unit = {
    newSample()
    _pingMetric.record(pingNs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    val connects = s"\tfab_net_open_count=${_connectOpenTally.longValue} (${prettyFixedNumber(_connectOpenTally.longValue)}),\n${_connectOpenMetric.report}${_connectCloseMetric.report}"
    val rates = s"${_messageTransmitRate.report}${_messageReceiveRate.report}"
    s"$connects$rates${_pingMetric.report}\n"
  }

}
