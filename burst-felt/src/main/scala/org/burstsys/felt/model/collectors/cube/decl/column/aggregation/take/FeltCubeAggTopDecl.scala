/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.take

import org.burstsys.brio.types.BrioTypes.BrioTypeKey
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.{FeltAggSemType, TOP_AGGREGATION_SEMANTIC}
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation

/**
 * A Felt compatible ''top''   take aggregate AST node
 */
trait FeltCubeAggTopDecl extends FeltCubeAggTakeDecl {

  final override val nodeName = "felt-cube-top-agg-col"

  final override val mode: FeltCubeTakeSemMode = FeltCubeTopTakeSemMode
  final override val semanticType: FeltAggSemType = TOP_AGGREGATION_SEMANTIC

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltCubeAggTopDecl = new FeltCubeAggTopDecl {
    sync(FeltCubeAggTopDecl.this)
    final override val topValues: Array[FeltExpression] = FeltCubeAggTopDecl.this.topValues.map(_.resolveTypes.reduceStatics)
    final override val refName: FeltPathExpr = FeltCubeAggTopDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeAggTopDecl.this.valueType
    final override val location: FeltLocation = FeltCubeAggTopDecl.this.location
  }

}
