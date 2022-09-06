/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits.decl

import org.burstsys.felt.model.FeltDeclaration
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.expressions.FeltExprBlock
import org.burstsys.felt.model.sweep.FeltSweep
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.FeltType

import scala.reflect.ClassTag

/**
 * An [[FeltActionDecl]] within a [[FeltStaticVisitDecl]] within a [[FeltFrameDecl]] within an [[FeltAnalysisDecl]]
 * This is where multiple ''programmed'' (from the source of the presented ''analysis'')
 * [[FeltExprBlock]] source units can be executed at various points
 * within a code generated [[FeltSweep]] scan/traversal.
 * <p/>
 * '''NOTE:''' this is the only ''user'' code is taken from the source of the analysis and spliced
 * into the code generation.
 */
trait FeltActionDecl extends FeltDeclaration {

  final override val nodeName = "felt-action-decl"

  /**
   * The set of expressions within action
   *
   * @return
   */
  def expressionBlock: FeltExprBlock

  /**
   * the type of action e.g. post, situ
   *
   * @return
   */
  def actionType: FeltActionType

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ expressionBlock.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = expressionBlock.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = expressionBlock.canInferTypes

  final override
  def resolveTypes: this.type = {
    expressionBlock.resolveTypes
    feltType = FeltType.unit
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltActionDecl = new FeltActionDecl {
    sync(FeltActionDecl.this)
    final override val expressionBlock: FeltExprBlock = FeltActionDecl.this.expressionBlock.reduceStatics.resolveTypes
    final override val actionType: FeltActionType = FeltActionDecl.this.actionType
    final override val location: FeltLocation = FeltActionDecl.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S$actionType => ${expressionBlock.normalizedSource}"

}
