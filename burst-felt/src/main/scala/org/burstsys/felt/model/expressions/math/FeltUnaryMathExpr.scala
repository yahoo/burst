/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.math

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types._
import org.burstsys.felt.model.brio.FeltReachValidator
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.op.FeltUnOpExpr
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}


/**
 * an expression that takes an ''OPERATOR'' and applies it to a right hand side expression
 */
trait FeltUnaryMathExpr extends FeltMathExpr with FeltUnOpExpr  {

  final override val nodeName = "felt-unary-math-expr"

  /**
   * the unary ''OPERATOR''
   *
   * @return
   */
  def op: FeltUnaryMathOp

  /**
   * the right hand side expression
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
    val relations = Array(BrioValueScalarRelation, BrioValueMapRelation, BrioValueVectorRelation)
    validateReach(traversePt, rhs, relations.toIndexedSeq: _*)
  }

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


  final override
  def reduceToLiteral: Option[FeltLiteral] =
    op.reduceToPrimLit(rhs.reduceToLiteral)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExpression = {
    op.reduceToPrimLit(rhs.reduceToLiteral) match {
      case Some(l) => l
      case None =>
        new FeltUnaryMathExpr {
          sync(FeltUnaryMathExpr.this)
          final override val op: FeltUnaryMathOp = FeltUnaryMathExpr.this.op
          final override val rhs: FeltExpression = FeltUnaryMathExpr.this.rhs.reduceStatics.resolveTypes
          final override val location: FeltLocation = FeltUnaryMathExpr.this.location
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
    s"${op.symbol}${rhs.normalizedSource}"
}
