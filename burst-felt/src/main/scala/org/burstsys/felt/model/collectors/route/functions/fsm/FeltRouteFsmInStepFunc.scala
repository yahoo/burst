/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.fsm

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.expressionEvaluate
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types._

import scala.language.postfixOps

object FeltRouteFsmInStepFunc {
  final val functionName: String = "routeFsmInStep"
}

/**
 * Return true if the FSM is in a specific step
 */
trait FeltRouteFsmInStepFunc extends FeltRouteFsmFunction with FeltBoolExpr {

  final override val nodeName = "felt-route-fsm-in-step-func"

  final val functionName: String = FeltRouteFsmInStepFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route, step) -> boolean
       |    true if the current step in a given route is of a given step key""".stripMargin

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
    parameterCountIs(2)

    // get route parameter
    val route = parameterAsReferenceOrThrow[FeltRouteRef](0, "route reference")
    val instance = s"$sweepRuntimeSym.${route.instanceVariable}"

    // get stepkey parameter
    val stepKeyExpr = fixedParameter[Int](1, "step-key")
    val stepKeyCursor = cursor indentRight 1 scopeDown

    s"""|
        |${T(this)}
        |${expressionEvaluate(stepKeyExpr, s"$nodeName-stepKey")(stepKeyCursor)}
        |$I${cursor.callScope.scopeNull} = false;
        |$I${cursor.callScope.scopeVal} = $I$instance.$functionName( ${stepKeyCursor.callScope.scopeVal} );""".stripMargin
  }

}
