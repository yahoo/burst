/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.common

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.collectors.route.functions.fsm.FeltRouteFsmBackFillFunc
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types._

object FeltRouteLastPathIsCompleteFunc {
  final val functionName: String = "routeLastPathIsComplete"
}

/**
 * returns a count of the full paths recorded in the route
 */
trait FeltRouteLastPathIsCompleteFunc extends FeltRouteCommonFunction {

  final override val nodeName = "felt-route-last-path-is-full-func"

  final val functionName: String = FeltRouteLastPathIsCompleteFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> boolean
       |  returns true if the last (committed) step recorded in the route was a 'full' path (null if route is empty)""".stripMargin

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
    val backFillCall = FeltRouteFsmBackFillFunc.call(route)

    s"""|
        |${T(this)}
        |${I1}if( $instance.isEmpty ) {
        |$I2${cursor.callScope.scopeNull} = true;
        |$I1} else {
        |${I1}$instance.$backFillCall ;
        |$I2${cursor.callScope.scopeNull} = false;
        |$I2${cursor.callScope.scopeVal} = $instance.$functionName;
        |$I1}""".stripMargin

  }

}
