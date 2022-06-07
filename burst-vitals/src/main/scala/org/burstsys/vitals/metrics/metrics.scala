/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties._

/**
  * TODO
 * @deprecated going way for OPEN SOURCE
  */
package object metrics extends VitalsLogger {

  type VitalsMetricsPackageName = String

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // MetricType
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  sealed trait MetricType

  object DevMetricType extends MetricType

  object OpsMetricType extends MetricType

}
