/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valmap

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexSlotIndex}
import org.burstsys.tesla.part.teslaBuilderUseDefaultSize
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.logging._
import org.burstsys.zap.mutable.valmap

package object flex extends VitalsLogger {

  final val defaultStartSize: Int = 1e3.toInt // 1K byte start size as a default (can be overridden)

  private[valmap]
  lazy val coupler: TeslaFlexCoupler[ZapValMapBuilder, ZapValMap, ZapFlexValMap] =
    new TeslaFlexCoupler[ZapValMapBuilder, ZapValMap, ZapFlexValMap] {

      val collectorName: String = "flex-val-map"

      final override val powersOf2SlotCount: TeslaMemorySize = 16 // 64K max val-maps

      final override def instantiateCollector(ptr: TeslaMemoryPtr): ZapValMap = ZapValMapAnyVal(ptr)

      final override def instantiateProxy(index: TeslaFlexSlotIndex): ZapFlexValMap = ZapFlexValMapAnyVal(index)

      final override def allocateInternalCollector(builder: ZapValMapBuilder, size: TeslaMemorySize): ZapValMap = {
        val instance = valmap.factory.grabValMap(builder, size)
        val newSize = instance.currentMemorySize
        val blkSize = instance.availableMemorySize
        log info
          s"""|
              |--------------------------------------
              |ALLOC(basePtr=${instance.basePtr}
              | size=$size (${prettyByteSizeString(size)}))
              | newSize=$newSize (${prettyByteSizeString(newSize)}))
              | blkSize=$blkSize (${prettyByteSizeString(blkSize)}))
              |--------------------------------------""".stripMargin
        instance
      }

      final override def releaseInternalCollector(collector: ZapValMap): Unit = {
        log info s"FREE(basePtr=${collector.basePtr})"
        valmap.factory.releaseValMap(collector)
      }

    }

  final def grabValMap(builder: ZapValMapBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValMap = {
    coupler.grabFlexCollectorProxy(builder, startSize)
  }

  final def releaseValMap(part: ZapValMap): Unit = coupler.freeFlexCollectorProxy(part.asInstanceOf[ZapFlexValMap])

}
