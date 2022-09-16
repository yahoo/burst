/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

package object factory extends TeslaPartFactory[ZapRoute, ZapRoutePool]
  with ZapRouteShop {

  startPartTender

  final override
  def grabZapRoute(builder: ZapRouteBuilder, startSize: TeslaMemorySize): ZapRoute = {
    val bs = TeslaBlockSizes findBlockSize startSize
    val route = perThreadPartPool(bs).grabZapRoute(builder, startSize)
    route.validateAndIncrementReferenceCount()
    route
  }

  final override
  def releaseZapRoute(route: ZapRoute): Unit = {
    route.validateAndDecrementReferenceCount()
    poolByPoolId(route.poolId).releaseZapRoute(route)
  }

  final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): ZapRoutePool =
    ZapRoutePool(poolId, size)

}
