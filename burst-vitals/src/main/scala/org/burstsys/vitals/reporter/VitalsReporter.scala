/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.reporter.metric.VitalsReporterMetric

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps
import org.burstsys.vitals.logging._

/**
 * super type for reporters
 */
abstract class VitalsReporter extends AnyRef with VitalsReporterSampler {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  final val _metricSet = new ArrayBuffer[VitalsReporterMetric]

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // public  API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def +=(metric: VitalsReporterMetric): Unit = {
    assert(metric != null)
    _metricSet += metric
  }

  /**
   * execute a ''report'' relevant to the specific reporter and return a human friendly string version
   *
   * @return
   */
  def report: String

  final
  def start(): Unit = {

  }

  override def sample(sampleMs: Long): Unit = {
    _metricSet.foreach{
      m =>
        try {
          m.sample(sampleMs)
        } catch safely {
          case t:Throwable =>
            log error burstStdMsg(s"${m.dName} sample failed $t")
        }
    }
  }

}
