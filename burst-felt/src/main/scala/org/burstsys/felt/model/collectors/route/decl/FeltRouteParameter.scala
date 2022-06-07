/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

import scala.reflect.ClassTag

trait FeltRouteParameter extends FeltNode {

  final override val nodeName = "felt-route-parameter"

  /**
   * TODO
   *
   * @return
   */
  def parameterName: String

  /**
   * TODO
   *
   * @return
   */
  def value: FeltExpression

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // rules
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ value.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = value.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltRouteParameter = new FeltRouteParameter {
    sync(FeltRouteParameter.this)
    final override val parameterName: String = FeltRouteParameter.this.parameterName
    final override val value: FeltExpression = FeltRouteParameter.this.value.reduceStatics.resolveTypes
    final override val location: FeltLocation = FeltRouteParameter.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def canInferTypes: Boolean = value.canInferTypes

  override def resolveTypes: this.type = {
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = s"$S$parameterName = ${value.normalizedSource}"

}

