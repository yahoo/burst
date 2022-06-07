/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valmap.factory

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.{TeslaPartShop, teslaBuilderUseDefaultSize}
import org.burstsys.zap.mutable.valmap.{ZapValMap, ZapValMapBuilder}

trait ZapValMapShop extends TeslaPartShop[ZapValMap, ZapValMapBuilder] {

  final val partName: String = "val-map"

  /**
   *
   * @param builder
   * @return
   */
  def grabValMap(builder: ZapValMapBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValMap

  /**
   *
   * @param set
   */
  def releaseValMap(set: ZapValMap): Unit

}
