/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.part.teslaBuilderUseDefaultSize
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

/**
 * TODO - should we be storing a LONG memory pointer instead of the AnyVal object?
 */
package object factory extends TeslaPartFactory[ZapCube2, ZapCube2Pool]
  with ZapCube2Shop {

  final val ZapDefaultG2CubeSize: TeslaPoolId = 10e6.toInt

  startPartTender

  @inline final override
  def grabCube2(builder: ZapCube2Builder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapCube2 = {
    val size = builder.chooseSize(startSize)
    val bs = TeslaBlockSizes findBlockSize size
    val part = perThreadPartPool(bs).grabCube2(builder = builder, size)
    part.validateAndIncrementReferenceCount()
    part
  }

  @inline final override
  def releaseCube2(cube: ZapCube2): Unit = {
    cube.validateAndDecrementReferenceCount()
    poolByPoolId(cube.poolId).releaseCube2(cube)
  }

  @inline final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): ZapCube2Pool =
    ZapCube2Pool(poolId, size)

}
