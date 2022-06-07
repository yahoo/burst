/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route

import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.route.{FeltRouteBuilder, FeltRouteCollector, FeltRoutePlan, FeltRouteProvider}
import org.burstsys.zap.route

final case
class ZapRouteProvider() extends FeltRouteProvider {
  override def newBuilder: FeltRouteBuilder = ZapRouteBuilder()

  override def builderClassName: String = classOf[ZapRouteBuilder].getName

  override def collectorClass: Class[_ <: FeltRouteCollector] = classOf[ZapRoute]

  override def collectorPlan(decl: FeltRouteDecl): FeltRoutePlan = ZapRoutePlan(decl)

  override def grabCollector(builder: FeltRouteBuilder): FeltRouteCollector =
    route.factory.grabZapRoute(builder.asInstanceOf[ZapRouteBuilder])

  override def releaseCollector(collector: FeltRouteCollector): Unit =
    route.factory.releaseZapRoute(collector.asInstanceOf[ZapRoute])

}
