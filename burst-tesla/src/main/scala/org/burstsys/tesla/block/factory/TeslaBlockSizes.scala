/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.block.factory

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.{block, offheap}
import org.burstsys.tesla.block._
import org.burstsys.vitals
import org.burstsys.vitals.logging.burstStdMsg

/**
  * helper functions for block sizes
  * Because of the memory used in the block header and in headers for parts build on blocks, we have a strange
  * situation where we are always allocate way too much memory
  */
object TeslaBlockSizes {

  final val pageSize: TeslaMemorySize = offheap.pageSize

  /**
   * we preallocate all these sizes for each thread
   * make sure we don't overflow
   * TODO make this just an array of page quanta and build the other structures at init time.
   */
  val blockSizes: Array[TeslaMemorySize] = {
    (for (i <- 0 to 16) yield {
      Math.exp(i).toLong*pageSize
    }).filter(_ < Int.MaxValue).map(_.toInt).toArray
  }

  @inline final
  def findBlockSize(byteSize: TeslaMemorySize): TeslaMemorySize = blockSize(byteSize + SizeofBlockHeader)

  // TODO SizeofBlockHeader accounted for twice

  /**
    * convert any size to one of our block sizes
    *
    * @param byteSize return the block that is the closest match
    * @return size
    */
  @inline private[block]
  def blockSize(byteSize: TeslaMemorySize): TeslaMemorySize = {

    // here we allow for the size of the memory block header
    val size = {
      val desired = byteSize + SizeofBlockHeader
      var chosen = 0
      var i = 0
      while (chosen == 0 && i < blockSizes.length) {
        if (desired < blockSizes(i))
          chosen = blockSizes(i)
        i += 1
      }
      if (chosen == 0) {
        throw new RuntimeException(s"TESLA_BAD_BLOCK_SIZE_REQUEST: request for unsupported block size: $byteSize (${ vitals.reporter.instrument.prettyByteSizeString(byteSize)})")
      }
      chosen
    }
    if (block.log.isTraceEnabled)
      block.log trace burstStdMsg(s"for request size $byteSize, allocated blocksize $size")
    size
  }
}
