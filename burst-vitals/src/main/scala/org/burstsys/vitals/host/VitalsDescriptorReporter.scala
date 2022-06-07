/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.host

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterFixedValueMetric

import scala.language.postfixOps

/**
 * helper types/functions for Host/Process/Os state
 */
private[vitals]
object VitalsDescriptorReporter extends VitalsReporter {

  final val dName: String = "vitals-descriptor"

  /////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  final val _openFiles = VitalsReporterFixedValueMetric("host_open_files")

  private[this]
  final val _maxFiles = VitalsReporterFixedValueMetric("host_max_files")

  this += _openFiles
  this += _maxFiles

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    newSample()
    _openFiles.sample(sampleMs)
    _openFiles.sample(sampleMs)
    _maxFiles.sample(sampleMs)
    _openFiles.record(openFiles)
    _maxFiles.record(maxFiles)
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def report: String = {
    if (nullData) return ""
    s"${_openFiles.report}${_maxFiles.report}"
  }

}
