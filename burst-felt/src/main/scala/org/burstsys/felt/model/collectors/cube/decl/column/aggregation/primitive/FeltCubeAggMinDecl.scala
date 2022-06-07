/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive

import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation._
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation

/**
 * A Felt compatible ''min'' aggregate AST node
 */
trait FeltCubeAggMinDecl extends FeltCubeAggPrimitiveDecl {

  final override val nodeName = "felt-cube-min-agg-col"

  final override val semanticType: FeltAggSemType = MIN_AGGREGATION_SEMANTIC

  final lazy val semantic: FeltCubeAggColSem = new FeltCubeAggMinColSem {
    final override val bType: BrioTypeKey = feltType.valueType
    final override val columnName: BrioRelationName = FeltCubeAggMinDecl.this.refName.shortName
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltCubeAggMinDecl = new FeltCubeAggMinDecl {
    sync(FeltCubeAggMinDecl.this)
    final override val location: FeltLocation = FeltCubeAggMinDecl.this.location
    final override val refName: FeltPathExpr = FeltCubeAggMinDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeAggMinDecl.this.valueType
  }

}
