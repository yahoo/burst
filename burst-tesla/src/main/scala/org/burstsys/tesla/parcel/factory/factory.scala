/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.TeslaPartBuilder
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._

package object factory extends TeslaPartFactory[TeslaParcel, TeslaParcelPool] with TeslaParcelShop {

  private final lazy
  val headerSize = TeslaParcelAnyVal(0).headerSize

  startPartTender

  @inline final override
  def grabParcel(byteSize: TeslaMemorySize): TeslaParcel = {
    // include our field overhead when asking for block memory
    val parcelByteSize = byteSize + headerSize
    val bs = TeslaBlockSizes findBlockSize parcelByteSize
    perThreadPartPool(bs) grabParcel parcelByteSize
  }

  @inline final override
  def releaseParcel(parcel: TeslaParcel): Unit = {
    try {
      poolByPoolId(parcel.poolId).releaseParcel(parcel)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  @inline final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): TeslaParcelPool =
    TeslaParcelPool(poolId, size)

}
