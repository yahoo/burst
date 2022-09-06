/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.flow.pattern

import org.burstsys.felt.model.expressions.{FeltExprBlock, FeltExpression}
import org.burstsys.felt.model.tree._
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.felt.model.tree.source._
import org.burstsys.vitals.strings._

import scala.reflect.ClassTag


/**
 * A value expression that is comprised of a set of match and case clauses
 */
trait FeltMatchCase extends FeltExpression {

  final override val nodeName = "felt-match-case"

  /**
   * the expression to be pattern matched
   *
   * @return
   */
  def expression: FeltExpression

  /**
   * the lexical expression scope to be executed
   *
   * @return
   */
  def expressionBlock: FeltExprBlock

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ expression.treeApply(rule) ++ expressionBlock.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = Array(expression, expressionBlock)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  final override
  def canInferTypes: Boolean = expression.canInferTypes && expressionBlock.canInferTypes

  final override
  def resolveTypes: this.type = {
    expression.resolveTypes
    expressionBlock.resolveTypes
    feltType = expressionBlock.feltType
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltMatchCase = new FeltMatchCase {
    sync(FeltMatchCase.this)
    final override val expression: FeltExpression = FeltMatchCase.this.expression.reduceStatics.resolveTypes
    final override val expressionBlock: FeltExprBlock = FeltMatchCase.this.expressionBlock.reduceStatics.resolveTypes
    final override val location: FeltLocation = FeltMatchCase.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |${I}case ${expression.generateExpression} => ${expressionBlock.generateExpression.singleLineEnd}""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"${S}case ${expression.normalizedSource} => ${expressionBlock.normalizedSource.singleLineEnd}"

}
