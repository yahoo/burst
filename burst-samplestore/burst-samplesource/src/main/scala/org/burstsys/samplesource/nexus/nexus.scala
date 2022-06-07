/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource

import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.{OpsMetricType, VitalsMetricsAgent, VitalsMetricsMeter, VitalsMetricsSlidingWindowHistogram}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort}

package object nexus extends VitalsLogger {

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // Types
  //////////////////////////////////////////////////////////////////////////////////////////////////////
  type SampleSourceNexusServerInfo = (VitalsHostAddress, VitalsHostName, VitalsHostPort)


}
