/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu.datetime

import org.burstsys.felt.model.expressions.expressionEvaluate
import org.burstsys.felt.model.ginsu.FeltGinsuFunction
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, I2, T}
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps

trait FeltGinsuDatetimeFuncExpr extends FeltGinsuFunction {

  final override val usage: String =
    s"""
       |usage: $functionName(value_expr:long) -> long
     """.stripMargin

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

  final
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    val dateExpr = fixedParameter[Long](0, "step-key")
    val dateCursor = cursor indentRight 1 scopeDown

    s"""|
        |${T(this)}
        |${expressionEvaluate(dateExpr, s"$nodeName-stepKey")(dateCursor)}
        |${I}if(${dateCursor.callScope.scopeNull}) {
        |$I2${cursor.callScope.scopeNull} = true;
        |$I} else {
        |$I2${cursor.callScope.scopeVal} = $sweepRuntimeSym.$functionName(${dateCursor.callScope.scopeVal});
        |$I}""".stripMargin

  }


}
