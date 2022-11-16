/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import com.codahale.metrics.Histogram
import org.burstsys.vitals.reporter.instrument.prettyTimeFromNanos

import scala.language.implicitConversions

/**
 * record a changing ns time value over a period of time. Report as an exponentially decaying histogram with
 * min, mean, max values
 * <p/>
 * <h3>Metrics:</h3>
 * <ol>
 * <li>min ns value</li>
 * <li>mean ns value</li>
 * <li>max ns value</li>
 * </ol>
 */
trait VitalsReporterTimeValueMetric extends VitalsReporterMetric {

  /**
   * record a long ns value
   *
   * @param ns
   */
  def record(ns: Long): Unit

}

object VitalsReporterTimeValueMetric {

  def apply(name: String): VitalsReporterTimeValueMetric =
    VitalsReporterTimeValueMetricContext(name: String)

}

private final case
class VitalsReporterTimeValueMetricContext(name: String) extends VitalsReporterMetricContext(name) with VitalsReporterTimeValueMetric {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _valueHistogram: Histogram = defaultHistogram()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {}

  override def record(value: Long): Unit = {
    newSample()
    _valueHistogram.update(value)
  }

  override def report: String = {
    if (nullData) return ""

    val min = _valueHistogram.getSnapshot.getMin
    val mean = _valueHistogram.getSnapshot.getMean
    val max = _valueHistogram.getSnapshot.getMax
    s"\t${dName}_min=$min (${prettyTimeFromNanos(min)}), ${dName}_mean=$mean (${prettyTimeFromNanos(mean)}), ${dName}_max=$max (${prettyTimeFromNanos(max)}) \n"
  }

}
