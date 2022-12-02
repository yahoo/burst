/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.tesla.TeslaTypes._
import org.burstsys.vitals.host
import org.burstsys.vitals.reporter.VitalsByteQuantReporter
import org.burstsys.vitals.stats._

import scala.language.postfixOps

package object buffer {

  object TeslaBufferReporter extends VitalsByteQuantReporter("tesla","buffer")

  final val partName: String = "buffer"

  ////////////////////////////////////////////////////////
  // BLOB stuff - yeah it should not be in this package...
  ////////////////////////////////////////////////////////

  /**
    * This is a pre zap blob normal brio dictionary blob
    */
  final val BlobEncodingVersion1 = 1

  /**
    * this is a zap blob
    */
  final val BlobEncodingVersion2 = 2

  final val BlobHeaderSize: TeslaMemorySize = SizeOfInteger + SizeOfInteger

  ////////////////////////////////////////////////////////
  // old school writer allocation...
  ////////////////////////////////////////////////////////

  final val TeslaWriterMaxSize: ByteSize = {
    val memory: ByteSize = host.osTotalPhysMemory
    val size: ByteSize = memory match {
      case s if s.between(0, 8 gb) => 3 mb
      case s if s.between(8 gb, 16 gb) => 3 mb
      case s if s.between(16 gb, 32 gb) => 5 mb
      case s if s.between(32 gb, 64 gb) => 5 mb
      case s if s.between(64 gb, 128 gb) => 8 mb
      case s if s.between(128 gb, 256 gb) => 8 mb
      case _ => 8 mb
    }
    //    log info burstStdMsg(s"avail=${prettyByteSizeString(memory.inB)}, burst_writer_size=${prettyByteSizeString(size.inB)}")
    size
  }

  ////////////////////////////////////////////////////////

  final val bufferDebug = false


}
