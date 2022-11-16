/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.host

import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.net._
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterFloatValueMetric

import scala.language.postfixOps

/**
 * helper types/functions for Host/Process/Os state
 */
private[vitals]
object VitalsHostReporter extends VitalsReporter {

  final val dName: String = "vitals-host"

  /////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  final val _systemLoadMetric = VitalsReporterFloatValueMetric("host_load_avg")
  this += _systemLoadMetric

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    newSample()
    _systemLoadMetric.record(loadAverage)
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def report: String = {
    if (nullData) return ""
    s"\thost_name=${getPublicHostName}, host_addr=${getPublicHostAddress}, proc_id=$localProcessId, host_uptime=$uptime ms (${prettyTimeFromMillis(uptime)}),\n\thost_os_name=$osName, host_os_version=$osVersion, host_os_arch=$osArch,\n${_systemLoadMetric.report}"
  }

}
