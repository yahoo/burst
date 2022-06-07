/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.bool

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.op.FeltUnOpExpr
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}


/**
 * an expression that returns a boolean/logical value from applying a UNARY OPERATOR to a single expression
 */
trait FeltUnaryBoolExpr extends FeltBoolExpr with FeltUnOpExpr {

  final override val nodeName = "felt-unary-bool-expr"

  /**
   * the unary operator
   *
   * @return
   */
  def op: FeltUnaryBoolOp

  /**
   * the expression to apply the operator to
   *
   * @return
   */
  def rhs: FeltExpression

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = rhs.canInferTypes

  final override
  def resolveTypes: this.type = {
    rhs.resolveTypes
    feltType = rhs.feltType
    op.feltType = feltType
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceToLiteral: Option[FeltLiteral] = op.reduceToPrimLit(rhs.reduceToBoolAtom)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltBoolExpr = {
    reduceToBoolAtom match {
      case Some(l) =>
        l
      case None =>
        new FeltUnaryBoolExpr {
          sync(FeltUnaryBoolExpr.this)
          final override val op: FeltUnaryBoolOp = FeltUnaryBoolExpr.this.op
          final override val rhs: FeltExpression = FeltUnaryBoolExpr.this.rhs.reduceStatics.resolveTypes
          final override val location: FeltLocation = FeltUnaryBoolExpr.this.location
        }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode =
    scopeUnaryOp(nodeName)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"${op.symbol} ${rhs.normalizedSource}"

}
