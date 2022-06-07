/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive

import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation._
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation

/**
 * A Felt compatible ''project'' aggregate AST node
 */
trait FeltCubeAggProjectDecl extends FeltCubeAggPrimitiveDecl {

  final override val nodeName = "felt-cube-proj-agg-col"

  final override val semanticType: FeltAggSemType = PROJECT_AGGREGATION_SEMANTIC

  final lazy val semantic: FeltCubeAggColSem = new FeltCubeAggProjectColSem {
    final override val bType: BrioTypeKey = feltType.valueType
    final override val columnName: BrioRelationName = FeltCubeAggProjectDecl.this.columnName
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltCubeAggProjectDecl = new FeltCubeAggProjectDecl {
    sync(FeltCubeAggProjectDecl.this)
    final override val location: FeltLocation = FeltCubeAggProjectDecl.this.location
    final override val refName: FeltPathExpr = FeltCubeAggProjectDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeAggProjectDecl.this.valueType
  }

}
