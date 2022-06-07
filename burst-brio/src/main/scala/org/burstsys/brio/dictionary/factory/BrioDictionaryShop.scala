/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.factory

import org.burstsys.brio.dictionary
import org.burstsys.brio.dictionary.defaultDictionaryBuilder
import org.burstsys.brio.dictionary.flex.BrioDictionaryBuilder
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.part.{TeslaPartShop, teslaBuilderUseDefaultSize}

/**
 * part shop for brio dictionaries
 */
trait BrioDictionaryShop extends TeslaPartShop[BrioMutableDictionary, BrioDictionaryBuilder] {

  def partName: String = dictionary.partName

  /**
   * grab a dictionary from the underlying factory and its part pools
   *
   * @param startSize
   * @return
   */
  def grabMutableDictionary(builder: BrioDictionaryBuilder = defaultDictionaryBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): BrioMutableDictionary

  /**
   * release a dictionary into the underlying factory and its part pools
   *
   * @param dictionary
   */
  def releaseMutableDictionary(dictionary: BrioMutableDictionary): Unit

}
