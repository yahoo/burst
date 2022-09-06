/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.math

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types._
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.op.FeltBinOpExpr
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.types.FeltType


/**
 * a value expression that executes an ''OPERATOR'' on a ''left hand side'' and a ''right hand side'' pair
 * of expressions and returns another value expression
 *
 * === JAVA VALUE PROMOTION RULES ===
 * ''All floating point values (float and double) in an arithmetic operation (+, −, *, /) are converted to double
 * type before the arithmetic operation in performed.
 * All integer values (byte, short and int) in an arithmetic operations (+, −, *, /, %) are converted to int type
 * before the arithmetic operation in performed.
 * However, if one of the values in an arithmetic operation (+, −, *, /, %) is long, then all values are converted
 * to long type before the arithmetic operation in performed.'' */
trait FeltBinMathExpr extends FeltMathExpr with FeltBinOpExpr {

  final override val nodeName = "felt-bin-math-expr"

  /**
   * the binary operator
   *
   * @return
   */
  def op: FeltBinMathOp

  /**
   * the ''left hand side'' expression
   *
   * @return
   */
  def lhs: FeltExpression

  /**
   * the ''right hand side'' expression
   *
   * @return
   */
  def rhs: FeltExpression

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  validation
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * validate references for a given traversal point
   *
   * @param traversePt
   */
  override final
  def validateBrioReferences(traversePt: BrioNode): Unit = {
    val relations = Array(BrioValueScalarRelation, BrioValueMapRelation, BrioValueVectorRelation, BrioReferenceScalarRelation, BrioReferenceVectorRelation)
    validateReach(traversePt, lhs, relations.toIndexedSeq: _*)
    validateReach(traversePt, rhs, relations.toIndexedSeq: _*
    )
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = lhs.canInferTypes && rhs.canInferTypes

  final override
  def resolveTypes: this.type = {
    if (!canInferTypes) return this
    lhs.resolveTypes
    rhs.resolveTypes
    feltType = FeltType.combine(lhs.feltType, rhs.feltType)
    op.feltType = feltType
    this
  }


  final override
  def reduceToLiteral: Option[FeltLiteral] = op.reduceToLiteral(lhs.reduceToLiteral, rhs.reduceToLiteral)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExpression = {
    reduceToLiteral match {
      case Some(literal) => literal
      case None =>
        new FeltBinMathExpr {
          sync(FeltBinMathExpr.this)
          final override val op: FeltBinMathOp = FeltBinMathExpr.this.op
          final override val lhs: FeltExpression = FeltBinMathExpr.this.lhs.reduceStatics.reduceToLiteral match {
            case Some(lit) => lit
            case None => FeltBinMathExpr.this.lhs.reduceStatics.resolveTypes
          }
          final override val rhs: FeltExpression = FeltBinMathExpr.this.rhs.reduceStatics.reduceToLiteral match {
            case Some(lit) => lit
            case None => FeltBinMathExpr.this.rhs.reduceStatics.resolveTypes
          }
          final override val location: FeltLocation = FeltBinMathExpr.this.location
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
