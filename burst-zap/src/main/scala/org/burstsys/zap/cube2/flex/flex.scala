/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2

import org.burstsys.brio.dictionary.flex.BrioFlexDictionary
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexSlotIndex}
import org.burstsys.tesla.part.teslaBuilderUseDefaultSize
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.logging._
import org.burstsys.zap.cube2

package object flex extends VitalsLogger {

  final val defaultStartSize: Int = 1e3.toInt // 1K byte start size as a default (can be overridden)

  private[cube2]
  lazy val coupler: TeslaFlexCoupler[ZapCube2Builder, ZapCube2, ZapFlexCube2] =
    new TeslaFlexCoupler[ZapCube2Builder, ZapCube2, ZapFlexCube2] {

      val collectorName: String = "flex-cube2"

      final override val powersOf2SlotCount: TeslaMemorySize = 16 // 64K max cubes

      final override def instantiateCollector(ptr: TeslaMemoryPtr): ZapCube2 =
        ZapCube2AnyVal(ptr)

      final override def instantiateProxy(index: TeslaFlexSlotIndex): ZapFlexCube2 =
        ZapFlexCube2AnyVal(index)

      final override def allocateInternalCollector(builder: ZapCube2Builder, size: TeslaMemorySize): ZapCube2 = {
        val cube = cube2.factory.grabCube2(builder, size)
        val newSize = cube.currentMemorySize
        val blkSize = cube.availableMemorySize
        log debug s"ALLOC(basePtr=${cube.basePtr} Cube2 newSize=$newSize blkSize=$blkSize"
        cube
      }

      final override def releaseInternalCollector(collector: ZapCube2): Unit = {
        log debug s"FREE(basePtr=${collector.basePtr}) Cube2"
        cube2.factory.releaseCube2(collector)
      }

    }

  final def grabFlexCube(dictionary: BrioFlexDictionary, builder: ZapCube2Builder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapCube2 = {
    val proxy = coupler.grabFlexCollectorProxy(builder, startSize)
    proxy.dictionary = dictionary
    proxy
  }

  final def grabFlexCube(builder: ZapCube2Builder, startSize: TeslaMemorySize): ZapCube2 = {
    val proxy = coupler.grabFlexCollectorProxy(builder, startSize)
    proxy
  }

  final def releaseFlexCube(cube: ZapCube2): Unit = coupler.freeFlexCollectorProxy(cube.asInstanceOf[ZapFlexCube2])

}
