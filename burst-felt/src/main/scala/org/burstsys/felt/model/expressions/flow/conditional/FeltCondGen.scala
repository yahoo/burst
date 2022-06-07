/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.flow.conditional

import org.burstsys.felt.model.expressions._
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.felt.model.types.FeltType
import org.burstsys.vitals.strings._

import scala.language.postfixOps


/**
 * code generation semantics for [[FeltCondExpr]]
 */
trait FeltCondGen {

  self: FeltCondExpr =>

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${T(this)}$I${ifCode(ifConditionTest, ifExpressionBlock)}$elseIfCode$elseCode""".stripMargin
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // internals
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final
  def ifCode(ifCondition: FeltBoolExpr, ifExpression: FeltExprBlock)(implicit cursor: FeltCodeCursor): FeltCode = {
    val ifConditionCursor = cursor scopeDown
    val ifConditionScope = ifConditionCursor.callScope
    val ifExprCursor = cursor indentRight 1 scopeDown
    val ifExprCode = ifExpression.generateExpression(ifExprCursor)
    val ifExprRange = callerRangeDeclare(ifExpression.feltType, s"$nodeName-if-expr")(ifExprCursor)
    s"""|
        |${I}if ( {
        |${callerRangeDeclare(FeltType.boolean, nodeName)(ifConditionCursor indentRight 2)}${ifCondition.generateExpression(ifConditionCursor indentRight 4)}
        |$I3!${ifConditionScope.scopeNull} && ${ifConditionScope.scopeVal}
        |$I2} ) {
        |$ifExprRange$ifExprCode
        |$I}""".stripMargin
  }

  private final
  def elseIfCode(implicit cursor: FeltCodeCursor): FeltCode = {
    elseIfConditionTest.zip(elseIfExpressionBlock).map {
      case (condition, expression) =>
        s"${I}else ${ifCode(condition, expression)}"
    }.stringify.singleLineEnd
  }

  private final
  def elseCode(implicit cursor: FeltCodeCursor): FeltCode = {
    elseExpressionBlock.map {
      expr =>
        val elseExprCursor = cursor indentRight 1 scopeDown
        val elseExprCode = elseExpressionBlock.get.generateExpression(elseExprCursor)
        val elseExprRange = callerRangeDeclare(expr.feltType, s"$nodeName-else-expr")(elseExprCursor)
        s"""|${I}else {
            |$elseExprRange$elseExprCode
            |$I}
       """.stripMargin
    }.toArray.stringify.trimAtEnd
  }

}
