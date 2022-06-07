/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.visits

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._

object FeltRouteVisitStepIsFirstFunc {
  final val functionName: String = "routeVisitStepIsFirst"
}

/**
 * current step time (valid only in a step visit)
 */
trait FeltRouteVisitStepIsFirstFunc extends FeltRouteVisitFunction {

  final override val nodeName = "felt-route-visit-step-is-first-func"

  final val functionName: String = FeltRouteVisitStepIsFirstFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> boolean
       |  is this the first step being visited? (valid only in a route visit)""".stripMargin

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
    val variable = s"$sweepRuntimeSym.${route.stepIsFirstVariable}"
    s"""|
        |${T(this)}
        |$I${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = $variable; """.stripMargin
  }

}
