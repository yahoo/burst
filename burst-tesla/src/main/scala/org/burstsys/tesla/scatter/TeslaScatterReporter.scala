/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter

import java.util.concurrent.atomic.LongAdder

import org.burstsys.tesla.scatter.slot.TeslaScatterSlot
import org.burstsys.vitals.instrument._
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.{VitalsReporterByteOpMetric, VitalsReporterFloatValueMetric}
import org.burstsys.vitals.stats.stdSkewStat
import org.burstsys.vitals.uid.VitalsUid

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

/**
 * a generic report for scatter gather performance/problems
 */
private[tesla]
object TeslaScatterReporter extends VitalsReporter {

  final val dName: String = "tesla-scatter"

  final val warnSkew = 2.0

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _scatterOpenRate = VitalsReporterByteOpMetric("tesla_scatter_open")
  this += _scatterOpenRate

  private[this]
  val _scatterSlotOpenRate = VitalsReporterByteOpMetric("tesla_scatter_slot_open")
  this += _scatterSlotOpenRate

  private[this]
  val _scatterSlotTardyRate = VitalsReporterByteOpMetric("tesla_scatter_slot_tardy")
  this += _scatterSlotTardyRate

  private[this]
  val _scatterSlotSuccessRate = VitalsReporterByteOpMetric("tesla_scatter_slot_success")
  this += _scatterSlotSuccessRate

  private[this]
  val _scatterSlotFailRate = VitalsReporterByteOpMetric("tesla_scatter_slot_fail")
  this += _scatterSlotFailRate

  private[this]
  val _scatterSkew = VitalsReporterFloatValueMetric("tesla_scatter_skew")
  this +=  _scatterSkew

  private[this]
  val _openScatters = new LongAdder


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final def scatterOpen(): Unit = {
    newSample()
    _scatterOpenRate.recordOp()
    _openScatters.increment()
  }

  final val hostCheckCount = 5

  final def scatterClose(guid: VitalsUid, successes: ArrayBuffer[TeslaScatterSlot], failures: ArrayBuffer[TeslaScatterSlot]): Unit = {
    newSample()
    _openScatters.decrement()
    if(failures.isEmpty) {
      val skew = calcSkew(successes)
      _scatterSkew.record(skew)
      if (skew > warnSkew) {
        val fastHosts= successes.sortBy(_.elapsedNanos).take(hostCheckCount).map {
          slot => s"tesla_fast_host=${slot.destinationHostName} (${prettyTimeFromNanos(slot.elapsedNanos)})"
        }.mkString("\n\t", ",\n\t", "")
        val slowHosts = successes.sortBy(_.elapsedNanos).reverse.take(hostCheckCount).map {
          slot => s"tesla_slow_host=${slot.destinationHostName} (${prettyTimeFromNanos(slot.elapsedNanos)})"
        }.mkString("\n\t", ",\n\t", "")
        log warn s"guid=$guid, wave_skew==$skew \n\tfastest $hostCheckCount hosts: $fastHosts \n\tslowest $hostCheckCount hosts: $slowHosts"
      }
    }
  }

  final def scatterSlotOpen(): Unit = {
    newSample()
    _scatterSlotOpenRate.recordOp()
  }

  final def scatterSlotTardy(): Unit = {
    newSample()
    _scatterSlotTardyRate.recordOp()
  }

  final def scatterSlotFail(): Unit = {
    newSample()
    _scatterSlotFailRate.recordOp()
  }

  final def scatterSlotSuccess(): Unit = {
    newSample()
    _scatterSlotSuccessRate.recordOp()
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def report: String = {
    if (nullData) return ""
    s"\ttesla_open_scatters=${_openScatters.longValue} (${prettySizeString(_openScatters.longValue)}))${_scatterOpenRate.report}${_scatterSlotOpenRate.report}${_scatterSlotTardyRate.report}${_scatterSlotSuccessRate.report}${_scatterSlotFailRate.report}${_scatterSkew.report}"
  }

  private
  def calcSkew(successes: ArrayBuffer[TeslaScatterSlot]): Double = {
    var minValue = Long.MaxValue
    var maxValue = Long.MinValue
    var index = 0
    while (index < successes.length) {
      val slot = successes(index)
      minValue = math.min(minValue, slot.elapsedNanos)
      maxValue = math.max(minValue, slot.elapsedNanos)
      index += 1
    }
    stdSkewStat(minValue, maxValue)
  }

}
