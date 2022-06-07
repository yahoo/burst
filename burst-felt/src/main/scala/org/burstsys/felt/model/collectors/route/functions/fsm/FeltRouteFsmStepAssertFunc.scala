/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.fsm

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.expressions._
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types._

import scala.language.postfixOps

object FeltRouteFsmStepAssertFunc {

  final val functionName: String = "routeFsmStepAssert"

}

/**
 * assert a possible map state transition to a stepKey on a route with a stepTime
 * tagged with a stepId. return true if the transition was successful
 */
trait FeltRouteFsmStepAssertFunc extends FeltRouteFsmFunction with FeltBoolExpr {

  final override val nodeName = "felt-route-fsm-assert-step-func"

  final val functionName: String = FeltRouteFsmStepAssertFunc.functionName

  final override val usage: String =
    s"""
       |usage:  $functionName(route, stepKey, stepTag, stepTime) -> boolean
       |    assert a possible map state transition to a stepKey on a route with a stepTime
       |    tagged with a stepTag. return true if the transition was successful""".stripMargin

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
    parameterCountIs(4)

    // get route parameter
    val route = parameterAsReferenceOrThrow[FeltRouteRef](0, "route reference")
    val instance = s"$sweepRuntimeSym.${route.instanceVariable}"

    // get stepkey parameter
    val stepKeyExpr = fixedParameter[Int](1, "step-key")
    val stepKeyCursor = cursor scopeDown

    // get step id parameter
    val stepIdExpr = fixedParameter[Int](2, "step-id")
    val stepIdCursor = cursor scopeDown

    // get step time parameter
    val stepTimeExpr = fixedParameter[Long](3, "step-time")
    val stepTimeCursor = cursor scopeDown


    s"""|
        |${T(this)}
        |${expressionEvaluate(stepKeyExpr, s"$nodeName-stepKey")(stepKeyCursor)}
        |${expressionEvaluate(stepIdExpr, s"$nodeName-stepId")(stepIdCursor)}
        |${expressionEvaluate(stepTimeExpr, s"$nodeName-stepTime")(stepTimeCursor)}
        |$I{ // test for null parameters execute
        |${I2}if( ${stepKeyCursor.callScope.scopeNull}|| ${stepIdCursor.callScope.scopeNull}|| ${stepTimeCursor.callScope.scopeNull} ) {
        |$I3${cursor.callScope.scopeNull} = true;
        |$I2} else {
        |$I3${cursor.callScope.scopeNull} = false;
        |$I3${cursor.callScope.scopeVal} =
        |$I4$instance.$functionName( ${route.builderVariable}, ${stepKeyCursor.callScope.scopeVal}, ${stepIdCursor.callScope.scopeVal}, ${stepTimeCursor.callScope.scopeVal} );
        |$I2}
        |$I}""".stripMargin
  }

}
