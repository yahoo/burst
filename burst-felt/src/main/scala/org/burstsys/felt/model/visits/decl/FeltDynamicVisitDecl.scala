/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits.decl

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.code.FeltNoCode
import org.burstsys.felt.model.tree.source.{S, SL}
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl

import scala.reflect.ClassTag

/**
 * A dynamic ''visit'' within a [[FeltFrameDecl]] within an [[FeltAnalysisDecl]]
 */
trait FeltDynamicVisitDecl extends FeltVisitDecl {

  final override val nodeName = "felt-dynamic-visit-decl"

  /**
   * the collector for this visit - must be a valid reference to a collector (frame)
   *
   * @return
   */
  def visitedCollector: FeltPathExpr

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    super.treeApply(rule) ++ visitedCollector.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = super.children ++ visitedCollector.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = super.canInferTypes && visitedCollector.canInferTypes

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    visitedCollector.resolveTypes
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltDynamicVisitDecl = new FeltDynamicVisitDecl {
    sync(FeltDynamicVisitDecl.this)
    final override val visitedCollector: FeltPathExpr =
      FeltDynamicVisitDecl.this.visitedCollector.reduceStatics.resolveTypes
    final override val traverseTarget: FeltPathExpr = FeltDynamicVisitDecl.this.traverseTarget.reduceStatics.resolveTypes
    final override val variables: Array[FeltGlobVarDecl] =
      FeltDynamicVisitDecl.this.variables.map(_.reduceStatics.resolveTypes)
    final override val actions: Array[FeltActionDecl] =
      FeltDynamicVisitDecl.this.actions.map(_.reduceStatics.resolveTypes)
    final override val location: FeltLocation = FeltDynamicVisitDecl.this.location
    final override val ordinalExpression: Option[FeltExpression] = {
      if (FeltDynamicVisitDecl.this.ordinalExpression.isEmpty)
        None
      else
        Some(FeltDynamicVisitDecl.this.ordinalExpression.get.reduceStatics.resolveTypes)
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    val ordinalCode = if (ordinalExpression.isEmpty) FeltNoCode else s"($ordinal)"
    s"""|$S${visitedCollector.normalizedSource} ${traverseTarget.normalizedSource} $ordinalCode ⇒ {${SL(variables)}${SL(actions)}
        |$S}""".stripMargin
  }

}
