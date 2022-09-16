/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet

import org.burstsys.felt.model.collectors.tablet.FeltTabletBuilder
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.pool._

package object factory extends TeslaPartFactory[ZapTablet, ZapTabletPool] with ZapTabletShop {

  startPartTender

  final override
  def grabZapTablet(builder: FeltTabletBuilder, startSize: TeslaMemorySize): ZapTablet = {
    val bs = TeslaBlockSizes findBlockSize startSize
    val tablet = perThreadPartPool(bs).grabZapTablet(builder, startSize)
    tablet.validateAndIncrementReferenceCount()
    tablet
  }

  final override
  def releaseZapTablet(tablet: ZapTablet): Unit = {
    tablet.validateAndDecrementReferenceCount()
    poolByPoolId(tablet.poolId).releaseZapTablet(tablet)
  }

  final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): ZapTabletPool =
    ZapTabletPool(poolId, size)

}
