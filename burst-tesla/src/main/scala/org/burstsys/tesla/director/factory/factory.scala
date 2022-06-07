/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.director

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._

package object factory extends TeslaPartFactory[TeslaDirector, TeslaDirectorPool] with TeslaDirectorShop {

  startPartTender

  @inline final override
  def grabDirector(byteSize: TeslaMemorySize): TeslaDirector = {
    val bs = TeslaBlockSizes findBlockSize byteSize
    perThreadPartPool(bs) grabDirector byteSize
  }

  @inline final override
  def releaseDirector(buffer: TeslaDirector): Unit = {
    try {
      poolByPoolId(buffer.poolId).releaseDirector(buffer)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  @inline final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): TeslaDirectorPool =
    TeslaDirectorPool(poolId, size)

}
