/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.director.factory

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockAnyVal}
import org.burstsys.tesla.director.{TeslaDirector, TeslaDirectorReporter}
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.pool.TeslaPoolId

/**
 * resource pool for tesla director parts
 */
final
case class TeslaDirectorPool(poolId: TeslaPoolId, partByteSize: TeslaMemorySize)
  extends TeslaPartPool[TeslaDirector] with TeslaDirectorShop {

  override val partQueueSize: Int = 1e4.toInt // 5e5

  @inline override
  def grabDirector(size: TeslaMemorySize): TeslaDirector = {
    markPartGrabbed()
    val part = partQueue.poll match {
      case null =>
        incrementPartsAllocated()
        TeslaDirectorReporter.alloc(partByteSize)
        val block = tesla.block.factory.grabBlock(size)
        TeslaDirector(block.blockBasePtr, block.dataSize).initialize(poolId, size)
      case p =>
        p.reset(size)
    }
    incrementPartsInUse()
    TeslaDirectorReporter.grab()
    part
  }

  @inline override
  def releaseDirector(r: TeslaDirector): Unit = {
    decrementPartsInUse()
    TeslaDirectorReporter.release()
    partQueue add r
  }

  @inline override
  def freePart(part: TeslaDirector): Long = {
    val block = TeslaBlockAnyVal(part.blockBasePtr)
    tesla.block.factory.releaseBlock(block)
    TeslaDirectorReporter.free(partByteSize)
    block.dataSize
  }
}
