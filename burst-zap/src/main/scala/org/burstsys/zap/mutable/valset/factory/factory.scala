/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valset

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.part.teslaBuilderUseDefaultSize
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

package object factory extends TeslaPartFactory[ZapValSet, ZapValSetPool] with ZapValSetShop {

  final val ZapDefaultValSetSize: TeslaPoolId = 1e3.toInt

  startPartTender

  @inline final override
  def grabValSet(builder: ZapValSetBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValSet = {
    val size = builder.chooseSize(startSize)
    val bs = TeslaBlockSizes findBlockSize size
    val part = perThreadPartPool(bs).grabValSet(builder = builder, size)
    part.validateAndIncrementReferenceCount()
    part
  }

  @inline final override
  def releaseValSet(valSet: ZapValSet): Unit = {
    valSet.validateAndDecrementReferenceCount()
    poolByPoolId(valSet.poolId).releaseValSet(valSet)
  }

  @inline final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): ZapValSetPool =
    ZapValSetPool(poolId, size)

}
