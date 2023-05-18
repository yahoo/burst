/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import org.burstsys.vitals.logging._

import scala.language.postfixOps

package object server extends VitalsLogger  {

  final val defaultFabricNetworkServerConfig = FabricNetworkConfig(
    maxConnections = Runtime.getRuntime.availableProcessors * 2
  )

  final val unitFabricNetworkServerConfig = FabricNetworkConfig()

}
