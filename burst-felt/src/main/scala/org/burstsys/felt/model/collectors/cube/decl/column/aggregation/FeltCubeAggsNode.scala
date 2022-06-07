/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation

import org.burstsys.felt.model.collectors.cube.decl.column.FeltCubeColsNode
import org.burstsys.felt.model.tree.FeltLocation

/**
 * A set of felt cube aggregates
 */
trait FeltCubeAggsNode extends FeltCubeColsNode[FeltCubeAggDecl] {

  final val nodeName: String = "felt-cube-aggs"

  final val keyName: String = "aggregates"

  ///////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ///////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltCubeAggsNode = new FeltCubeAggsNode {
    sync(FeltCubeAggsNode.this)
    final override val location: FeltLocation = FeltCubeAggsNode.this.location
    final override val columns: Array[FeltCubeAggDecl] = FeltCubeAggsNode.this.columns.map(_.reduceStatics.resolveTypes)
  }

}

