/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.factory

import org.burstsys.brio.dictionary.{BrioDictionaryReporter, factory}
import org.burstsys.brio.dictionary.flex.BrioDictionaryBuilder
import org.burstsys.brio.dictionary.mutable.{BrioMutableDictionary, BrioMutableDictionaryAnyVal}
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.TeslaBlockAnyVal
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.logging.burstStdMsg

import scala.language.postfixOps

/**
 * tesla parts pool for dictionaries
 */
final
case class BrioDictionaryPool(poolId: TeslaPoolId, partByteSize: TeslaMemoryOffset)
  extends TeslaPartPool[BrioMutableDictionary] with BrioDictionaryShop {

  override val partQueueSize: Int = 5e4.toInt // 5e4

  @inline override
  def grabMutableDictionary(builder: BrioDictionaryBuilder, bytesSize: TeslaMemorySize): BrioMutableDictionary = {
    val size = builder.chooseSize(bytesSize)
    markPartGrabbed()
    val part = partQueue poll match {
      case null =>
        incrementPartsAllocated()
        BrioDictionaryReporter.alloc(partByteSize)
        val dictionary = BrioMutableDictionaryAnyVal(tesla.block.factory.grabBlock(size).blockBasePtr)
        dictionary.initialize(poolId, builder)
        if (factory.log.isTraceEnabled) {
          factory.log trace burstStdMsg(s"allocated new dictionary=${dictionary.basePtr} inuse=$partsInUse allocated=$partsAllocated")
        }
        dictionary
      case dictionary =>
        dictionary.reset(builder)
        if (factory.log.isTraceEnabled) {
          factory.log trace burstStdMsg(s"allocated pooled dictionary=${dictionary.basePtr} inuse=$partsInUse allocated=$partsAllocated")
        }
        dictionary
    }
    incrementPartsInUse()
    BrioDictionaryReporter.grab()
    part
  }

  @inline override
  def releaseMutableDictionary(d: BrioMutableDictionary): Unit = {
    decrementPartsInUse()
    BrioDictionaryReporter.release()
    if (factory.log.isTraceEnabled) {
      factory.log trace burstStdMsg(s"returning dictionary=${d.basePtr} inuse=$partsInUse allocated=$partsAllocated")
    }
    partQueue add d
  }

  @inline override
  def freePart(part: BrioMutableDictionary): Long = {
    if (factory.log.isTraceEnabled) {
      factory.log trace burstStdMsg(s"freeing dictionary=${part.basePtr} inuse=$partsInUse allocated=$partsAllocated")
    }
    tesla.block.factory.releaseBlock(TeslaBlockAnyVal(part.blockPtr))
    BrioDictionaryReporter.free(partByteSize)
    partByteSize
  }

}
