/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl.graph

import org.burstsys.felt.model.collectors.route._
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.vitals.strings._

import scala.reflect.ClassTag


trait FeltRouteStep extends FeltNode {

  final override val nodeName = "felt-route-step"

  /**
   * TODO
   *
   * @return
   */
  def traits: Array[StepTrait]

  /**
   * TODO
   *
   * @return
   */
  def stepKey: FeltRouteStepKey

  /**
   * TODO
   *
   * @return
   */
  def tos: Array[FeltRouteStepTo]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // rules
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ tos.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = tos

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltRouteStep = new FeltRouteStep {
    sync(FeltRouteStep.this)
    override final val location: FeltLocation = FeltRouteStep.this.location
    override final val traits: Array[StepTrait] = FeltRouteStep.this.traits
    override final val stepKey: Int = FeltRouteStep.this.stepKey
    override final val tos: Array[FeltRouteStepTo] = FeltRouteStep.this.tos.map(_.reduceStatics.resolveTypes)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = tos.canInferTypes

  final override
  def resolveTypes: this.type = {
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S${traits.mkString(", ")} $stepKey {\n${tos.map(_.normalizedSource(index + 1).singleLineEnd).stringify.trimAtEnd}\n$S}"

}
