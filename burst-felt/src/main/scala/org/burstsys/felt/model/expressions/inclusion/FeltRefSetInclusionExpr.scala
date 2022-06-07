/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.inclusion

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree._
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.types.FeltType

import scala.reflect.ClassTag

/**
 * an expression determining if a source value is in the set defined by the memberPath
 * ===OPTIMIZATIONS===
 * <ol>
 * <li>if the member path refers to data that is or can be sorted, and the size is large enough,
 * then a binary search should be implemented</li>
 * </ol>
 */
trait FeltRefSetInclusionExpr extends FeltInclusionExpr {

  final override val nodeName = "felt-ref-set-inclusion-expr"

  /**
   * the source data to test for inclusion
   *
   * @return
   */
  def source: FeltPathExpr

  /**
   * the path referring to set members to test against
   *
   * @return
   */
  def memberPath: FeltPathExpr

  /**
   * is this expression inverted?
   *
   * @return
   */
  def invert: Boolean

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ source.treeApply(rule) ++ memberPath.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = Array(source, memberPath)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExpression =
    reduceToLiteral match {
      case Some(l) => l
      case None => new FeltRefSetInclusionExpr {
        sync(FeltRefSetInclusionExpr.this)
        final override val invert: Boolean = FeltRefSetInclusionExpr.this.invert
        final override val source: FeltPathExpr = FeltRefSetInclusionExpr.this.source.reduceStatics.resolveTypes
        final override val memberPath: FeltPathExpr = FeltRefSetInclusionExpr.this.memberPath.reduceStatics.resolveTypes
        final override val location: FeltLocation = FeltRefSetInclusionExpr.this.location
      }
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def canInferTypes: Boolean = memberPath.canInferTypes

  final override
  def resolveTypes: this.type = {
    source.resolveTypes
    memberPath.resolveTypes
    feltType = FeltType.valScal[Boolean]
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceToLiteral: Option[FeltLiteral] = None

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"${source.fullPath} ${if (invert) "not" else ""} in ${source.fullPath}"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |//??? // get going bub!""".stripMargin

}
