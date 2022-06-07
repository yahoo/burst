/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.wheel

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

package object factory extends TeslaPartFactory[ZapWheel, ZapWheelPool] with ZapWheelShop {

  startPartTender

  final override
  def grabZapWheel(schema: ZapWheelBuilder): ZapWheel = {
    val bs = TeslaBlockSizes findBlockSize schema.defaultStartSize
    val wheel = perThreadPartPool(bs) grabZapWheel schema
    wheel.validateAndIncrementReferenceCount()
    wheel
  }

  final override
  def releaseZapWheel(wheel: ZapWheel): Unit = {
    wheel.validateAndDecrementReferenceCount()
    poolByPoolId(wheel.poolId).releaseZapWheel(wheel)
  }

  final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): ZapWheelPool =
    ZapWheelPool(poolId, size)

}
