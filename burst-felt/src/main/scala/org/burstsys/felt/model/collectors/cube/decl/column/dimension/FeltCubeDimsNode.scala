/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension

import org.burstsys.felt.model.collectors.cube.decl.column.FeltCubeColsNode
import org.burstsys.felt.model.tree.FeltLocation

/**
 * A set of felt cube dimensions
 */
trait FeltCubeDimsNode extends FeltCubeColsNode[FeltCubeDimDecl] {

  final val nodeName: String = "felt-cube-dims"

  final val keyName: String = "dimensions"

  ///////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ///////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltCubeDimsNode = new FeltCubeDimsNode {
    sync(FeltCubeDimsNode.this)
    final override val location: FeltLocation = FeltCubeDimsNode.this.location
    final override val columns: Array[FeltCubeDimDecl] = FeltCubeDimsNode.this.columns.map(_.reduceStatics.resolveTypes)
  }

}
