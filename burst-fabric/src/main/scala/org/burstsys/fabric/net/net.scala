/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import java.util.concurrent.atomic.AtomicLong

import org.burstsys.vitals.instrument._
import org.burstsys.vitals.logging._

import scala.language.postfixOps

package object net extends VitalsLogger {

  final val debugFabNet = false

  final val fabricKryoOutputBufferMaxSize: Long = 10 * MB

  final case
  class FabricNetworkConfig(
                             isServer: Boolean,
                             maxConnections: Int = 10,
                             connectionBacklog: Int = 10,
                             lowWaterMark: Int = 10,
                             highWaterMark: Int = 20
                           ) {

    def printRole: String = if (isServer) "role=server" else "role=client"

    override def toString: String =
      s"""FabricNetworkConfig(
         |  isServer: Boolean  = $isServer,
         |  maxConnections: Int = $maxConnections,
         |  connectionBacklog:Int = $connectionBacklog,
         |  lowWaterMark: Int = $lowWaterMark,
         |  highWaterMark: Int = $highWaterMark
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
