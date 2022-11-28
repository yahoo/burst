/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.block.factory

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.offheap
import org.burstsys.tesla.block._
import org.burstsys.vitals

/**
  * helper functions for block sizes
  * Because of the memory used in the block header and in headers for parts build on blocks, we have a strange
  * situation where we are always allocate way too much memory
  */
object TeslaBlockSizes {

  final val pageSize: TeslaMemorySize = offheap.pageSize

  /**
    * we preallocate all these sizes for each thread
    * TODO make this just an array of page quanta and build the other structures at init time.
    */
  val blockSizes: Array[TeslaMemorySize] = Array[TeslaMemorySize](
    pageSize, 2 * pageSize, 4 * pageSize, 8 * pageSize, 16 * pageSize, 32 * pageSize,
    64 * pageSize, 128 * pageSize, 256 * pageSize, 1024 * pageSize, 2048 * pageSize,
    4096 * pageSize, 8192 * pageSize, 16384 * pageSize, 32768 * pageSize, 65536 * pageSize, 131072 * pageSize
  )

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

    // TODO optimize this - use bit shifting to align to next highest page size somehow
    @inline
    def between(value: TeslaMemorySize, low: TeslaMemorySize, high: TeslaMemorySize): Boolean =
      value >= low && value < high

    // here we allow for the size of the memory block header
    val size = byteSize + SizeofBlockHeader match {
      // TODO SizeofBlockHeader accounted for twice
      case s if between(s, 0, pageSize) => pageSize
      case s if between(s, pageSize, 2 * pageSize) => 2 * pageSize
      case s if between(s, 2 * pageSize, 4 * pageSize) => 4 * pageSize
      case s if between(s, 4 * pageSize, 8 * pageSize) => 8 * pageSize
      case s if between(s, 8 * pageSize, 16 * pageSize) => 16 * pageSize
      case s if between(s, 16 * pageSize, 32 * pageSize) => 32 * pageSize
      case s if between(s, 32 * pageSize, 64 * pageSize) => 64 * pageSize
      case s if between(s, 64 * pageSize, 128 * pageSize) => 128 * pageSize
      case s if between(s, 128 * pageSize, 256 * pageSize) => 256 * pageSize
      case s if between(s, 256 * pageSize, 1024 * pageSize) => 1024 * pageSize
      case s if between(s, 1024 * pageSize, 2048 * pageSize) => 2048 * pageSize
      case s if between(s, 2048 * pageSize, 4096 * pageSize) => 4096 * pageSize
      case s if between(s, 4096 * pageSize, 8192 * pageSize) => 8192 * pageSize
      case s if between(s, 8192 * pageSize, 16384 * pageSize) => 16384 * pageSize
      case s if between(s, 16384 * pageSize, 32768 * pageSize) => 32768 * pageSize
      case s if between(s, 32768 * pageSize, 65536 * pageSize) => 65536 * pageSize
      case s if between(s, 65536 * pageSize, 131072 * pageSize) => 131072 * pageSize
      case _ =>
        throw new RuntimeException(s"TESLA_BAD_BLOCK_SIZE_REQUEST: request for unsupported block size: $byteSize (${ vitals.reporter.instrument.prettyByteSizeString(byteSize)})")
    }
    //log debug burstStdMsg(s"for request size $byteSize, allocated blocksize $size")
    size
  }
}
