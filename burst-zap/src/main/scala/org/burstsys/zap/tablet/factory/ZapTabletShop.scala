/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet.factory

import org.burstsys.felt.model.collectors.tablet.FeltTabletBuilder
import org.burstsys.tesla.part.TeslaPartShop
import org.burstsys.zap.tablet.ZapTablet

trait ZapTabletShop extends TeslaPartShop[ZapTablet, FeltTabletBuilder] {

  final val partName: String = "tablet"

  /**
   *
   * @param builder
   * @return
   */
  def grabZapTablet(builder: FeltTabletBuilder): ZapTablet

  /**
   *
   * @param route
   */
  def releaseZapTablet(route: ZapTablet): Unit

}
