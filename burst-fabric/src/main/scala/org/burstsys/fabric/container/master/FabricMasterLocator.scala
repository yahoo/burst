/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.master

import org.burstsys.fabric
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort, VitalsUrl}

/**
  * The way we bind/connect to the Fabric master
  */
trait FabricMasterLocator extends Any {

  /**
    * A useful URL for the protocol
    *
    * @return
    */
  final
  def protocolUrl: VitalsUrl = s"$protocolHost:$protocolPort"

  /**
    * The ip address of the master protocol server
    *
    * @return
    */
  final
  def protocolHost: VitalsHostAddress = {
    fabric.configuration.burstFabricHostProperty.getOrThrow
  }

  /**
    * the ip port of the master protocol serer
    *
    * @return
    */
  final
  def protocolPort: VitalsHostPort = {
    fabric.configuration.burstFabricPortProperty.getOrThrow
  }


}
