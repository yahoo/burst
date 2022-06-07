/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route

import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.route.{FeltRoutePlan, FeltRouteProvider}

/**
 *
 */
trait ZapRoutePlan extends FeltRoutePlan

object ZapRoutePlan {
  def apply(cubeDecl: FeltRouteDecl): ZapRoutePlan = ZapRoutePlanContext(cubeDecl: FeltRouteDecl)
}

private final case
class ZapRoutePlanContext(decl: FeltRouteDecl) extends ZapRoutePlan {

  val binding: FeltRouteProvider = decl.global.binding.collectors.routes

}
