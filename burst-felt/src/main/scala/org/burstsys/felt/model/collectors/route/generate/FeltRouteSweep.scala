/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.generate

import org.burstsys.felt.model.collectors.route.{FeltRouteBuilder, FeltRouteCollector}
import org.burstsys.felt.model.sweep.FeltSweepComponent

/**
 * The per sweep runtime functions for routes
 */
trait FeltRouteSweep extends Any with FeltSweepComponent {

  final
  def grabRouteCollector(builder: FeltRouteBuilder): FeltRouteCollector = ???

  final
  def releaseRouteCollector(route: FeltRouteCollector): Unit = ???

}

