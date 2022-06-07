/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.bool

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.op.FeltBinOpExpr
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps

/**
 * a boolean expression composed of a ''left hand expression'' OP ''right hand expression'' tuple
 */
trait FeltBinBoolExpr extends FeltBoolExpr with FeltBinOpExpr {

  final override val nodeName = "felt-bin-bool-expr"

  /**
   * the operator to apply to the right and left hand side expressions
   *
   * @return
   */
  def op: FeltBinBoolOp

  /**
   * the left hand side expression
   *
   * @return
   */
  def lhs: FeltExpression

  /**
   * the right hand side expression
   *
   * @return
   */
  def rhs: FeltExpression

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  final override
  def canInferTypes: Boolean = lhs.canInferTypes && rhs.canInferTypes

  final override
  def resolveTypes: this.type = {
    lhs.resolveTypes
    rhs.resolveTypes
    feltType = FeltType.combine(lhs.feltType, rhs.feltType)
    op.feltType = feltType
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceToLiteral: Option[FeltLiteral] = {
    op.reduceToNull(lhs.reduceToNull, rhs.reduceToNull) match {
      case None =>
      case Some(c) => return Some(c)
    }
    op.reduceToLiteral(lhs.reduceStatics.reduceToBoolAtom, rhs.reduceStatics.reduceToBoolAtom)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExpression = {
    // if either side if null, return a null
    op.reduceToNull(lhs.reduceToLiteral, rhs.reduceToLiteral) match {
      case None =>
      case Some(nl) =>
        return nl
    }
    op.reduceToLiteral(lhs.reduceToBoolAtom, rhs.reduceToBoolAtom) match {
      case Some(bl) => bl
      case None => new FeltBinBoolExpr {
        sync(FeltBinBoolExpr.this)
        final override val op: FeltBinBoolOp = FeltBinBoolExpr.this.op
        final override val lhs: FeltExpression = FeltBinBoolExpr.this.lhs.reduceStatics.resolveTypes
        final override val rhs: FeltExpression = FeltBinBoolExpr.this.rhs.reduceStatics.resolveTypes
        final override val location: FeltLocation = FeltBinBoolExpr.this.location
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode =
    scopeBinaryOp(nodeName)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"${lhs.normalizedSource} ${op.symbol} ${rhs.normalizedSource}"

}
