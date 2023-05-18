/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.fabric.configuration.{burstFabricNetHostProperty, burstFabricNetPortProperty}

import java.util.concurrent.atomic.AtomicLong
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort, VitalsUrl}

import java.net.InetSocketAddress
import scala.language.postfixOps

package object net extends VitalsLogger {
  final val fabricKryoOutputBufferMaxSize: Long = 10 * MB

  final case
  class FabricNetworkConfig(
    maxConnections: Int = 10,
    netSupervisorAddress: VitalsHostAddress = burstFabricNetHostProperty.get,
    netSupervisorPort: VitalsHostPort = burstFabricNetPortProperty.get
  ) {

    /**
     * A useful URL for the protocol
     *
     * @return
     */
    def netSupervisorUrl: VitalsUrl = s"$netSupervisorAddress:$netSupervisorPort"

    def netSupervisorSocketAddress: InetSocketAddress = new InetSocketAddress(netSupervisorAddress, netSupervisorPort)

    override def toString: String =
      s"""FabricNetworkConfig(
         |  supervisorURL = $netSupervisorUrl,
         |  maxConnections: Int = $maxConnections,
         |)""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////
  // Io Mode
  ///////////////////////////////////////////////////////////////////////////

  /**
   * NOTE THAT TO USE ``EPollIoMode`` you need to install native support
   * https://netty.io/wiki/native-transports.html
   */
  object FabricNetIoMode extends Enumeration {

    type FabricNetIoMode = Value

    val EPollIoMode, NioIoMode, KqIoMode = Value

  }

  type FabricNetMessageId = Long

  private final val requestIdFactory = new AtomicLong

  final def newRequestId: Long = requestIdFactory.getAndIncrement()

}
