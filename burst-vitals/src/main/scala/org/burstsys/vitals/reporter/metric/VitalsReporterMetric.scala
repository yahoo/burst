/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter.metric

import org.burstsys.vitals.reporter.VitalsReporterSampler

/**
 * base trait for all reporter metrics
 */
trait VitalsReporterMetric extends AnyRef with VitalsReporterSampler {

  /**
   * implemented by concrete subtypes to generate the report
   */
  def report: String

}

abstract
class VitalsReporterMetricContext(val dName: String) extends VitalsReporterMetric {


}

