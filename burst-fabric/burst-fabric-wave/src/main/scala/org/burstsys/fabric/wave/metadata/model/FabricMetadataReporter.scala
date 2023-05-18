/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.metadata.model

import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.metric.VitalsReporterUnitOpMetric

private[fabric]
object FabricMetadataReporter extends VitalsReporter {

  final val dName: String = "fab-metadata"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _domainLookupTally = VitalsReporterUnitOpMetric("metadata_domain_lookup")

  private[this]
  val _viewLookupTally = VitalsReporterUnitOpMetric("metadata_view_lookup")

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def recordDomainLookup(): Unit = {
    _domainLookupTally.recordOp()
  }

  final
  def recordViewLookup(): Unit = {
    _viewLookupTally.recordOp()
  }
}
