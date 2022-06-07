/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.poly.functions

import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.poly.FeltPolyRefFuncGen
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.types.FeltType


object FeltPolyContainsFunc {
  final val functionName: String = "contains"
}

/**
 * a function that returns a boolean/logical datatype if the first parameter can be said to ''contain''
 * the second parameter. The exact semantics of this depend on the nature/type of the first parameter e.g. is it
 * a brio data model relationship, a literal, or the result of more complex expression
 */
trait FeltPolyContainsFunc extends FeltFuncExpr with FeltBoolExpr {

  final override val nodeName = "felt-poly-contains-func"

  final override val usage: String =
    s"""
       |usage: $functionName(<reference_path>, value_expr {, value_expr}* ):boolean
     """.stripMargin

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
    parameterCountAtLeast(1)
    val polyDispatch = parameterAsReferenceOrThrow[FeltPolyRefFuncGen](0, nodeName)
    polyDispatch.generateContainsPolyFunc(this)
  }

}
