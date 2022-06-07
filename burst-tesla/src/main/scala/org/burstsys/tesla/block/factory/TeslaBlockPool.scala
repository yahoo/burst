/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.block.factory

import java.lang

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockAnyVal, TeslaBlockReporter}
import org.burstsys.tesla.offheap
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.pool.TeslaPoolId

import scala.language.postfixOps

/**
 * A block pool is a single allocation of a large number of contiguous fixed size blocks.
 * It is meant to be used by a single thread. These are allocated in page size quantums
 */
final case class TeslaBlockPool(poolId: TeslaPoolId, partByteSize: TeslaMemorySize)
  extends TeslaPartPool[java.lang.Long] with TeslaBlockShop {

  override val partQueueSize: Int = 5e5.toInt // 5e5

  @inline override
  def grabBlock(byteSize: TeslaMemorySize): TeslaBlock = {
    markPartGrabbed()
    val part = partQueue poll match {
      case null =>
        incrementPartsAllocated()
        TeslaBlockReporter.alloc(partByteSize)
        TeslaBlockAnyVal(offheap.allocateMemory(partByteSize)).initialize(partByteSize, poolId)
      case m => TeslaBlockAnyVal(m)
    }
    incrementPartsInUse()
    TeslaBlockReporter.grab()
    part
  }

  @inline override
  def releaseBlock(block: TeslaBlock): Unit = {
    decrementPartsInUse()
    TeslaBlockReporter.release()
    partQueue add block.blockBasePtr
  }

  @inline override
  def freePart(part: lang.Long): TeslaMemoryPtr = {
    offheap.freeMemory(part)
    TeslaBlockReporter.free(partByteSize)
    partByteSize
  }

}
