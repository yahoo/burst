/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.functions

import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}

object FeltTabletMemberValueFunc {
  final val functionName: String = "tabletMemberValue"
}

/**
 * current path ordinal (valid only in a  path or step visit)
 */
trait FeltTabletMemberValueFunc extends FeltTabletFunction {

  final override val nodeName = "felt-tablet-member-value-func"

  final val functionName: String = FeltTabletMemberValueFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(route) -> long
       |  current tablet member value (valid only in a  path or step visit)""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    parameterCountIs(1)
    parameterAsReference[FeltTabletRef](0, "tablet reference") match {
      case None =>
      case Some(r) => feltType = r.refDecl.feltType
    }
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    // get tablet parameter
    val tablet = parameterAsReferenceOrThrow[FeltTabletRef](0, "tablet reference")
    val memberValue = s"$sweepRuntimeSym.${tablet.memberValueVariable}"
    s"""|
        |${T(this)}
        |$I${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = $memberValue ; """.stripMargin
  }

}
