/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.flow.pattern

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.flow.FeltFlowExpr
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.FeltType
import org.burstsys.vitals.strings._

import scala.language.postfixOps
import scala.reflect.ClassTag


/**
 * a value expression that is comprised of a match with a set of sub case clauses
 */
trait FeltMatchExpr extends FeltFlowExpr with FeltMatchGen {

  final override val nodeName = "felt-match-expr"

  /**
   * the path (should this be a value expression?) that is to be 'matched'
   *
   * @return
   */
  def path: FeltPathExpr

  /**
   * set of possible match pattern cases
   *
   * @return
   */
  def cases: Array[FeltMatchCase]

  /**
   * optional 'default' no pattern match
   *
   * @return
   */
  def default: Option[FeltMatchDefault]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ path.treeApply(rule) ++ cases.treeApply(rule) ++ default.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = Array(path) ++ default ++ cases

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = path.canInferTypes && cases.canInferTypes && default.canInferTypes

  final override
  def resolveTypes: this.type = {
    path.resolveTypes
    cases.foreach(_.resolveTypes)
    default.foreach(_.resolveTypes)
    feltType = FeltType.combine(cases.map(_.feltType) ++ default.map(_.feltType): _*)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltMatchExpr = new FeltMatchExpr {
    sync(FeltMatchExpr.this)
    final override val path: FeltPathExpr = FeltMatchExpr.this.path.reduceStatics.resolveTypes
    final override val cases: Array[FeltMatchCase] = FeltMatchExpr.this.cases.map(_.reduceStatics.resolveTypes.asInstanceOf[FeltMatchCase])
    final override val default: Option[FeltMatchDefault] = FeltMatchExpr.this.default match {
      case None => None
      case Some(e) => Some(e.reduceStatics.resolveTypes)
    }
    final override val location: FeltLocation = FeltMatchExpr.this.location

  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S${path.normalizedSource} match {${printCases(cases)(index + 1)}${printDefault(default)(index + 1)}$S}"

  private
  def printCases(cases: Array[FeltMatchCase])(implicit index: Int): String = {
    if (cases.nonEmpty)
      s"\n${cases.map(_.normalizedSource).stringify.singleLineEnd}"
    else ""
  }

  private
  def printDefault(default: Option[FeltMatchDefault])(implicit index: Int): String = {
    default match {
      case None => ""
      case Some(e) => s"${e.normalizedSource.singleLineEnd}"
    }
  }

}
