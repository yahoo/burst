/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexSlotIndex}
import org.burstsys.vitals.logging._
import org.burstsys.zap.{route, tablet}

package object flex extends VitalsLogger {

  final val defaultStartSize: Int = 1e3.toInt // 1K byte start size as a default (can be overridden)

  private[tablet]
  lazy val coupler: TeslaFlexCoupler[ZapTabletBuilder, ZapTablet, ZapFlexTablet] =
    new TeslaFlexCoupler[ZapTabletBuilder, ZapTablet, ZapFlexTablet] {

      val collectorName: String = "flex-tablet"

      final override val powersOf2SlotCount: TeslaMemorySize = 16 // 64K max tablets

      final override def instantiateCollector(ptr: TeslaMemoryPtr): ZapTablet =
        ZapTabletAnyVal(ptr)

      final override def instantiateProxy(index: TeslaFlexSlotIndex): ZapFlexTablet =
        ZapFlexTabletAnyVal(index)

      final override def allocateInternalCollector(builder: ZapTabletBuilder, size: TeslaMemorySize): ZapTablet = {
        val newTablet = tablet.factory.grabZapTablet(builder, size)
        val newSize = newTablet.currentMemorySize
        val blkSize = newTablet.availableMemorySize
        log debug s"ALLOC(basePtr=${newTablet.basePtr} Tablet newSize=$newSize blkSize=$blkSize"
        newTablet
      }

      final override def releaseInternalCollector(collector: ZapTablet): Unit = {
        log debug s"FREE(basePtr=${collector.basePtr}) Tablet"
        tablet.factory.releaseZapTablet(collector)
      }

    }

  final def grabFlexTablet(builder: ZapTabletBuilder, startSize: TeslaMemorySize): ZapFlexTablet = {
    val proxy = coupler.grabFlexCollectorProxy(builder, startSize)
    proxy
  }

  final def releaseFlexTablet(cube: ZapTablet): Unit = {
    coupler.freeFlexCollectorProxy(cube.asInstanceOf[ZapFlexTablet])
  }

}
