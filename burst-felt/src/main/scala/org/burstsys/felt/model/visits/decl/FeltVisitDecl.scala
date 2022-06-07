/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits.decl

import org.burstsys.felt.model.FeltDeclaration
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.types.FeltType
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl

import scala.reflect.ClassTag

/**
 * Abstract type for a ''visit'' within a [[FeltFrameDecl]] within an [[FeltAnalysisDecl]]
 * These can be [[FeltStaticVisitDecl]] or [[FeltDynamicVisitDecl]]
 */
trait FeltVisitDecl extends FeltDeclaration {

  /**
   * where in the schema/lattice traversal this visit occurs at - this is for both
   * static and dynamic visits
   *
   * @return
   */
  def traverseTarget: FeltPathExpr

  /**
   * the local scoped variables within this visit
   *
   * @return
   */
  def variables: Array[FeltGlobVarDecl]

  /**
   * the set of actions within this visit e.g. pre, post etc
   *
   * @return
   */
  def actions: Array[FeltActionDecl]

  /**
   * if provided, this ordinal should statically reduce to a fixed number
   * that is used to determine 'splicing' order so as to allow the Felt analysis
   * semantics order when equivalent visits are done if there is more than
   * one provded across the analysis (all frames). If not provided then ordering
   * is assumed to be not required. Static visits default to ordinal zero
   * and before dynamic visits. Dynamic visits
   *
   * @return
   */
  def ordinalExpression: Option[FeltExpression]

  lazy val ordinal: Int = ordinalExpression match {
    case None => 0
    case Some(e) => e.resolveTypes.reduceStatics.reduceToFixAtomOrThrow.value.toInt
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltVisitDecl = this

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ traverseTarget.treeApply(rule) ++ variables.treeApply(rule) ++ actions.treeApply(rule)

  override
  def children: Array[_ <: FeltNode] = traverseTarget.asArray ++ variables ++ actions

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def canInferTypes: Boolean = variables.canInferTypes && traverseTarget.canInferTypes && actions.canInferTypes

  override
  def resolveTypes: this.type = {
    feltType = FeltType.unit
    this
  }

}
