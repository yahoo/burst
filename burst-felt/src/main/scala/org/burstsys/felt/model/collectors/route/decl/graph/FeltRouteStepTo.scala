/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl.graph

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.route.{FeltRouteForever, FeltRouteStepKey, FeltRouteStepTime}
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

import scala.reflect.ClassTag


trait FeltRouteStepTo extends FeltNode {

  final override val nodeName = "felt-route-step"

  /**
   * TODO
   *
   * @return
   */
  def stepKey: FeltExpression

  /**
   * TODO
   *
   * @return
   */
  def minTime: Option[FeltExpression]

  /**
   * TODO
   *
   * @return
   */
  def maxTime: Option[FeltExpression]

  final
  def reducedStepKey: FeltRouteStepKey = {
    stepKey.reduceToFixAtom.getOrElse(
      throw FeltException(location, s"stepKey in route did not reduce to a static fixed atom")
    ).value.toInt
  }

  final
  def reducedMaxTime: FeltRouteStepTime = {
    maxTime match {
      case None => FeltRouteForever
      case Some(mTime) =>
        mTime.reduceToFixAtom.getOrElse(
          throw FeltException(location, s"maxTime in route did not reduce to a static fixed atom")
        ).value

    }
  }

  final
  def reducedMinTime: FeltRouteStepTime = {
    minTime match {
      case None => FeltRouteForever
      case Some(mTime) =>
        mTime.reduceToFixAtom.getOrElse(
          throw FeltException(location, s"minTime in route did not reduce to a static fixed atom")
        ).value

    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // rules
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ stepKey.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = stepKey.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltRouteStepTo = new FeltRouteStepTo {
    sync(FeltRouteStepTo.this)
    final override val location: FeltLocation = FeltRouteStepTo.this.location
    final override val stepKey: FeltExpression = FeltRouteStepTo.this.stepKey.reduceStatics.resolveTypes
    final override val minTime: Option[FeltExpression] = if (FeltRouteStepTo.this.minTime.isEmpty) None else Some(FeltRouteStepTo.this.minTime.get.reduceStatics.resolveTypes)
    final override val maxTime: Option[FeltExpression] = if (FeltRouteStepTo.this.maxTime.isEmpty) None else Some(FeltRouteStepTo.this.maxTime.get.reduceStatics.resolveTypes)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = stepKey.canInferTypes && minTime.canInferTypes && maxTime.canInferTypes

  final override
  def resolveTypes: this.type = {
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    val minTimeSource = if (minTime.nonEmpty) minTime.get.normalizedSource else FeltRouteForever
    val maxTimeSource = if (maxTime.nonEmpty) maxTime.get.normalizedSource else FeltRouteForever
    s"${S}to(${stepKey.normalizedSource}, $minTimeSource, $maxTimeSource)"
  }


}
