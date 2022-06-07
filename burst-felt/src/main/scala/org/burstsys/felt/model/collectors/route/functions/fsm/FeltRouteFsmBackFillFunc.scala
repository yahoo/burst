/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.fsm

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._

object FeltRouteFsmBackFillFunc {

  final val functionName: String = "routeFsmBackFill"

  final
  def call(reference: FeltRouteRef)(implicit cursor: FeltCodeCursor): FeltCode = {
    val builder = s"${reference.builderVariable}"
    s"$functionName( $builder ); // back fill route journal state"
  }

}

trait FeltRouteFsmBackFillFunc extends FeltRouteFsmFunction {

  final override val nodeName = "felt-route-fsm-back-fill-func"

  final val functionName: String = FeltRouteFsmBackFillFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> unit
       |    backtrack/rerwite journal to create consistent state sequence""".stripMargin

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
    val builder =   route.builderVariable

    s"""|
        |${T(this)}
        |$I$instance.$functionName( $builder );""".stripMargin
  }

}
