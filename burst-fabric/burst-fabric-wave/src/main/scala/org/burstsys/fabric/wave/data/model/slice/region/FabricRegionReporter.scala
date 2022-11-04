/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.region

import java.util.concurrent.atomic.LongAdder

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterPercentValueMetric}

import scala.language.postfixOps

/**
 * a generic report for worker side operations on regions
 */
private[fabric]
object FabricRegionReporter extends VitalsReporter {

  final val dName: String = "fab-region"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _readOpensMetric = VitalsReporterByteOpMetric("region_read_opens")
  this += _readOpensMetric

  private[this]
  val _readClosesMetric = VitalsReporterByteOpMetric("region_read_closes")
  this += _readClosesMetric

  private[this]
  val _writeOpensMetric = VitalsReporterByteOpMetric("region_write_opens")
  this += _writeOpensMetric

  private[this]
  val _writeParcelsMetric = VitalsReporterByteOpMetric("region_parcel_writes")
  this += _writeParcelsMetric

  private[this]
  val _inflateParcelsMetric = VitalsReporterByteOpMetric("region_parcel_inflate")
  this += _inflateParcelsMetric

  private[this]
  val _parcelCompressionMetric = VitalsReporterPercentValueMetric("region_parcel_compression")
  this += _parcelCompressionMetric

  private[this]
  val _currentRegionsOpen = new LongAdder

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
   * count a region write open
   */
  final
  def countWriteOpen(): Unit = {
    newSample()
    _writeOpensMetric.recordOp()
  }

  final
  def countWriteClose(): Unit = {
    newSample()
  }

  /**
   * count a parcel write with its byte size
   *
   * @param byteSize
   */
  final
  def countParcelWrite(byteSize: Long): Unit = {
    newSample()
    _writeParcelsMetric.recordOpWithSize(byteSize)
  }

  final
  def recordParcelInflate(ns:Long, deflatedSize: Long, inflatedSize:Long): Unit = {
    newSample()
    _parcelCompressionMetric.record(inflatedSize.toDouble/deflatedSize.toDouble)
    _inflateParcelsMetric.recordOpWithTimeAndSize(ns, inflatedSize)
  }

  /**
   * count a region read open with its byte size
   *
   * @param byteSize
   */
  final
  def countReadOpen(byteSize: Long): Unit = {
    newSample()
    _readOpensMetric.recordOpWithSize(byteSize)
    _currentRegionsOpen.increment()
  }

  final
  def countReadClose(): Unit = {
    newSample()
    _readClosesMetric.recordOp()
    _currentRegionsOpen.decrement()
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    val current = s"\tcurrent_regions_open=${_currentRegionsOpen.sum}\n"
    val reads = s"${_readOpensMetric.report}${_readClosesMetric.report}"
    val writes = s"${_writeOpensMetric.report}${_writeParcelsMetric.report}"
    val compression = s"${_inflateParcelsMetric.report}${_parcelCompressionMetric.report}"
    s"$current$reads$writes$compression"
  }

}
