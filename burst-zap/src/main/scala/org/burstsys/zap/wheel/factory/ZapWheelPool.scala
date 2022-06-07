/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.wheel.factory

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr}
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockAnyVal}
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.zap.wheel.{ZapWheel, ZapWheelAnyVal, ZapWheelBuilder}

import scala.language.postfixOps

final case
class ZapWheelPool(poolId: TeslaPoolId, partByteSize: TeslaMemoryOffset)
  extends TeslaPartPool[ZapWheel] with ZapWheelShop {

  override val partQueueSize: Int = 5e5.toInt // 5e3

  @inline override
  def grabZapWheel(schema: ZapWheelBuilder): ZapWheel = {
    markPartGrabbed()
    val part = partQueue poll match {
      case null =>
        incrementPartsAllocated()
        ZapWheelAnyVal(tesla.block.factory.grabBlock(schema.defaultStartSize).blockBasePtr).initialize(poolId)
      case r =>
        r.reset
    }
    incrementPartsInUse()
    part
  }

  @inline override
  def releaseZapWheel(wheel: ZapWheel): Unit = {
    decrementPartsInUse()
    partQueue add wheel
  }

  /**
   *
   * @param part
   * @return
   */
  @inline override
  def freePart(part: ZapWheel): TeslaMemoryPtr = {
    val block = TeslaBlockAnyVal(part.blockBasePtr)
    tesla.block.factory.releaseBlock(block)
    partByteSize
  }
}
