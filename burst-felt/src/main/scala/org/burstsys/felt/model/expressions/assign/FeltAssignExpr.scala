/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.assign

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.{BrioValueMapRelation, BrioValueScalarRelation, BrioValueVectorRelation}
import org.burstsys.felt.model.brio.FeltReachValidator
import org.burstsys.felt.model.expressions._
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.assertCanAssignOrUpdate

import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * assign (update) a LHS path from a RHS expression. The left hand side must be a [[FeltPathExpr]] that refers
 * to a [[org.burstsys.felt.model.reference.FeltRefDecl]] artifact. This '''path reference''' must have a
 * a [[org.burstsys.felt.model.reference.FeltReference]] installed
 *
 * __return value__
 * {{{
 *   The rule is to return the right-hand operand of = converted to the type of the lhs variable which is assigned to
 * }}}
 */
trait FeltAssignExpr extends FeltExpression with FeltReachValidator {

  final override val nodeName = "felt-assign-expr"

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
    validateReach(traversePt, lhs, relations.toIndexedSeq: _*)
    validateReach(traversePt, rhs, relations.toIndexedSeq: _*)
  }

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
  def reduceStatics: FeltAssignExpr = new FeltAssignExpr {
    sync(FeltAssignExpr.this)
    final override val lhs: FeltPathExpr = FeltAssignExpr.this.lhs.reduceStatics.resolveTypes
    final override val rhs: FeltExpression = FeltAssignExpr.this.rhs.reduceStatics.resolveTypes
    final override val location: FeltLocation = FeltAssignExpr.this.location
    feltType = FeltAssignExpr.this.feltType
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    val lhsSource = lhs.normalizedSource
    val rhsSource = rhs.normalizedSource
    s"""|$S$lhsSource = $rhsSource""".stripMargin
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {

    val rhsCursor = cursor indentRight 1 scopeDown
    val lhsRange = callerRangeDeclare(feltType, nodeName)(rhsCursor)
    val rhsCode = rhs.generateExpression(rhsCursor indentRight 1)
    val lhsCode = lhs.reference.get.generateReferenceAssign(rhsCursor)
    val rangeResolve = calleeRangeReturn(cursor, rhsCursor, nodeName)(rhsCursor)

    s"""|
        |${T(this)}
        |$I{
        |$lhsRange$rhsCode$lhsCode
        |$rangeResolve
        |$I}""".stripMargin
  }

}
