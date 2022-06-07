/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import java.net.InetSocketAddress

import org.burstsys.fabric.configuration._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort, VitalsUrl}

/**
 * The way we bind/connect to the fabric net master
 */
trait FabricNetLocator extends Any {

  /**
   * A useful URL for the protocol
   *
   * @return
   */
  final
  def netMasterUrl: VitalsUrl = s"$netMasterAddress:$netMasterPort"

  /**
   * The ip address of the master protocol server
   *
   * @return
   */
  final
  def netMasterAddress: VitalsHostAddress = {
    burstFabricNetHostProperty.getOrThrow
  }

  /**
   * the ip port of the master protocol serer
   *
   * @return
   */
  final
  def netMasterPort: VitalsHostPort = {
    burstFabricNetPortProperty.getOrThrow
  }

  final
  def netMasterSocketAddress: InetSocketAddress = new InetSocketAddress(netMasterAddress, netMasterPort)

}
