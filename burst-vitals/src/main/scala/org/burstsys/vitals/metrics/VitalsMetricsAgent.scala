/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.metrics

import com.codahale.metrics.MetricRegistry

/**
  * TODO
 * @deprecated going way for OPEN SOURCE
  */
trait VitalsMetricsAgent {

  /**
    * TODO
    *
    * @return
    */
  implicit def registry: MetricRegistry = VitalsMetricsRegistry.registry

  /**
    * TODO
    */
  implicit lazy val metricsPackageName: VitalsMetricsPackageName = s"${
    getClass.getPackage.getName.stripPrefix(
      "org.burstsys."
    )
  }"
}
