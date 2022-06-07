/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.fsm

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._


object FeltRouteFsmEndPathFunc {
  final val functionName: String = "routeFsmEndPath"
}

/**
 * Tell the FSM to end whatever path you are in. This creates a partial path if you are in a path
 */
trait FeltRouteFsmEndPathFunc extends FeltRouteFsmFunction {

  final override val nodeName = "felt-route-fsm-end-path-func"

  final val functionName: String = FeltRouteFsmEndPathFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> unit
       |    'end' whatever path you are in. This creates a partial path if you are in a path""".stripMargin

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

    s"""|
        |${T(this)}
        |$I$instance.$functionName( );""".stripMargin
  }

}
