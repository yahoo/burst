/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import com.codahale.metrics.Histogram
import com.codahale.metrics.SlidingTimeWindowArrayReservoir
import org.burstsys.vitals.instrument.prettyByteSizeString
import org.burstsys.vitals.reporter

import scala.language.implicitConversions

/**
 * record a changing byte value over a period of time. Report as an exponentially decaying histogram with
 * min, mean, max values
 * <p/>
 * <h3>Metrics:</h3>
 * <ol>
 * <li>min fixed value</li>
 * <li>mean fixed value</li>
 * <li>max fixed value</li>
 * </ol>
 */
trait VitalsReporterByteValueMetric extends VitalsReporterMetric {

  /**
   * record a long value
   *
   * @param value
   */
  def record(value: Long): Unit

}

object VitalsReporterByteValueMetric {

  def apply(name: String): VitalsReporterByteValueMetric =
    VitalsReporterByteValueMetricContext(name: String)

}

private final case
class VitalsReporterByteValueMetricContext(name: String) extends VitalsReporterMetricContext(name) with VitalsReporterByteValueMetric {

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
    s"\t${dName}_bytes_min=$min (${prettyByteSizeString(min)}), ${dName}_bytes_mean=${"%.2f".format(mean)} (${prettyByteSizeString(mean)}), ${dName}_bytes_max=$max (${prettyByteSizeString(max)}) \n"
  }

}
