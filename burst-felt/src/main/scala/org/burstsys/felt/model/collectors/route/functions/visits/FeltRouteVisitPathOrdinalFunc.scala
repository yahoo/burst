/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.visits

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._


object FeltRouteVisitPathOrdinalFunc {
  final val functionName: String = "routeVisitPathOrdinal"
}

/**
 * current path ordinal (valid only in a  path or step visit)
 */
trait FeltRouteVisitPathOrdinalFunc extends FeltRouteVisitFunction {

  final override val nodeName = "felt-route-visit-path-ordinal-func"

  final val functionName: String = FeltRouteVisitPathOrdinalFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> long
       |  current path ordinal (valid only in a  path or step visit)""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    feltType = FeltType.long
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    // get route parameter
    val route = parameterAsReferenceOrThrow[FeltRouteRef](0, "route reference")
    val visitInstance = s"$sweepRuntimeSym.${route.instanceVariable}.currentIteration"
    s"""|
        |${T(this)}
        |$I${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = $visitInstance.pathOrdinal; """.stripMargin
  }

}
