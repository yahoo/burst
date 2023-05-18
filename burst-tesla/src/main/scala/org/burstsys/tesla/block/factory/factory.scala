/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.block

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

package object factory extends TeslaPartFactory[java.lang.Long, TeslaBlockPool]
  with TeslaBlockShop with VitalsLogger {

  final
  val TeslaNullMemoryBlock:TeslaBlock = TeslaBlockAnyVal()

  startPartTender

  @inline final override
  def grabBlock(byteSize: TeslaMemoryOffset): TeslaBlock = {
    val bs = TeslaBlockSizes findBlockSize byteSize
    perThreadPartPool(bs) grabBlock bs
  }

  @inline final override
  def releaseBlock(block: TeslaBlock): Unit = {
    try {
      poolByPoolId(block.poolId).releaseBlock(block)
    } catch safely {
      case t: Throwable =>
        throw t
    }
  }

  @inline final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): TeslaBlockPool =
    TeslaBlockPool(poolId, size)

}
