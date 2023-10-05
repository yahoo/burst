/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.region

import io.opentelemetry.api.metrics.LongUpDownCounter
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterPercentValueMetric, VitalsReporterUnitOpMetric}

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
  val _readOpensMetric = VitalsReporterUnitOpMetric("region_read_opens")

  private[this]
  val _readClosesMetric = VitalsReporterUnitOpMetric("region_read_closes")

  private[this]
  val _writeOpensMetric = VitalsReporterUnitOpMetric("region_write_opens")

  private[this]
  val _writeClosesMetric = VitalsReporterUnitOpMetric("region_write_closes")

  private[this]
  val _writeParcelsMetric = VitalsReporterUnitOpMetric("region_parcel_writes")

  private[this]
  val _inflateParcelsMetric = VitalsReporterUnitOpMetric("region_parcel_inflate")

  private[this]
  val _parcelCompressionMetric = VitalsReporterPercentValueMetric("region_parcel_compression")

  private[this]
  val _currentRegionsOpenCounter: LongUpDownCounter = metric.upDownCounter(s"region_open_counter")
    .setDescription(s"regions open")
    .setUnit("regions")
    .build()


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * count a region write open
   */
  final
  def countWriteOpen(): Unit = {
    _writeOpensMetric.recordOp()
  }

  final
  def countWriteClose(): Unit = {
    _writeClosesMetric.recordOp()
  }

  /**
   * count a parcel write with its byte size
   */
  final
  def countParcelWrite(byteSize: Long): Unit = {
    _writeParcelsMetric.recordOpWithSize(byteSize)
  }

  final
  def recordParcelInflate(ns:Long, deflatedSize: Long, inflatedSize:Long): Unit = {
    _parcelCompressionMetric.record(inflatedSize.toDouble/deflatedSize.toDouble)
    _inflateParcelsMetric.recordOpWithTimeAndSize(ns, inflatedSize)
  }

  /**
   * count a region read open with its byte size
   */
  final
  def countReadOpen(byteSize: Long): Unit = {
    _readOpensMetric.recordOpWithSize(byteSize)
    _currentRegionsOpenCounter.add(1)
  }

  final
  def countReadClose(): Unit = {
    _readClosesMetric.recordOp()
    _currentRegionsOpenCounter.add(-1)
  }
}
