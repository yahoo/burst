/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.factory

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockAnyVal}
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.{TeslaParcel, TeslaParcelAnyVal, TeslaParcelReporter}
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.pool.TeslaPoolId

/**
 * the tesla pool for parcels
 */
final
case class TeslaParcelPool(poolId: TeslaPoolId, partByteSize: TeslaMemorySize)
  extends TeslaPartPool[TeslaParcel] with TeslaParcelShop {

  override val partQueueSize: Int = 1e4.toInt //  5e5

  @inline override
  def grabParcel(size: TeslaMemorySize): TeslaParcel = {
    markPartGrabbed()
    val part = partQueue.poll match {
      case null =>
        incrementPartsAllocated()
        TeslaParcelReporter.alloc(partByteSize)
        val bp = tesla.block.factory.grabBlock(size).blockBasePtr
        val allocatedBuffer = TeslaParcelAnyVal(bp)
        allocatedBuffer.initialize(poolId)
        allocatedBuffer
      case p => p.reset
    }
    incrementPartsInUse()
    TeslaParcelReporter.grab()
    part
  }

  @inline override
  def releaseParcel(r: TeslaParcel): Unit = {
    decrementPartsInUse()
    TeslaParcelReporter.release()
    partQueue add r.blockPtr
  }

  @inline override
  def freePart(part: TeslaParcel): TeslaMemoryPtr = {
    val block = TeslaBlockAnyVal(part.blockBasePtr)
    tesla.block.factory.releaseBlock(block)
    TeslaParcelReporter.free(partByteSize)
    block.dataSize
  }
}
