/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client

import io.opentelemetry.api.metrics.LongUpDownCounter
import org.burstsys.vitals.reporter.metric.{VitalsReporterFloatValueMetric, VitalsReporterUnitOpMetric}
import org.burstsys.vitals.reporter.{VitalsReporter, metric}

private[nexus]
object NexusClientReporter extends VitalsReporter {


  final val dName: String = "nexus-client"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _clientParcelReadMetric = VitalsReporterUnitOpMetric("nexus_client_parcel_read")

  private[this]
  val _clientCompressionMetric = VitalsReporterFloatValueMetric("nexus_client_parcel_compression")

  private[this]
  val _clientSlowMetric = VitalsReporterUnitOpMetric("nexus_client_slow")

  private[this]
  val _clientTimeoutMetric = VitalsReporterUnitOpMetric("nexus_client_timeout")

  private[this]
  val _clientHeartbeatMetric = VitalsReporterUnitOpMetric("nexus_client_heartbeat")

  private[this]
  val _clientStreamSucceedMetric = VitalsReporterUnitOpMetric("nexus_client_stream_succeed")

  private[this]
  val _clientStreamFailMetric = VitalsReporterUnitOpMetric("nexus_client_stream_fail")

  private[this]
  val _clientConnectionCounter: LongUpDownCounter = metric.upDownCounter(s"nexus_client_connection_counter")
    .setDescription(s"client connections in use")
    .setUnit("connections")
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def onParcelRead(deflatedBytes: Long, inflatedBytes: Long): Unit = {
    if (inflatedBytes != 0)
      _clientCompressionMetric.record(inflatedBytes.toDouble / deflatedBytes.toDouble)
    _clientParcelReadMetric.recordOpWithSize(deflatedBytes)
  }

  def onClientSlow(ns: Long): Unit = {
    _clientSlowMetric.recordOpWithTime(ns)
  }

  def onClientTimeout(ns: Long): Unit = {
    _clientTimeoutMetric.recordOpWithTime(ns)
    _clientStreamFailMetric.recordOp()
  }

  def onClientHeartbeat(): Unit = {
    _clientHeartbeatMetric.recordOp()
  }

  final
  def onClientStreamSucceed(): Unit = {
    _clientStreamSucceedMetric.recordOp()
  }

  final
  def onClientStreamFail(): Unit = {
    _clientStreamFailMetric.recordOp()
  }

  final
  def onClientConnectionStart(): Unit = {
    _clientConnectionCounter.add(1)
  }

  final
  def onClientConnectionStop(): Unit = {
    _clientConnectionCounter.add(-1)
  }
}
