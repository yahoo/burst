/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.factory

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockAnyVal}
import org.burstsys.tesla.part
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.zap.cube2.{ZapCube2, ZapCube2AnyVal, ZapCube2Builder}

import scala.language.postfixOps

/**
 */
final
case class ZapCube2Pool(poolId: TeslaPoolId, partByteSize: TeslaMemoryOffset)
  extends TeslaPartPool[ZapCube2] with ZapCube2Shop {

  override val partQueueSize: Int = 5e4.toInt // 5e4

  @inline override
  def grabCube2(builder: ZapCube2Builder, startSize: TeslaMemorySize = part.teslaBuilderUseDefaultSize): ZapCube2 = {
    markPartGrabbed()
    val part = partQueue poll match {
      case null =>
        incrementPartsAllocated()
        val size = builder.chooseSize(startSize)
        val cube = ZapCube2AnyVal(tesla.block.factory.grabBlock(size).blockBasePtr)
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
  def releaseCube2(d: ZapCube2): Unit = {
    decrementPartsInUse()
    partQueue add d
  }

  @inline override
  def freePart(part: ZapCube2): Long = {
    tesla.block.factory.releaseBlock(TeslaBlockAnyVal(part.blockPtr))
    partByteSize
  }

}
