/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.fsm

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._


object FeltRouteFsmInPathFunc {
  final val functionName: String = "routeFsmInPath"
}

/**
 * Returns true if Fsm is currently in an active path
 */
trait FeltRouteFsmInPathFunc extends FeltRouteFsmFunction with FeltBoolExpr {

  final override val nodeName = "felt-route-fsm-in-path-func"

  final val functionName: String = FeltRouteFsmInPathFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> boolean
       |    true if the route is currently in a path""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    feltType = FeltType.valScal[Boolean]
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
        |$I${cursor.callScope.scopeVal} = $I$instance.$functionName();""".stripMargin
  }

}
