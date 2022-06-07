/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.worker.cache

import org.burstsys.vitals.instrument._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.time._

package object bench extends VitalsLogger {

  final case
  class FabricCacheBenchmark(targetBytesSize: Long, actualBytesSize: Long, elapsedNs: Long) {

    def report: String = {
      val byteSize = actualBytesSize / nsToSec(elapsedNs)
      s"""|
          |FabricCacheBenchmark(
          |   inflated=true (zero inflation time)
          |   cache_benchmark_byte_size=$actualBytesSize ( ${prettyByteSizeString(actualBytesSize)} )
          |   cache_benchmark_elapsed_ns=${elapsedNs} ( ${prettyTimeFromNanos(elapsedNs)} )
          |   cache_benchmark_bytes_sec=$byteSize ( ${prettyByteSizeString(byteSize)}/s )
          |)""".stripMargin
    }
  }


}
