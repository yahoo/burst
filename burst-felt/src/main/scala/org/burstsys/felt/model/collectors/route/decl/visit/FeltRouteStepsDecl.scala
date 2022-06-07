/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl.visit

import org.burstsys.felt.model.reference.FeltRefDecl
import org.burstsys.felt.model.reference.names.FeltNamedNode
import org.burstsys.felt.model.reference.path.{FeltPathExpr, FeltSimplePath}
import org.burstsys.felt.model.tree.code.FeltNoCode
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.visits.decl.FeltVisitDecl

import scala.reflect.ClassTag

/**
 * a virtual part of a [[FeltVisitDecl]] that provides a referenceable step by step
 */
trait FeltRouteStepsDecl extends FeltRefDecl with FeltNamedNode {

  final override val nodeName = "felt-route-steps-decl"

  final override lazy val nsName: String = "steps"

  final override lazy val refName: FeltPathExpr = FeltSimplePath(nameSpace.absoluteName)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = true

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltRouteStepsDecl = new FeltRouteStepsDecl {
    sync(FeltRouteStepsDecl.this)
    final override val location: FeltLocation = FeltRouteStepsDecl.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def normalizedSource(implicit index: Int): String = FeltNoCode

}
