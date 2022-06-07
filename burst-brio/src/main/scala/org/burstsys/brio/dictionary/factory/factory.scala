/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary

import org.burstsys.brio.dictionary.flex.BrioDictionaryBuilder
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.part.teslaBuilderUseDefaultSize
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

package object factory extends TeslaPartFactory[BrioMutableDictionary, BrioDictionaryPool]
  with BrioDictionaryShop with VitalsLogger {

  startPartTender

  @inline final override
  def grabMutableDictionary(builder: BrioDictionaryBuilder = defaultDictionaryBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): BrioMutableDictionary = {
    val size = builder.chooseSize(startSize)
    val bs = TeslaBlockSizes findBlockSize size
    val dictionary = perThreadPartPool(bs) grabMutableDictionary(builder, size)
    dictionary.validateAndIncrementReferenceCount()
    dictionary
  }

  @inline final override
  def releaseMutableDictionary(dictionary: BrioMutableDictionary): Unit = {
    dictionary.validateAndDecrementReferenceCount()
    poolByPoolId(dictionary.poolId).releaseMutableDictionary(dictionary)
  }

  @inline final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): BrioDictionaryPool =
    BrioDictionaryPool(poolId, size)

}
