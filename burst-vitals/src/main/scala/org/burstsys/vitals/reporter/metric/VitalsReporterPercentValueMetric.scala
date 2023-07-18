/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import io.opentelemetry.api.metrics.DoubleHistogram

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
  val _valueHist: DoubleHistogram = meter.histogramBuilder(s"${name}_histo")
    .setDescription(s"$name histogram")
    .setUnit("percent")
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def record(value: Double): Unit = {
    _valueHist.record(externalToInternal(value).toDouble, metricAttributes)
  }
}
