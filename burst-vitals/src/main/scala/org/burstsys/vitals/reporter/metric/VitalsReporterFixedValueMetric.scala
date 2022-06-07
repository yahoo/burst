/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import com.codahale.metrics.{ExponentiallyDecayingReservoir, Histogram}
import org.burstsys.vitals.instrument.prettySizeString

import scala.language.implicitConversions

/**
 * record a changing value over a period of time. Report as an exponentially decaying histogram with
 * min, mean, max values
 * <p/>
 * <h3>Metrics:</h3>
 * <ol>
 * <li>min fixed value</li>
 * <li>mean fixed value</li>
 * <li>max fixed value</li>
 * </ol>
 */
trait VitalsReporterFixedValueMetric extends VitalsReporterMetric {

  /**
   * record a long value
   *
   * @param value
   */
  def record(value: Long): Unit

}

object VitalsReporterFixedValueMetric {

  def apply(name: String): VitalsReporterFixedValueMetric =
    VitalsReporterFixedValueMetricContext(name: String)

}

private final case
class VitalsReporterFixedValueMetricContext(name: String) extends VitalsReporterMetricContext(name) with VitalsReporterFixedValueMetric {

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
    s"\t${dName}_min=$min (${prettySizeString(min)}), ${dName}_mean=$mean (${prettySizeString(mean)}), ${dName}_max=$max (${prettySizeString(max)}) \n"
  }

}
