/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.assign

import org.burstsys.felt.model.expressions._
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, T}
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.assertCanAssignOrUpdate

import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * assign (update) a LHS path from a RHS expression. The left hand side must be a [[FeltPathExpr]] that refers
 * to a [[org.burstsys.felt.model.reference.FeltRefDecl]] artifact. This '''path reference''' must have a
 * a [[org.burstsys.felt.model.reference.FeltReference]] installed
 */
trait FeltUpdateExpr extends FeltExpression {

  final override val nodeName = "felt-update-expr"

  /**
   * the location to receive the update
   *
   * @return
   */
  def lhs: FeltPathExpr

  /**
   * the update data
   *
   * @return
   */
  def rhs: FeltExpression

  /**
   * what type of update is this?
   *
   * @return
   */
  def op: FeltUpdateOp

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ lhs.treeApply(rule) ++ rhs.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = Array(lhs, rhs)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  TYPE INFERENCE
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = lhs.canInferTypes && rhs.canInferTypes

  final override
  def resolveTypes: this.type = {
    if (!canInferTypes) return this
    lhs.resolveTypes
    feltType = lhs.feltType
    rhs.resolveTypes
    assertCanAssignOrUpdate(expression = this, lhs = lhs, rhs = rhs)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltUpdateExpr = new FeltUpdateExpr {
    sync(FeltUpdateExpr.this)
    final override val location: FeltLocation = FeltUpdateExpr.this.location
    final override val op: FeltUpdateOp = FeltUpdateExpr.this.op
    final override val lhs: FeltPathExpr = FeltUpdateExpr.this.lhs.reduceStatics.resolveTypes
    final override val rhs: FeltExpression = FeltUpdateExpr.this.rhs.reduceStatics.resolveTypes
    feltType = FeltUpdateExpr.this.feltType
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    val lhsSource = lhs.normalizedSource
    val rhsSource = rhs.normalizedSource
    s"""|$S$lhsSource ${op.normalizedSource} $rhsSource""".stripMargin
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    val rhsCursor = cursor scopeDown;
    val rhsCode = expressionEvaluate(rhs, s"$nodeName-rhs")(rhsCursor)
    s"""|
        |${T(this)}$rhsCode${lhs.reference.get.generateReferenceUpdate(op)(rhsCursor)}""".stripMargin
  }

}
