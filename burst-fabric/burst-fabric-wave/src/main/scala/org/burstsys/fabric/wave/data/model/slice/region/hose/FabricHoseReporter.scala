/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.region.hose

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterPercentValueMetric, VitalsReporterUnitOpMetric}

import scala.language.postfixOps

/**
 * a generic report for worker side operations on regions
 */
private[fabric]
object FabricHoseReporter extends VitalsReporter {

  final val dName: String = "fab-hose"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _writeParcelsMetric = VitalsReporterUnitOpMetric("hose_write")

  private[this]
  val _inflateParcelsMetric = VitalsReporterUnitOpMetric("hose_inflate")

  private[this]
  val _parcelCompressionMetric = VitalsReporterPercentValueMetric("hose_inflate_ratio")

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * count a parcel write with its byte size
   */
  final
  def sampleParcelWrite(ns: Long, byteSize: Int): Unit = {
    _writeParcelsMetric.recordOpWithTimeAndSize(ns, byteSize)
  }

  final
  def sampleParcelInflate(ns: Long, deflatedSize: Long, inflatedSize: Long): Unit = {
    _parcelCompressionMetric.record(inflatedSize.toDouble / deflatedSize.toDouble)
    _inflateParcelsMetric.recordOpWithTimeAndSize(ns, inflatedSize)
  }
}
