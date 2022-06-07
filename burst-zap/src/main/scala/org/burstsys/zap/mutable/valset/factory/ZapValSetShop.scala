/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valset.factory

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.{TeslaPartShop, teslaBuilderUseDefaultSize}
import org.burstsys.zap.mutable.valset.{ZapValSet, ZapValSetBuilder}

trait ZapValSetShop extends TeslaPartShop[ZapValSet, ZapValSetBuilder] {

  final val partName: String = "val-set"

  /**
   *
   * @param builder
   * @return
   */
  def grabValSet(builder: ZapValSetBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValSet

  /**
   *
   * @param set
   */
  def releaseValSet(set: ZapValSet): Unit

}
