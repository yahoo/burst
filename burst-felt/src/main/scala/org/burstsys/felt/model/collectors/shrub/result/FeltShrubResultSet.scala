/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.shrub.result

import org.burstsys.fabric.execution.model.result.set.FabricResultSetName
import org.burstsys.felt.model.collectors.result.{FeltCollectorResultSet, FeltCollectorResultSetContext}
import org.burstsys.felt.model.collectors.shrub.plane.FeltShrubPlane
import org.burstsys.felt.model.collectors.shrub.{FeltShrubBuilder, FeltShrubCollector}

/**
 * zero or more result rows...
 */
trait FeltShrubResultSet extends FeltCollectorResultSet

/**
 * constructors
 */
object FeltShrubResultSet {

  def apply(name: FabricResultSetName, plane: FeltShrubPlane): FeltShrubResultSet =
    FeltShrubResultSetContext(resultName = name, plane = plane)

}

private[result] final case
class FeltShrubResultSetContext(resultName: FabricResultSetName, plane: FeltShrubPlane)
  extends FeltCollectorResultSetContext[FeltShrubBuilder, FeltShrubCollector](resultName, plane)
    with FeltShrubResultSet {

  override def toString: FabricResultSetName = s"FeltShrubResultSet(resultName=$resultName)"


}
