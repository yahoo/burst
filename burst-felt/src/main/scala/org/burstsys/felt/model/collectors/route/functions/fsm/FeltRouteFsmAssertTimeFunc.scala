/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.fsm

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions._
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types._

import scala.language.postfixOps


object FeltRouteFsmAssertTimeFunc {
  final val functionName: String = "routeFsmAssertTime"
}

/**
 * record a time (or new time) into the current step if the given step is the current step as well
 * a new step within the current scope. Return true if new time was applied
 */
trait FeltRouteFsmAssertTimeFunc extends FeltRouteFsmFunction with FeltBoolExpr {

  final override val nodeName = "felt-route-fsm-assert-time-func"

  final val functionName: String = FeltRouteFsmAssertTimeFunc.functionName

  final override val usage: String =
    s"""
       |usage:  $functionName(route, stepKey, stepTime) -> boolean
       |    record a time (or new time) into the current step if the given step is the current step as well
       |    a new step within the current scope. Return true if new time was applied""".stripMargin

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
    parameterCountIs(3)

    // get route parameter
    val route = parameterAsReferenceOrThrow[FeltRouteRef](0, "route reference")
    val instance = s"$sweepRuntimeSym.${route.instanceVariable}"

    // get integer step key parameter
    val stepKeyExpr = fixedParameter[Int](1, "step-key")
    val stepKeyCursor = cursor indentRight 1 scopeDown

    // get long step time parameter
    val stepTimeExpr = fixedParameter[Long](2, "step-time")
    val stepTimeCursor = cursor indentRight 1 scopeDown

    s"""|
        |${T(this)}
        |${expressionEvaluate(stepKeyExpr, s"$nodeName-stepKey")(stepKeyCursor)}
        |${expressionEvaluate(stepTimeExpr, s"$nodeName-stepTime")(stepTimeCursor)}
        |$I{ // resolve parameters and execute
        |${I2}if( ${stepKeyCursor.callScope.scopeNull}|| ${stepTimeCursor.callScope.scopeNull} ) {
        |$I3${cursor.callScope.scopeNull} = true;
        |$I2} else {
        |$I3${cursor.callScope.scopeNull} = false;
        |$I3${cursor.callScope.scopeVal} =
        |$I4$instance.$functionName( ${route.builderVariable}, ${stepKeyCursor.callScope.scopeVal}, ${stepTimeCursor.callScope.scopeVal} );
        |$I2}
        |$I}""".stripMargin
  }

}
