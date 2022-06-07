/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.scopes

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._


object FeltRouteScopeAbortFunc {
  final val functionName: String = "routeScopeAbort"
}

/**
 * abort a route transaction (roll back dirty ptr to clean ptr)
 */
trait FeltRouteScopeAbortFunc extends FeltRouteScopeFunction {

  final override val nodeName = "felt-route-scope-abort-func"

  final val functionName: String = FeltRouteScopeAbortFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> unit
       |  abort a route transaction (roll back dirty ptr to clean ptr)""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    feltType = FeltType.unit
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
    val builder = route.builderVariable
    s"""|
        |${T(this)}
        |$I$instance.$functionName( $builder );""".stripMargin
  }

}
