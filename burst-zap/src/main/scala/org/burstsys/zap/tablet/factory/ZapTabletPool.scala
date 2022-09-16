/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet.factory

import org.burstsys.felt.model.collectors.tablet.FeltTabletBuilder
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.block.TeslaBlockAnyVal
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.zap.tablet.{ZapTablet, ZapTabletAnyVal, ZapTabletReporter}

import scala.language.postfixOps

final
case class ZapTabletPool(poolId: TeslaPoolId, partByteSize: TeslaMemoryOffset)
  extends TeslaPartPool[ZapTablet] with ZapTabletShop {

  override val partQueueSize: Int = 5e3.toInt // 5e3

  @inline override
  def grabZapTablet(builder: FeltTabletBuilder, startSize: TeslaMemorySize): ZapTablet = {
    markPartGrabbed()
    val part = partQueue poll match {
      case null =>
        incrementPartsAllocated()
        ZapTabletReporter.alloc(partByteSize)
        ZapTabletAnyVal(tesla.block.factory.grabBlock(startSize).blockBasePtr).initialize(poolId)
      case r => r.reset
    }
    incrementPartsInUse()
    ZapTabletReporter.grab()
    part
  }

  @inline override
  def releaseZapTablet(tablet: ZapTablet): Unit = {
    decrementPartsInUse()
    ZapTabletReporter.release()
    partQueue add tablet
  }

  /**
   *
   * @param part
   * @return
   */
  @inline override
  def freePart(part: ZapTablet): TeslaMemoryPtr = {
    val block = TeslaBlockAnyVal(part.blockBasePtr)
    tesla.block.factory.releaseBlock(block)
    ZapTabletReporter.free(partByteSize)
    partByteSize
  }

}
