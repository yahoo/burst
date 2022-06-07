/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.take

import org.burstsys.brio.types.BrioTypes.BrioTypeKey
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.{BOTTOM_AGGREGATION_SEMANTIC, FeltAggSemType}
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation

/**
 * A Felt compatible  ''bottom'' take aggregate AST node
 */
trait FeltCubeAggBottomDecl extends FeltCubeAggTakeDecl {

  final override val nodeName = "felt-cube-bottom-agg-col"

  final override val mode: FeltCubeTakeSemMode = FeltCubeBottomTakeSemMode

  final override val semanticType: FeltAggSemType = BOTTOM_AGGREGATION_SEMANTIC

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltCubeAggBottomDecl = new FeltCubeAggBottomDecl {
    sync(FeltCubeAggBottomDecl.this)
    final override val topValues: Array[FeltExpression] = FeltCubeAggBottomDecl.this.topValues.map(_.resolveTypes.reduceStatics)
    final override val refName: FeltPathExpr = FeltCubeAggBottomDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeAggBottomDecl.this.valueType
    final override val location: FeltLocation = FeltCubeAggBottomDecl.this.location
  }

}
