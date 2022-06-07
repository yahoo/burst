/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.common

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._

object FeltRouteStepCountFunc {
  final val functionName: String = "routeStepCount"
}

/**
 * returns a count of the full paths recorded in the route
 */
trait FeltRouteStepCountFunc extends FeltRouteCommonFunction {

  final override val nodeName = "felt-route-step-count-func"

  final val functionName: String = FeltRouteStepCountFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> long
       |  returns a count of the full (committed) steps recorded in the route""".stripMargin

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
    s"""|
        |${T(this)}
        |$I${cursor.callScope.scopeNull} = false;
        |$I${cursor.callScope.scopeVal} = $instance.$functionName;""".stripMargin
  }

}
