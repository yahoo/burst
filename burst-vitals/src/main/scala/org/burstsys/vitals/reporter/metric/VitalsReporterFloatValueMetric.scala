/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import com.codahale.metrics.Histogram
import org.burstsys.vitals.reporter.instrument.prettyFloatNumber

import scala.language.implicitConversions

/**
 * a reasonably accurate (at least two decimal pts precision) float value (0.00 to 100.00)
 * Report as an exponentially decaying histogram with min, mean, max values
 * <p/>
 * <h3>Metrics:</h3>
 * <ol>
 * <li>min percentage</li>
 * <li>mean percentage</li>
 * <li>max oercentage</li>
 * </ol>
 */
trait VitalsReporterFloatValueMetric extends VitalsReporterMetric {

  /**
   * record a long value
   *
   * @param value
   */
  def record(value: Double): Unit

}

object VitalsReporterFloatValueMetric {

  def apply(name: String): VitalsReporterFloatValueMetric =
    VitalsReporterFloatValueMetricContext(name: String)

}

private final case
class VitalsReporterFloatValueMetricContext(name: String) extends VitalsReporterMetricContext(name) with VitalsReporterFloatValueMetric {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _valueHistogram: Histogram = defaultHistogram()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {}

  override def record(value: Double): Unit = {
    newSample()
    _valueHistogram.update(externalToInternal(value))
  }

  override def report: String = {
    if (nullData) return ""

    val min = internalToExternal(_valueHistogram.getSnapshot.getMin)
    val mean = internalToExternal(_valueHistogram.getSnapshot.getMean)
    val max = internalToExternal(_valueHistogram.getSnapshot.getMax)
    s"\t${dName}_min=$min (${prettyFloatNumber(min)}), ${dName}_mean=$mean (${prettyFloatNumber(mean)}), ${dName}_max=$max (${prettyFloatNumber(max)}) \n"
  }

}
