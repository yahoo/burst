/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexSlotIndex}
import org.burstsys.vitals.logging._
import org.burstsys.zap.route

package object flex extends VitalsLogger {

  final val defaultStartSize: Int = 1e3.toInt // 1K byte start size as a default (can be overridden)

  private[route]
  lazy val coupler: TeslaFlexCoupler[ZapRouteBuilder, ZapRoute, ZapFlexRoute] =
    new TeslaFlexCoupler[ZapRouteBuilder, ZapRoute, ZapFlexRoute] {

      val collectorName: String = "flex-route"

      final override val powersOf2SlotCount: TeslaMemorySize = 16 // 64K max routes

      final override def instantiateCollector(ptr: TeslaMemoryPtr): ZapRoute =
        ZapRouteContext(ptr)

      final override def instantiateProxy(index: TeslaFlexSlotIndex): ZapFlexRoute =
        ZapFlexRouteAnyVal(index)

      final override def allocateInternalCollector(builder: ZapRouteBuilder, size: TeslaMemorySize): ZapRoute = {
        val newRoute = route.factory.grabZapRoute(builder, size)
        val newSize = newRoute.currentMemorySize
        val blkSize = newRoute.availableMemorySize
        log debug s"ALLOC(basePtr=${newRoute.basePtr} Route newSize=$newSize blkSize=$blkSize"
        newRoute
      }

      final override def releaseInternalCollector(collector: ZapRoute): Unit = {
        log debug s"FREE(basePtr=${collector.basePtr}) Route"
        route.factory.releaseZapRoute(collector)
      }

    }

  final def grabFlexRoute(builder: ZapRouteBuilder, startSize: TeslaMemorySize): ZapFlexRoute = {
    val proxy = coupler.grabFlexCollectorProxy(builder, startSize)
    proxy
  }

  final def releaseFlexRoute(cube: ZapRoute): Unit = {
    coupler.freeFlexCollectorProxy(cube.asInstanceOf[ZapFlexRoute])
  }

}
