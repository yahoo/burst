/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.scopes

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._


object FeltRouteScopeCurrentStepFunc {
  final val functionName: String = "routeScopeCurrentStep"
}

/**
 * the current (dirty) step last recorded in the journal for this scope
 */
trait FeltRouteScopeCurrentStepFunc extends FeltRouteScopeFunction {

  final override val nodeName = "felt-route-scope-current-step-func"

  final val functionName: String = FeltRouteScopeCurrentStepFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> integer
       |  the current (dirty) step last recorded in the journal for this scope""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    feltType = FeltType.valScal[Int]
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
        |$I${cursor.callScope.scopeVal} = $instance.$functionName( );""".stripMargin
  }

}
