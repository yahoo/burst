/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.visits

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._

object FeltRouteVisitStepIsLastFunc {
  final val functionName: String = "routeVisitStepIsLast"
}

/**
 * current step time (valid only in a step visit)
 */
trait FeltRouteVisitStepIsLastFunc extends FeltRouteVisitFunction {

  final override val nodeName = "felt-route-visit-step-is-last-func"

  final val functionName: String = FeltRouteVisitStepIsLastFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> boolean
       |  is this the last step being visited? (valid only in a route visit)""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    feltType = FeltType.boolean
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
    val variable = s"$sweepRuntimeSym.${route.stepIsLastVariable}"
    s"""|
        |${T(this)}
        |$I${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = $variable; """.stripMargin
  }

}
