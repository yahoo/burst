/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterUnitOpMetric

private[hydra]
object HydraReporter extends VitalsReporter {

  final val dName: String = "hydra"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _parseMetric = VitalsReporterUnitOpMetric("hydra_parse", "lines")

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def recordParse(elapsedNs: Long, source: String): Unit = {
    _parseMetric.recordOpWithTimeAndSize(elapsedNs, Predef.augmentString(source).linesIterator.size)
  }
}
