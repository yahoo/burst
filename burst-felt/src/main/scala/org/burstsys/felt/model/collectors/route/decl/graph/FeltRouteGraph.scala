/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl.graph

import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.vitals.strings._

import scala.reflect.ClassTag

trait FeltRouteGraph extends FeltNode {

  final override val nodeName = "felt-route-graph"

  /**
   * TODO
   *
   * @return
   */
  def steps: Array[FeltRouteStep]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ steps.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = steps

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltRouteGraph = new FeltRouteGraph {
    sync(FeltRouteGraph.this)
    final override val location: FeltLocation = FeltRouteGraph.this.location
    final override val steps: Array[FeltRouteStep] = FeltRouteGraph.this.steps.map(_.reduceStatics.resolveTypes)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = steps.canInferTypes

  final override
  def resolveTypes: this.type = {
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"""
       |${S}graph {\n${steps.map(_.normalizedSource(index + 1).singleLineEnd).stringify.singleLineEnd}$S}""".stripMargin

}
