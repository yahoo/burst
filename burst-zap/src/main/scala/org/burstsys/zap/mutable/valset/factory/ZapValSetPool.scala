/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valset.factory

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockAnyVal}
import org.burstsys.tesla.part.{TeslaPartPool, teslaBuilderUseDefaultSize}
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.zap.mutable.valset.{ZapValSet, ZapValSetContext, ZapValSetBuilder}

import scala.language.postfixOps

/**
 * TODO - should we be storing a LONG memory pointer instead of the AnyVal object?
 */
final
case class ZapValSetPool(poolId: TeslaPoolId, partByteSize: TeslaMemoryOffset)
  extends TeslaPartPool[ZapValSet] with ZapValSetShop {

  override val partQueueSize: Int = 5e4.toInt // 5e4

  @inline override
  def grabValSet(builder: ZapValSetBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValSet = {
    markPartGrabbed()
    val part = partQueue poll match {
      case null =>
        incrementPartsAllocated()
        val size = builder.chooseSize(startSize)
        val cube = ZapValSetContext(tesla.block.factory.grabBlock(size).blockBasePtr)
        cube.initialize(pId = poolId, builder)
        cube
      case cube =>
        cube.reset(builder)
        cube
    }
    incrementPartsInUse()
    part
  }

  @inline override
  def releaseValSet(d: ZapValSet): Unit = {
    decrementPartsInUse()
    partQueue add d
  }

  @inline override
  def freePart(part: ZapValSet): Long = {
    tesla.block.factory.releaseBlock(TeslaBlockAnyVal(part.blockPtr))
    partByteSize
  }

}
