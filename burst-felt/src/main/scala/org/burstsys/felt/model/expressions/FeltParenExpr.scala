/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions

import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.literals.primitive.FeltNullPrimitive
import org.burstsys.felt.model.tree._
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

import scala.reflect.ClassTag

/**
 * an expression enclosed in matching parens
 */
trait FeltParenExpr extends FeltBoolExpr {

  final override val nodeName = "felt-paren-expr"

  /**
   * the parenthesized expression
   *
   * @return
   */
  def expression: FeltExpression

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ expression.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = expression.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceToLiteral: Option[FeltLiteral] = expression.reduceToLiteral

  final override
  def reduceToNull: Option[FeltNullPrimitive] = expression.reduceToNull

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExpression =
    reduceToLiteral match {
      case Some(l) => l
      case None => new FeltParenExpr {
        sync(FeltParenExpr.this)
        final override val expression: FeltExpression = FeltParenExpr.this.expression.reduceStatics.resolveTypes
        final override val location: FeltLocation = FeltParenExpr.this.location
      }
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = expression.canInferTypes

  final override
  def resolveTypes: this.type = {
    expression.resolveTypes
    feltType = expression.feltType
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = s"${expression.generateExpression}"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = s"(${expression.normalizedSource})"

}
