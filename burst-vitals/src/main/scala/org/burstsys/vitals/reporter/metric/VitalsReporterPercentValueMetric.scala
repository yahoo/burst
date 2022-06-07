/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import com.codahale.metrics.{ExponentiallyDecayingReservoir, Histogram}
import org.burstsys.vitals.instrument.prettyFloatNumber

import scala.language.implicitConversions

/**
 * a reasonably accurate (at least two decimal pts precision) percentage value (0.00 to 100.00)
 * Report as an exponentially decaying histogram with min, mean, max values
 * <p/>
 * <h3>Metrics:</h3>
 * <ol>
 * <li>min percentage</li>
 * <li>mean percentage</li>
 * <li>max oercentage</li>
 * </ol>
 */
trait VitalsReporterPercentValueMetric extends VitalsReporterMetric {

  /**
   * record a percent float value
   *
   * @param value
   */
  def record(value: Double): Unit

}

object VitalsReporterPercentValueMetric {

  def apply(name: String): VitalsReporterPercentValueMetric =
    VitalsReporterPercentValueMetricContext(name: String)

}

private final case
class VitalsReporterPercentValueMetricContext(name: String) extends VitalsReporterMetricContext(name) with VitalsReporterPercentValueMetric {

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
    s"\t${dName}_min=${prettyFloatNumber(min)} % ${dName}_mean=${prettyFloatNumber(mean)} % ${dName}_max=${prettyFloatNumber(max)} % \n"
  }

}
