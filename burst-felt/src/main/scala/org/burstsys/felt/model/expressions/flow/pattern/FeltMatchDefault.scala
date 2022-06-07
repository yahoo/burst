/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.flow.pattern

import org.burstsys.felt.model.expressions.{FeltExprBlock, FeltExpression}
import org.burstsys.felt.model.tree._
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.felt.model.tree.source._
import org.burstsys.vitals.strings._

import scala.reflect.ClassTag


/**
 * the default case clause of a match expression
 */
trait FeltMatchDefault extends FeltExpression {

  final override val nodeName = "felt-match-default"

  /**
   * the expression block that is chosen if this part of the pattern case statements is the best match
   *
   * @return
   */
  def expressionBlock: FeltExprBlock

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ expressionBlock.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = Array(expressionBlock)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = expressionBlock.canInferTypes

  final override
  def resolveTypes: this.type = {
    expressionBlock.resolveTypes
    feltType = expressionBlock.feltType
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltMatchDefault = new FeltMatchDefault {
    sync(FeltMatchDefault.this)
    final override val expressionBlock: FeltExprBlock = FeltMatchDefault.this.expressionBlock.reduceStatics.resolveTypes
    final override val location: FeltLocation = FeltMatchDefault.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |${I}case _ ⇒ ${expressionBlock.generateExpression.singleLineEnd}""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"${S}case _ ⇒ ${expressionBlock.normalizedSource.singleLineEnd}"
}
