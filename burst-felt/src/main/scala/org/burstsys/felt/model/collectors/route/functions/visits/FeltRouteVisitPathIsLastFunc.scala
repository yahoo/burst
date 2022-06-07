/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.visits

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._

object FeltRouteVisitPathIsLastFunc {
  final val functionName: String = "routeVisitPathIsLast"
}

/**
 * current step time (valid only in a step visit)
 */
trait FeltRouteVisitPathIsLastFunc extends FeltRouteVisitFunction {

  final override val nodeName = "felt-route-visit-path-is-last-func"

  final val functionName: String = FeltRouteVisitPathIsLastFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> boolean
       |  is this the last path being visited? (valid only in a route visit)""".stripMargin

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
    val variable = s"$sweepRuntimeSym.${route.pathIsLastVariable}"
    s"""|
        |${T(this)}
        |$I${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = $variable; """.stripMargin
  }

}
