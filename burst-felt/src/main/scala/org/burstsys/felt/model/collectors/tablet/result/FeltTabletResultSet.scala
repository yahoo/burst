/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.result

import org.burstsys.fabric.wave.execution.model.result.set.FabricResultSetName
import org.burstsys.felt.model.collectors.result.{FeltCollectorResultSet, FeltCollectorResultSetContext}
import org.burstsys.felt.model.collectors.tablet.plane.FeltTabletPlane
import org.burstsys.felt.model.collectors.tablet.{FeltTabletBuilder, FeltTabletCollector}

/**
 * zero or more result rows...
 */
trait FeltTabletResultSet extends FeltCollectorResultSet

/**
 * constructors
 */
object FeltTabletResultSet {

  def apply(name: FabricResultSetName, plane: FeltTabletPlane): FeltTabletResultSet =
    FeltTabletResultSetContext(resultName = name, plane = plane)

}

private[result] final case
class FeltTabletResultSetContext(resultName: FabricResultSetName, plane: FeltTabletPlane)
  extends FeltCollectorResultSetContext[FeltTabletBuilder, FeltTabletCollector](resultName, plane)
    with FeltTabletResultSet {

  override def toString: FabricResultSetName = s"FeltTabletResultSet(resultName=$resultName)"

}
