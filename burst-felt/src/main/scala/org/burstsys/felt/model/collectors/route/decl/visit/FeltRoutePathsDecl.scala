/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl.visit

import org.burstsys.felt.model.reference.FeltRefDecl
import org.burstsys.felt.model.reference.names.FeltNamedNode
import org.burstsys.felt.model.reference.path.{FeltPathExpr, FeltSimplePath}
import org.burstsys.felt.model.tree.code.FeltNoCode
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

import scala.reflect.ClassTag

trait FeltRoutePathsDecl extends FeltRefDecl with FeltNamedNode {

  final override val nodeName = "felt-route-paths-decl"

  final override lazy val nsName: String = "paths"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override lazy val refName: FeltPathExpr = FeltSimplePath(nameSpace.absoluteName)

  /**
   * supports dynamic step iteration
   *
   * @return
   */
  def stepsDecl: FeltRouteStepsDecl

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this) ++ stepsDecl.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = stepsDecl.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = stepsDecl.canInferTypes

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    stepsDecl.resolveTypes
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltRoutePathsDecl = new FeltRoutePathsDecl {
    final override val location: FeltLocation = FeltRoutePathsDecl.this.location
    final override val stepsDecl: FeltRouteStepsDecl = FeltRoutePathsDecl.this.stepsDecl.reduceStatics.resolveTypes
    sync(FeltRoutePathsDecl.this)
  }

  final
  def sync(node: FeltRoutePathsDecl): Unit = {
    super.sync(node)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def normalizedSource(implicit index: Int): String = FeltNoCode

}
