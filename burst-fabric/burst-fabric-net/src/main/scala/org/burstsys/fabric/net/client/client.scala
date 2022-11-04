/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.{OpsMetricType, VitalsMetricsAgent, VitalsMetricsGauge}

import scala.concurrent.duration._
import scala.language.postfixOps

package object client extends VitalsLogger {

  final val clientConfig = FabricNetworkConfig(
    isServer = false
  )

}
