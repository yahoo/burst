/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter

import io.opentelemetry.api.metrics.LongUpDownCounter
import org.burstsys.tesla.scatter.slot.TeslaScatterSlot
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.reporter.metric.{VitalsReporterFloatValueMetric, VitalsReporterUnitOpMetric}
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

  private final val warnSkew = 2.0

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _scatterOpenRate = VitalsReporterUnitOpMetric(s"${dName}_open")

  private[this]
  val _scatterSlotOpenRate = VitalsReporterUnitOpMetric(s"${dName}_slot_open")

  private[this]
  val _scatterSlotTardyRate = VitalsReporterUnitOpMetric(s"${dName}_slot_tardy")

  private[this]
  val _scatterSlotSuccessRate = VitalsReporterUnitOpMetric(s"${dName}_slot_success")

  private[this]
  val _scatterSlotFailRate = VitalsReporterUnitOpMetric(s"${dName}_slot_fail")

  private[this]
  val _scatterSkew = VitalsReporterFloatValueMetric(s"${dName}_skew")

  private[this]
  val _openScattersCounter: LongUpDownCounter = metric.meter.upDownCounterBuilder(s"${dName}_open_scatters_counter")
    .setDescription(s"open scatters in use")
    .setUnit("scatters")
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final def scatterOpen(): Unit = {
    _scatterOpenRate.recordOp()
    _openScattersCounter.add(1)
  }

  private final val hostCheckCount = 5

  final def scatterClose(guid: VitalsUid, successes: ArrayBuffer[TeslaScatterSlot], failures: ArrayBuffer[TeslaScatterSlot]): Unit = {
    _openScattersCounter.add(-1)
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
    _scatterSlotOpenRate.recordOp()
  }

  final def scatterSlotTardy(): Unit = {
    _scatterSlotTardyRate.recordOp()
  }

  final def scatterSlotFail(): Unit = {
    _scatterSlotFailRate.recordOp()
  }

  final def scatterSlotSuccess(): Unit = {
    _scatterSlotSuccessRate.recordOp()
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
