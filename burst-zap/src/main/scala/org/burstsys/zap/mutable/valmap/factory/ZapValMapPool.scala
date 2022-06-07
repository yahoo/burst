/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valmap.factory

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockAnyVal}
import org.burstsys.tesla.part.{TeslaPartPool, teslaBuilderUseDefaultSize}
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.zap.mutable.valmap.{ZapValMap, ZapValMapAnyVal, ZapValMapBuilder}

import scala.language.postfixOps

/**
 * TODO - should we be storing a LONG memory pointer instead of the AnyVal object?
 */
final
case class ZapValMapPool(poolId: TeslaPoolId, partByteSize: TeslaMemoryOffset)
  extends TeslaPartPool[ZapValMap] with ZapValMapShop {

  override val partQueueSize: Int = 5e4.toInt // 5e4

  @inline override
  def grabValMap(builder: ZapValMapBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValMap = {
    markPartGrabbed()
    val part = partQueue poll match {
      case null =>
        incrementPartsAllocated()
        val size = builder.chooseSize(startSize)
        val instance = ZapValMapAnyVal(tesla.block.factory.grabBlock(size).blockBasePtr)
        instance.initialize(pId = poolId, builder)
        instance
      case instance =>
        instance.reset(builder)
        instance
    }
    incrementPartsInUse()
    part
  }

  @inline override
  def releaseValMap(d: ZapValMap): Unit = {
    decrementPartsInUse()
    partQueue add d
  }

  @inline override
  def freePart(part: ZapValMap): Long = {
    tesla.block.factory.releaseBlock(TeslaBlockAnyVal(part.blockPtr))
    partByteSize
  }

}
