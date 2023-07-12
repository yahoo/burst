/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import io.opentelemetry.api.metrics.LongHistogram

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
  val _valueHist: LongHistogram = meter.histogramBuilder(s"${name}_histo").ofLongs()
    .setDescription(s"$name histogram")
    .setUnit("units")
    .build()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def record(value: Long): Unit = {
    _valueHist.record(value, metricAttributes)
  }
}
