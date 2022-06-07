/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.method

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

import scala.reflect.ClassTag


/**
 * an expression that defined the exit from a method with an optional typed value
 */
trait FeltReturnExpr extends FeltExpression {

  final override val nodeName = "felt-return-expr"

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
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = expression.canInferTypes

  final override
  def resolveTypes: this.type = {
    expression.feltType
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltReturnExpr = new FeltReturnExpr {
    sync(FeltReturnExpr.this)
    final override val expression: FeltExpression = FeltReturnExpr.this.expression.reduceStatics.resolveTypes
    final override val location: FeltLocation = FeltReturnExpr.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"""${S}return ${expression.normalizedSource}""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = ???

}
