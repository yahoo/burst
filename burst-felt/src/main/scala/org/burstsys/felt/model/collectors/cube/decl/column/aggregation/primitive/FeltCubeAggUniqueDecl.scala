/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive

import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation._
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation

/**
 * A Felt compatible ''unique'' aggregate AST node
 */
trait FeltCubeAggUniqueDecl extends FeltCubeAggPrimitiveDecl {

  final override val nodeName = "felt-cube-proj-agg-col"

  final override val semanticType: FeltAggSemType = UNIQUE_AGGREGATION_SEMANTIC

  final lazy val semantic: FeltCubeAggColSem = new FeltCubeAggUniqueColSem {
    final override val bType: BrioTypeKey = feltType.valueType
    final override val columnName: BrioRelationName = FeltCubeAggUniqueDecl.this.columnName
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltCubeAggUniqueDecl = new FeltCubeAggUniqueDecl {
    sync(FeltCubeAggUniqueDecl.this)
    final override val location: FeltLocation = FeltCubeAggUniqueDecl.this.location
    final override val refName: FeltPathExpr = FeltCubeAggUniqueDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeAggUniqueDecl.this.valueType
  }

}
