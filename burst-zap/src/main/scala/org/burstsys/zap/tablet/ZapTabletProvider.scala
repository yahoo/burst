/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet

import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.collectors.tablet.{FeltTabletBuilder, FeltTabletCollector, FeltTabletPlan, FeltTabletProvider}
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.zap.tablet

final case
class ZapTabletProvider() extends FeltTabletProvider {
  override def newBuilder: FeltTabletBuilder = ZapTabletBuilder()

  override def builderClassName: String = classOf[ZapTabletBuilder].getName

  override def collectorClass: Class[_ <: FeltTabletCollector] = classOf[ZapTablet]

  override def collectorPlan(decl: FeltTabletDecl): FeltTabletPlan = ZapTabletPlan(decl)

  override def grabCollector(builder: FeltTabletBuilder, desiredSize: TeslaMemorySize): FeltTabletCollector =
    tablet.factory.grabZapTablet(builder.asInstanceOf[ZapTabletBuilder])

  override def releaseCollector(collector: FeltTabletCollector): Unit =
    tablet.factory.releaseZapTablet(collector.asInstanceOf[ZapTablet])
}
