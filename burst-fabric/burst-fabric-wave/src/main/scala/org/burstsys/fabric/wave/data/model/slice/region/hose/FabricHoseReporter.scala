/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.region.hose

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterPercentValueMetric}

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
  val _writeParcelsMetric = VitalsReporterByteOpMetric("hose_write")
  this += _writeParcelsMetric

  private[this]
  val _inflateParcelsMetric = VitalsReporterByteOpMetric("hose_inflate")
  this += _inflateParcelsMetric

  private[this]
  val _parcelCompressionMetric = VitalsReporterPercentValueMetric("hose_inflate_ratio")
  this += _parcelCompressionMetric

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def sample(sampleMs: Long): Unit = {
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * count a parcel write with its byte size
   *
   * @param byteSize
   * @param ns
   */
  final
  def sampleParcelWrite(ns: Long, byteSize: Int): Unit = {
    newSample()
    _writeParcelsMetric.recordOpWithTimeAndSize(ns, byteSize)
  }

  final
  def sampleParcelInflate(ns: Long, deflatedSize: Long, inflatedSize: Long): Unit = {
    newSample()
    _parcelCompressionMetric.record(inflatedSize.toDouble / deflatedSize.toDouble)
    _inflateParcelsMetric.recordOpWithTimeAndSize(ns, inflatedSize)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    s"${_writeParcelsMetric.report}${_inflateParcelsMetric.report}${_parcelCompressionMetric.report}"
  }

}
