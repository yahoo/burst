/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.factory

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.TeslaMemoryOffset
import org.burstsys.tesla.block.TeslaBlockAnyVal
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.zap.cube.{ZapCubeBuilder, ZapCubeContext, ZapCubeReporter}

import scala.language.postfixOps

final
case class ZapCubePool(poolId: TeslaPoolId, partByteSize: TeslaMemoryOffset)
  extends TeslaPartPool[ZapCubeContext] with ZapCubeShop {

  override val partQueueSize: Int = 1e4.toInt // 5e4

  @inline override
  def grabZapCube(builder: ZapCubeBuilder): ZapCubeContext = {
    markPartGrabbed()
    val part = partQueue poll match {
      case null =>
        incrementPartsAllocated()
        ZapCubeReporter.alloc(partByteSize)
        val cube = new ZapCubeContext(tesla.block.factory.grabBlock(builder.totalMemorySize).blockBasePtr, poolId)
        cube.initialize(builder, cube)
      case r =>
        r.initialize(builder, r)
    }
    incrementPartsInUse()
    ZapCubeReporter.grab()
    part
  }

  @inline override
  def releaseZapCube(part: ZapCubeContext): Unit = {
    decrementPartsInUse()
    ZapCubeReporter.release()
    partQueue add part
  }

  @inline override
  def freePart(part: ZapCubeContext): Long = {
    tesla.block.factory.releaseBlock(TeslaBlockAnyVal(part.blockPtr))
    ZapCubeReporter.free(partByteSize)
    partByteSize
  }

}
