/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route

import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.route.{FeltRouteBuilder, FeltRouteCollector, FeltRoutePlan, FeltRouteProvider}
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.zap.route

final case
class ZapRouteProvider() extends FeltRouteProvider {
  override def newBuilder: FeltRouteBuilder = ZapRouteBuilder()

  override def builderClassName: String = classOf[ZapRouteBuilder].getName

  override def collectorClass: Class[_ <: FeltRouteCollector] = classOf[ZapRoute]

  override def collectorPlan(decl: FeltRouteDecl): FeltRoutePlan = ZapRoutePlan(decl)

  override def grabCollector(builder: FeltRouteBuilder, desiredSize: TeslaMemorySize): FeltRouteCollector = {
    route.flex.grabFlexRoute(builder.asInstanceOf[ZapRouteBuilder], desiredSize)
  }

  override def releaseCollector(collector: FeltRouteCollector): Unit =
    route.flex.releaseFlexRoute(collector.asInstanceOf[ZapRoute])

}
