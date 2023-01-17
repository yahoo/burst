/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.server

import io.opentelemetry.api.metrics.LongUpDownCounter

import java.util.concurrent.atomic.LongAdder
import org.burstsys.nexus.NexusSliceKey
import org.burstsys.vitals.reporter.instrument.prettySizeString
import org.burstsys.vitals.reporter.{VitalsReporter, metric}
import org.burstsys.vitals.reporter.metric.VitalsReporterUnitOpMetric

private[nexus]
object NexusServerReporter extends VitalsReporter {

  final val dName: String = "nexus-server"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _serverWriteMetric = VitalsReporterUnitOpMetric("nexus_server_write")

  private[this]
  val _serverDropMetric = VitalsReporterUnitOpMetric("nexus_server_drop")

  private[this]
  val _serverSlowMetric = VitalsReporterUnitOpMetric("nexus_server_slow")

  private[this]
  val _serverHeartbeatMetric = VitalsReporterUnitOpMetric("nexus_server_heartbeat")

  private[this]
  val _serverStreamSucceedMetric = VitalsReporterUnitOpMetric("nexus_server_stream_succeed")

  private[this]
  val _serverStreamFailMetric = VitalsReporterUnitOpMetric("nexus_server_stream_fail")

  private[this]
  val _serverConnectionCounter: LongUpDownCounter = metric.meter.upDownCounterBuilder(s"nexus_server_connection_counter")
    .setDescription(s"server connections in use")
    .setUnit("connections")
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def onServerDrop(): Unit = {
    _serverDropMetric.recordOp()
  }

  final
  def onServerSlow(ns: Long): Unit = {
    _serverSlowMetric.recordOpWithTime(ns)
  }

  final
  def onServerHeartbeat(): Unit = {
    _serverHeartbeatMetric.recordOp()
  }

  final
  def onServerStreamSucceed(): Unit = {
    _serverStreamSucceedMetric.recordOp()
  }

  final
  def onServerStreamFail(): Unit = {
    _serverStreamFailMetric.recordOp()
  }

  final
  def onServerWrite(bytes: Long, ns: Long): Unit = {
    _serverWriteMetric.recordOpWithTimeAndSize(ns, bytes)
  }

  final
  def onServerConnectionStart(): Unit = {
    _serverConnectionCounter.add(1)
  }

  final
  def onServerConnectionStop(): Unit = {
    _serverConnectionCounter.add(-1)
  }
}
