/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import java.net.InetSocketAddress

import org.burstsys.fabric.configuration._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort, VitalsUrl}

/**
 * The way we bind/connect to the fabric net supervisor
 */
trait FabricNetLocator extends Any {

  /**
   * A useful URL for the protocol
   *
   * @return
   */
  final
  def netSupervisorUrl: VitalsUrl = s"$netSupervisorAddress:$netSupervisorPort"

  /**
   * The ip address of the supervisor protocol server
   *
   * @return
   */
  final
  def netSupervisorAddress: VitalsHostAddress = {
    burstFabricNetHostProperty.getOrThrow
  }

  /**
   * the ip port of the supervisor protocol serer
   *
   * @return
   */
  final
  def netSupervisorPort: VitalsHostPort = {
    burstFabricNetPortProperty.getOrThrow
  }

  final
  def netSupervisorSocketAddress: InetSocketAddress = new InetSocketAddress(netSupervisorAddress, netSupervisorPort)

}
