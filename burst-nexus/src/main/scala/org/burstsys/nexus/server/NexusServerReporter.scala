/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.server

import java.util.concurrent.atomic.LongAdder

import org.burstsys.nexus.NexusSliceKey
import org.burstsys.vitals.reporter.instrument.prettySizeString
import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterUnitOpMetric}

private[nexus]
object NexusServerReporter extends VitalsReporter {

  final val dName: String = "nexus-server"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _serverWriteMetric = VitalsReporterByteOpMetric("nexus_server_write")
  this += _serverWriteMetric

  private[this]
  val _serverDropMetric = VitalsReporterUnitOpMetric("nexus_server_drop")
  this += _serverDropMetric

  private[this]
  val _serverSlowMetric = VitalsReporterUnitOpMetric("nexus_server_slow")
  this += _serverSlowMetric

  private[this]
  val _serverHeartbeatMetric = VitalsReporterUnitOpMetric("nexus_server_heartbeat")
  this += _serverHeartbeatMetric

  private[this]
  val _serverStreamSucceedMetric = VitalsReporterUnitOpMetric("nexus_server_stream_succeed")
  this += _serverStreamSucceedMetric

  private[this]
  val _serverStreamFailMetric = VitalsReporterUnitOpMetric("nexus_server_stream_fail")
  this += _serverStreamFailMetric

  private[this]
  val _serverConnectionCount = new LongAdder

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: NexusSliceKey): Unit = {
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def onServerDrop(): Unit = {
    newSample()
    _serverDropMetric.recordOp()
  }

  final
  def onServerSlow(ns: Long): Unit = {
    newSample()
    _serverSlowMetric.recordOpWithTime(ns)
  }

  final
  def onServerHeartbeat(): Unit = {
    newSample()
    _serverHeartbeatMetric.recordOp()
  }

  final
  def onServerStreamSucceed(): Unit = {
    newSample()
    _serverStreamSucceedMetric.recordOp()
  }

  final
  def onServerStreamFail(): Unit = {
    newSample()
    _serverStreamFailMetric.recordOp()
  }

  final
  def onServerWrite(bytes: Long, ns: Long): Unit = {
    newSample()
    _serverWriteMetric.recordOpWithTimeAndSize(ns = ns, bytes = bytes)
  }

  final
  def onServerConnectionStart(): Unit = {
    newSample()
    _serverConnectionCount.increment()
  }

  final
  def onServerConnectionStop(): Unit = {
    newSample()
    _serverConnectionCount.decrement()
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    val connections = s"\tnexus_server_connections=${_serverConnectionCount.longValue} (${prettySizeString(_serverConnectionCount.longValue)})\n"
    val rates = s"${_serverWriteMetric.report}${_serverDropMetric.report}${_serverSlowMetric.report}${_serverHeartbeatMetric.report}"
    val streams = s"${_serverStreamSucceedMetric.report}${_serverStreamFailMetric.report}"
    s"$connections$rates$streams"
  }

}
