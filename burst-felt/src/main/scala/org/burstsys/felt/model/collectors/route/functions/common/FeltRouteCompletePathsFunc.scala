/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.common

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.collectors.route.functions.fsm.FeltRouteFsmBackFillFunc
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._

object FeltRouteCompletePathsFunc {
  final val functionName: String = "routeCompletePaths"
}

/**
 * returns a count of the full paths recorded in the route
 */
trait FeltRouteCompletePathsFunc extends FeltRouteCommonFunction {

  final override val nodeName = "felt-route-complete-paths-func"

  final val functionName: String = FeltRouteCompletePathsFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> long
       |  returns a count of the full (committed) paths recorded in the route""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    feltType = FeltType.valScal[Long]
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
    val instance = s"$sweepRuntimeSym.${route.instanceVariable}"
    val backFillCall = FeltRouteFsmBackFillFunc.call(route)
    s"""|
        |${T(this)}
        |${I}$instance.$backFillCall ;
        |$I${cursor.callScope.scopeNull} = false;
        |$I${cursor.callScope.scopeVal} = $instance.$functionName;""".stripMargin
  }

}
