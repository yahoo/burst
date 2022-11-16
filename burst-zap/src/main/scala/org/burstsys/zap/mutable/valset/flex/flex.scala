/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valset

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexSlotIndex}
import org.burstsys.tesla.part.teslaBuilderUseDefaultSize
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.logging._
import org.burstsys.zap.mutable.valset

package object flex extends VitalsLogger {

  final val defaultStartSize: Int = 1e3.toInt // 1K byte start size as a default (can be overridden)

  private[valset]
  lazy val coupler: TeslaFlexCoupler[ZapValSetBuilder, ZapValSet, ZapFlexValSet] =
    new TeslaFlexCoupler[ZapValSetBuilder, ZapValSet, ZapFlexValSet] {

      val collectorName: String = "flex-val-set"

      final override val powersOf2SlotCount: TeslaMemorySize = 16 // 64K max val-sets

      final override def instantiateCollector(ptr: TeslaMemoryPtr): ZapValSet = ZapValSetContext(ptr)

      final override def instantiateProxy(index: TeslaFlexSlotIndex): ZapFlexValSet = ZapFlexValSetAnyVal(index)

      final override def allocateInternalCollector(builder: ZapValSetBuilder, size: TeslaMemorySize): ZapValSet = {
        val instance = valset.factory.grabValSet(builder, size)
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

      final override def releaseInternalCollector(collector: ZapValSet): Unit = {
        log info s"FREE(basePtr=${collector.basePtr})"
        valset.factory.releaseValSet(collector)
      }

    }

  final def grabValSet(builder: ZapValSetBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValSet = {
    coupler.grabFlexCollectorProxy(builder, startSize)
  }

  final def releaseValSet(part: ZapValSet): Unit = coupler.freeFlexCollectorProxy(part.asInstanceOf[ZapFlexValSet])

}
