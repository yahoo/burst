/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.nexus.client.NexusClient
import org.burstsys.vitals.instrument.{prettyByteSizeString, prettyTimeFromNanos}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostAddress
import org.burstsys.vitals.time.nsToSec

import scala.collection.JavaConverters._

package object bench extends VitalsLogger {

  val loopbackModeProperty = "loopbackMode"

  val loopbackByteSizePropery = "loopbackByteSize"

  val loopback = "LOOPBACK"

  private[bench] final
  val _hostCache = new ConcurrentHashMap[VitalsHostAddress, NexusClient].asScala

  final case
  class NexusNetBenchmark(deflatedByteTally: Long, inflatedByteTally: Long, elapsedNs: Long) {

    def report: String = {
      val deflatedByteRate = deflatedByteTally / nsToSec(elapsedNs)
      val inflatedByteRate = inflatedByteTally / nsToSec(elapsedNs)
      s"""|
          |NexusNetBenchmark(
          |   net_benchmark_compression_ratio=${deflatedByteTally/inflatedByteTally}
          |   net_benchmark_inflated_byte_size=$deflatedByteTally ( ${prettyByteSizeString(deflatedByteTally)} )
          |   net_benchmark_inflated_bytes_sec=$deflatedByteRate ( ${prettyByteSizeString(deflatedByteRate)}/s )
          |   net_benchmark_inflated_byte_size=$inflatedByteTally ( ${prettyByteSizeString(inflatedByteTally)} )
          |   net_benchmark_inflated_bytes_sec=$inflatedByteRate ( ${prettyByteSizeString(inflatedByteRate)}/s )
          |)""".stripMargin
    }
  }

}
