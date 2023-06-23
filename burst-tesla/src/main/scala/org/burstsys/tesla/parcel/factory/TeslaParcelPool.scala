/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.factory

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.TeslaBlockAnyVal
import org.burstsys.tesla.parcel
import org.burstsys.tesla.parcel.{TeslaParcel, TeslaParcelAnyVal, TeslaParcelReporter, packer}
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.logging.burstStdMsg

/**
 * the tesla pool for parcels
 */
final
case class TeslaParcelPool(poolId: TeslaPoolId, partByteSize: TeslaMemorySize)
  extends TeslaPartPool[TeslaParcel] with TeslaParcelShop {

  override val partQueueSize: Int = 1e4.toInt //  5e5

  @inline override
  def grabParcel(size: TeslaMemorySize): TeslaParcel = {
    markPartGrabbed()
    val part = partQueue.poll match {
      case null =>
        incrementPartsAllocated()
        TeslaParcelReporter.alloc(partByteSize)
        val bp = tesla.block.factory.grabBlock(size).blockBasePtr
        val allocatedBuffer = TeslaParcelAnyVal(bp)
        if (parcel.log.isTraceEnabled) {
          parcel.log trace burstStdMsg(s"allocated new parcel=${allocatedBuffer.basePtr} inuse=$partsInUse allocated=$partsAllocated")
        }
        allocatedBuffer.initialize(poolId)
        allocatedBuffer
      case p =>
        if (parcel.log.isTraceEnabled) {
          parcel.log trace burstStdMsg(s"allocated pooled parcel=${p.basePtr} inuse=$partsInUse allocated=$partsAllocated")
        }
        p.reset
    }
    incrementPartsInUse()
    TeslaParcelReporter.grab()
    part
  }

  @inline override
  def releaseParcel(r: TeslaParcel): Unit = {
    decrementPartsInUse()
    TeslaParcelReporter.release()
    if (parcel.log.isTraceEnabled) {
      parcel.log trace burstStdMsg(s"release parcel=${r.basePtr} inuse=$partsInUse allocated=$partsAllocated")
    }
    partQueue add r.blockPtr
  }

  @inline override
  def freePart(part: TeslaParcel): Long = {
    val block = TeslaBlockAnyVal(part.blockBasePtr)
    tesla.block.factory.releaseBlock(block)
    TeslaParcelReporter.free(partByteSize)
    // decrementPartsAllocated() isn't done here because the caller decremented it already
    if (parcel.log.isTraceEnabled) {
      parcel.log trace burstStdMsg(s"free parcel=${part.basePtr} inuse=$partsInUse allocated=$partsAllocated")
    }
    block.dataSize
  }
}
