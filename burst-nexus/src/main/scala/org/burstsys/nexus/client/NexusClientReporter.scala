/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client

import java.util.concurrent.atomic.LongAdder

import org.burstsys.nexus.NexusSliceKey
import org.burstsys.vitals.reporter.instrument.prettySizeString
import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterFloatValueMetric, VitalsReporterPercentValueMetric, VitalsReporterUnitOpMetric}

private[nexus]
object NexusClientReporter extends VitalsReporter {


  final val dName: String = "nexus-client"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _clientParcelReadMetric = VitalsReporterByteOpMetric("nexus_client_parcel_read")
  this += _clientParcelReadMetric

  private[this]
  val _clientCompressionMetric = VitalsReporterFloatValueMetric("nexus_client_parcel_compression")
  this += _clientCompressionMetric

  private[this]
  val _clientSlowMetric = VitalsReporterUnitOpMetric("nexus_client_slow")
  this += _clientSlowMetric

  private[this]
  val _clientTimeoutMetric = VitalsReporterUnitOpMetric("nexus_client_timeout")
  this += _clientTimeoutMetric

  private[this]
  val _clientHeartbeatMetric = VitalsReporterUnitOpMetric("nexus_client_heartbeat")
  this += _clientHeartbeatMetric

  private[this]
  val _clientStreamSucceedMetric = VitalsReporterUnitOpMetric("nexus_client_stream_succeed")
  this += _clientStreamSucceedMetric

  private[this]
  val _clientStreamFailMetric = VitalsReporterUnitOpMetric("nexus_client_stream_fail")
  this += _clientStreamFailMetric

  private[this]
  val _clientConnectionCount = new LongAdder

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
  def onParcelRead(deflatedBytes: Long, inflatedBytes: Long): Unit = {
    newSample()
    if (inflatedBytes != 0)
      _clientCompressionMetric.record(inflatedBytes.toDouble / deflatedBytes.toDouble)
    _clientParcelReadMetric.recordOpWithSize(bytes = deflatedBytes)
  }

  def onClientSlow(ns: Long): Unit = {
    newSample()
    _clientSlowMetric.recordOpWithTime(ns)
  }

  def onClientTimeout(ns: Long): Unit = {
    newSample()
    _clientTimeoutMetric.recordOpWithTime(ns)
    _clientStreamFailMetric.recordOp()
  }

  def onClientHeartbeat(): Unit = {
    newSample()
    _clientHeartbeatMetric.recordOp()
  }

  final
  def onClientStreamSucceed(): Unit = {
    newSample()
    _clientStreamSucceedMetric.recordOp()
  }

  final
  def onClientStreamFail(): Unit = {
    newSample()
    _clientStreamFailMetric.recordOp()
  }

  final
  def onClientConnectionStart(): Unit = {
    newSample()
    _clientConnectionCount.increment()
  }

  final
  def onClientConnectionStop(): Unit = {
    newSample()
    _clientConnectionCount.decrement()
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    val connections = s"\tnexus_client_connections=${_clientConnectionCount.longValue} (${prettySizeString(_clientConnectionCount.longValue)}) \n "
    val rates = s"${_clientParcelReadMetric.report}${_clientSlowMetric.report}${_clientTimeoutMetric.report}${_clientHeartbeatMetric.report} "
    val streams = s"${_clientStreamSucceedMetric.report}${_clientStreamFailMetric.report} "
    s"$connections$rates$streams${_clientCompressionMetric.report}"
  }

}
