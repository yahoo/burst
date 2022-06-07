/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.{OpsMetricType, VitalsMetricsAgent, VitalsMetricsGauge}

import scala.language.postfixOps

package object server extends VitalsLogger  {

  final val defaultFabricNetworkServerConfig = FabricNetworkConfig(
    isServer = true,
    maxConnections = Runtime.getRuntime.availableProcessors * 2,
    connectionBacklog = 512,
    lowWaterMark = 2 * 65536,
    highWaterMark = 10 * 65536
  )

  final val unitFabricNetworkServerConfig = FabricNetworkConfig(
    isServer = true,
    maxConnections = 10,
    connectionBacklog = 10,
    lowWaterMark = 10,
    highWaterMark = 20
  )

}
