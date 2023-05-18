/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import io.opentelemetry.api.metrics.LongUpDownCounter
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterFixedValueMetric, VitalsReporterUnitOpMetric}

import scala.language.postfixOps

private[fabric]
object FabricNetReporter extends VitalsReporter {

  final val dName: String = "fab-net"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _messageTransmitRate = VitalsReporterUnitOpMetric("fab_net_msg_xmit")

  private[this]
  val _messageReceiveRate = VitalsReporterUnitOpMetric("fab_net_msg_rcv")

  private[this]
  val _connectOpenMetric = VitalsReporterUnitOpMetric("fab_net_open")

  private[this]
  val _connectCloseMetric = VitalsReporterUnitOpMetric("fab_net_close")

  private[this]
  val _pingMetric = VitalsReporterFixedValueMetric("fab_net_ping_ns")

  private[this]
  val _connectOpenCounter: LongUpDownCounter = metric.meter.upDownCounterBuilder(s"${dName}_connect_open_counter")
    .setDescription(s"open connections in use")
    .setUnit("connections")
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def onMessageXmit(bytes: Long): Unit = {
        _messageTransmitRate.recordOpWithSize(bytes)
  }

  final
  def onMessageRecv(bytes: Long): Unit = {
        _messageReceiveRate.recordOpWithSize(bytes)
  }

  final
  def recordConnectOpen(): Unit = {
    _connectOpenCounter.add(1)
    _connectOpenMetric.recordOp()
  }

  final
  def recordConnectClose(): Unit = {
    _connectOpenCounter.add(1)
    _connectCloseMetric.recordOp()
  }

  final
  def recordPing(pingNs: Long): Unit = {
    _pingMetric.record(pingNs)
  }
}
