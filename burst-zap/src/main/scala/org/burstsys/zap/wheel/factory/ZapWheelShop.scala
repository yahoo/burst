/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.wheel.factory

import org.burstsys.tesla.part.TeslaPartShop
import org.burstsys.zap.wheel
import org.burstsys.zap.wheel.{ZapWheel, ZapWheelAnyVal, ZapWheelBuilder}

trait ZapWheelShop extends TeslaPartShop[ZapWheel, ZapWheelBuilder] {

  def partName: String = wheel.partName

  /**
   *
   * @return
   */
  def grabZapWheel(schema: ZapWheelBuilder): ZapWheel

  /**
   *
   * @param route
   */
  def releaseZapWheel(route: ZapWheel): Unit

}
