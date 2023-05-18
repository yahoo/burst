/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus

import org.burstsys.vitals.logging._

import scala.concurrent.duration._
import scala.language.postfixOps

package object server extends VitalsLogger  {

  final val serverConfig = NexusConfig(
    isServer = true,
    maxBytesBetweenFlush = 50e3.toLong,
    maxPacketsBetweenFlush = 50,
    maxNsBetweenFlush = (100 millis).toNanos,
    lowWaterMark = 2 * 65536,
    highWaterMark = 10 * 65536
  )

}
