/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary

import org.burstsys.brio.dictionary
import org.burstsys.brio.dictionary.mutable.{BrioMutableDictionary, BrioMutableDictionaryAnyVal}
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexSlotIndex}
import org.burstsys.vitals.instrument.MB
import org.burstsys.vitals.logging._

import scala.language.postfixOps

package object flex extends VitalsLogger {

  final val flexDictionaryDefaultStartSize: Int = (10 * MB).toInt // 10MB  start size as a default (can be overridden)

  private[brio]
  lazy val coupler: TeslaFlexCoupler[BrioDictionaryBuilder, BrioMutableDictionary, BrioFlexDictionary] =
    new TeslaFlexCoupler[BrioDictionaryBuilder, BrioMutableDictionary, BrioFlexDictionary] {

      val collectorName: String = "flex-dictionary"

      final override val powersOf2SlotCount: TeslaMemorySize = 17 // 128K max dictionaries

      final override def instantiateCollector(ptr: TeslaMemoryPtr): BrioMutableDictionary = BrioMutableDictionaryAnyVal(ptr)

      final override def instantiateProxy(index: TeslaFlexSlotIndex): BrioFlexDictionary = BrioFlexDictionaryAnyVal(index)

      final override def allocateInternalCollector(builder: BrioDictionaryBuilder, size: TeslaMemorySize): BrioMutableDictionary =
        dictionary.factory.grabMutableDictionary(builder, size)

      final override def releaseInternalCollector(collector: BrioMutableDictionary): Unit =
        dictionary.factory.releaseMutableDictionary(collector)

    }

  final
  def grabFlexDictionary(builder: BrioDictionaryBuilder = defaultDictionaryBuilder,
                         startSize: TeslaMemorySize = flex.flexDictionaryDefaultStartSize): BrioFlexDictionary =
    coupler.grabFlexCollectorProxy(builder, startSize)

  final
  def releaseFlexDictionary(dictionary: BrioFlexDictionary): Unit = coupler.freeFlexCollectorProxy(dictionary)

}
