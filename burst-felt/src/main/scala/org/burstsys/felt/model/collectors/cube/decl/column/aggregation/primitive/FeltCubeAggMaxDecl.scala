/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive

import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation._
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation

/**
 * A Felt compatible ''max'' aggregate AST node
 */
trait FeltCubeAggMaxDecl extends FeltCubeAggPrimitiveDecl {

  final override val nodeName = "felt-cube-max-agg-col"

  final override val semanticType: FeltAggSemType = MAX_AGGREGATION_SEMANTIC

  final lazy val semantic: FeltCubeAggColSem = new FeltCubeAggMaxColSem {
    final override val bType: BrioTypeKey = feltType.valueType
    final override val columnName: BrioRelationName = refName.shortName
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltCubeAggMaxDecl = new FeltCubeAggMaxDecl {
    sync(FeltCubeAggMaxDecl.this)
    final override val location: FeltLocation = FeltCubeAggMaxDecl.this.location
    final override val refName: FeltPathExpr = FeltCubeAggMaxDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeAggMaxDecl.this.valueType
  }

}
