/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.result

import org.burstsys.fabric.wave.execution.model.result.set.FabricResultSetName
import org.burstsys.felt.model.collectors.result.{FeltCollectorResultSet, FeltCollectorResultSetContext}
import org.burstsys.felt.model.collectors.route.plane.FeltRoutePlane
import org.burstsys.felt.model.collectors.route.{FeltRouteBuilder, FeltRouteCollector}

/**
 * zero or more result rows...
 */
trait FeltRouteResultSet extends FeltCollectorResultSet

/**
 * constructors
 */
object FeltRouteResultSet {

  def apply(name: FabricResultSetName, plane: FeltRoutePlane): FeltRouteResultSet =
    FeltRouteResultSetContext(resultName = name, plane = plane)

}

private[result] final case
class FeltRouteResultSetContext(resultName: FabricResultSetName, plane: FeltRoutePlane)
  extends FeltCollectorResultSetContext[FeltRouteBuilder, FeltRouteCollector](resultName, plane)
    with FeltRouteResultSet {

  override def toString: FabricResultSetName = s"FeltRouteResultSet(resultName=$resultName)"

}
