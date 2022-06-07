/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valarray

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.part.teslaBuilderUseDefaultSize
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

package object factory extends TeslaPartFactory[ZapValArr, ZapValArrayPool] with ZapValArrayShop {

  final val ZapDefaultValArraySize: TeslaPoolId = 1e3.toInt

  startPartTender

  @inline final override
  def grabValArray(builder: ZapValArrayBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValArr = {
    val size = builder.chooseSize(startSize)
    val bs = TeslaBlockSizes findBlockSize size
    val part = perThreadPartPool(bs).grabValArray(builder = builder, size)
    part.validateAndIncrementReferenceCount()
    part
  }

  @inline final override
  def releaseValArray(valArray: ZapValArr): Unit = {
    valArray.validateAndDecrementReferenceCount()
    poolByPoolId(valArray.poolId).releaseValArray(valArray)
  }

  @inline final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): ZapValArrayPool =
    ZapValArrayPool(poolId, size)

}
