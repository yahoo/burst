/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._

package object factory extends TeslaPartFactory[TeslaMutableBuffer, TeslaBufferPool] with TeslaBufferShop {

  override def poolSizeAsPercentOfDirectMemory: Double = 0.2

  startPartTender

  @inline final override
  def grabBuffer(byteSize: TeslaMemorySize): TeslaMutableBuffer = {
    val bufferByteSize = byteSize + mutable.SizeofMutableBufferHeader
    val bs = TeslaBlockSizes findBlockSize bufferByteSize
    perThreadPartPool(bs) grabBuffer bufferByteSize
  }

  @inline final override
  def releaseBuffer(buffer: TeslaMutableBuffer): Unit = {
    try {
      poolByPoolId(buffer.poolId).releaseBuffer(buffer)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  @inline final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): TeslaBufferPool =
    TeslaBufferPool(poolId, size)


}
