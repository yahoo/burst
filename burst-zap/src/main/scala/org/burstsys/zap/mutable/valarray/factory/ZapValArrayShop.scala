/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valarray.factory

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.{TeslaPartShop, teslaBuilderUseDefaultSize}
import org.burstsys.zap.mutable.valarray.{ZapValArr, ZapValArrayBuilder}

trait ZapValArrayShop extends TeslaPartShop[ZapValArr, ZapValArrayBuilder] {

  final val partName: String = "val-array"

  /**
   *
   * @param builder
   * @return
   */
  def grabValArray(builder: ZapValArrayBuilder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapValArr

  /**
   *
   * @param array
   */
  def releaseValArray(array: ZapValArr): Unit

}
