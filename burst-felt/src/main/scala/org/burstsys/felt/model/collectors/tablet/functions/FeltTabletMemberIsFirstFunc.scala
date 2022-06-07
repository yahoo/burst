/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.functions

import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletRef
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types._

import scala.language.postfixOps

object FeltTabletMemberIsFirstFunc {
  final val functionName: String = "tabletMemberIsFirst"
}

/**
 * add a value to a tablet
 */
trait FeltTabletMemberIsFirstFunc extends FeltTabletFunction with FeltBoolExpr {

  final override val nodeName = "felt-tablet-member-is-first-func"

  final val functionName: String = FeltTabletMemberIsFirstFunc.functionName

  final override val usage: String =
    s"""
       |usage:  $functionName(tablet_name) -> unit
       |    is this the first member of the tablet? (value only during a visit)""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    feltType = FeltType.boolean
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    // get route parameter
    val tablet = parameterAsReferenceOrThrow[FeltTabletRef](0, "tablet reference")
    val variable = s"$sweepRuntimeSym.${tablet.memberIsFirstVariable}"
    s"""|
        |${T(this)}
        |$I${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = $variable; """.stripMargin

  }

}
