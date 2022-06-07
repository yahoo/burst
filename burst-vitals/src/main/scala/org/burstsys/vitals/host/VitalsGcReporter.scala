/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.host

import java.lang.management.ManagementFactory
import org.burstsys.vitals.instrument._
import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterPercentValueMetric

import scala.collection.JavaConverters._
import scala.language.postfixOps

/**
 * helper types/functions for Host/Process/Os state
 */
private[vitals]
object VitalsGcReporter extends VitalsReporter {

  final val dName: String = "vitals-gc"

  /////////////////////////////////////////////////////////////////////////////////////////////
  // GC Churn....
  /////////////////////////////////////////////////////////////////////////////////////////////

  private var lastGcMeasureEpoch: Long = System.currentTimeMillis
  private var lastGcTime: Long = 0L
  private var gcChurn: Double = 0L

  private
  def calculateGcChurn(): Unit = {
    val currentGcTime = gcCollectionTime
    val elapsedTimeMs = System.currentTimeMillis - lastGcMeasureEpoch
    val gcOverElapsedTime = currentGcTime - lastGcTime
    gcChurn = gcOverElapsedTime.toDouble / elapsedTimeMs.toDouble
    lastGcTime = currentGcTime
    lastGcMeasureEpoch = System.currentTimeMillis
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  final val gcChurnMetric = VitalsReporterPercentValueMetric("proc_gc_churn")

  this +=gcChurnMetric

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    newSample()
    calculateGcChurn()
    gcChurnMetric.record(gcChurn)
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def report: String = {
    if (nullData) return ""
    val list = (for (b <- ManagementFactory.getGarbageCollectorMXBeans.asScala)
      yield
        s"\tproc_gc_${b.getName.replaceAll(" ", "_").toLowerCase}_count=${b.getCollectionCount}, proc_gc_${b.getName.replaceAll(" ", "_").toLowerCase}_time=${b.getCollectionTime} ms (${prettyTimeFromMillis(b.getCollectionTime)}) "
      ).mkString(" \n")

    val main = s"\tproc_gc_collect_time=$gcCollectionTime ms (${prettyTimeFromMillis(gcCollectionTime)}), proc_gc_collect_count=$gcCollectionCount \n"

    s"${gcChurnMetric.report}$main$list \n"
  }

}
