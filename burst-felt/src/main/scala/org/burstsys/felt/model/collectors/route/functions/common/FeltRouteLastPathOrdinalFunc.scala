/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions.common

import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef
import org.burstsys.felt.model.collectors.route.functions.fsm.FeltRouteFsmBackFillFunc
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I1, I2, T}
import org.burstsys.felt.model.types._

object FeltRouteLastPathOrdinalFunc {
  final val functionName: String = "routeLastPathOrdinal"
}

/**
 * returns a count of the full paths recorded in the route
 */
trait FeltRouteLastPathOrdinalFunc extends FeltRouteCommonFunction {

  final override val nodeName = "felt-route-last-path-ordinal-func"

  final val functionName: String = FeltRouteLastPathOrdinalFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> long
       |  returns the path ordinal for the last (committed) step recorded in the route (null if route is empty)""".stripMargin

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
