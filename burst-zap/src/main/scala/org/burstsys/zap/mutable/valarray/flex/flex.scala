/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valarray

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexSlotIndex}
import org.burstsys.tesla.part.teslaBuilderUseDefaultSize
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.logging._
import org.burstsys.zap.mutable.valarray

package object flex extends VitalsLogger {

  final val defaultStartSize: Int = 1e3.toInt // 1K byte start size as a default (can be overridden)

  private[valarray]
  lazy val coupler: TeslaFlexCoupler[ZapValArrayBuilder, ZapValArr, ZapFlexValArr] =
    new TeslaFlexCoupler[ZapValArrayBuilder, ZapValArr, ZapFlexValArr] {

      val collectorName: String = "flex-val-array"

      final override val powersOf2SlotCount: TeslaMemorySize = 16 // 64K max val arrays

      final override def instantiateCollector(ptr: TeslaMemoryPtr): ZapValArr = ZapValArrayAnyVal(ptr)

      final override def instantiateProxy(index: TeslaFlexSlotIndex): ZapFlexValArr = ZapFlexValArrayAnyVal(index)

      final override def allocateInternalCollector(builder: ZapValArrayBuilder, size: TeslaMemorySize): ZapValArr = {
        val instance = valarray.factory.grabValArray(builder, size)
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

      final override def releaseInternalCollector(collector: ZapValArr): Unit = {
        log info s"FREE(basePtr=${collector.basePtr})"
        valarray.factory.releaseValArray(collector)
      }

    }

  final def grabValArray(builder: ZapValArrayBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValArr = {
    coupler.grabFlexCollectorProxy(builder, startSize)
  }

  final def releaseValArray(part: ZapValArr): Unit = coupler.freeFlexCollectorProxy(part.asInstanceOf[ZapFlexValArr])

}
