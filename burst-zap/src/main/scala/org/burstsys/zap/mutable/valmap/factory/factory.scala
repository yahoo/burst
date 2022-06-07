/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valmap

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.part.teslaBuilderUseDefaultSize
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

package object factory extends TeslaPartFactory[ZapValMap, ZapValMapPool]
  with ZapValMapShop {

  final val ZapDefaultValMapSize: TeslaPoolId = 1e3.toInt

  startPartTender

  @inline final override
  def grabValMap(builder: ZapValMapBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValMap = {
    val size = builder.chooseSize(startSize)
    val bs = TeslaBlockSizes findBlockSize size
    val part = perThreadPartPool(bs).grabValMap(builder = builder, size)
    part.validateAndIncrementReferenceCount()
    part
  }

  @inline final override
  def releaseValMap(valMap: ZapValMap): Unit = {
    valMap.validateAndDecrementReferenceCount()
    poolByPoolId(valMap.poolId).releaseValMap(valMap)
  }

  @inline final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): ZapValMapPool =
    ZapValMapPool(poolId, size)

}
