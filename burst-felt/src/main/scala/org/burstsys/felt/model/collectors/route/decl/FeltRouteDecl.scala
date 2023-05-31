/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl

import org.burstsys.felt.model.collectors.decl.FeltCollectorDecl
import org.burstsys.felt.model.collectors.route.decl.graph.FeltRouteGraph
import org.burstsys.felt.model.collectors.route.decl.visit.FeltRoutePathsDecl
import org.burstsys.felt.model.collectors.route.{FeltRouteBuilder, _}
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.strings._

import scala.reflect.ClassTag

/**
 * Felt declaration construct for route collectors
 */
trait FeltRouteDecl extends FeltCollectorDecl[FeltRouteRef, FeltRouteBuilder] {

  final override val nodeName: String = "felt-route-decl"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * TODO
   *
   * @return
   */
  def graph: FeltRouteGraph

  /**
   *
   * @return
   */
  def parameters: Array[FeltRouteParameter]

  /**
   * the routeName is the frameName
   *
   * @return
   */
  final
  def routeName: String = frame.frameName

  /**
   * the routeId is the frameId
   *
   * @return
   */
  final
  def routeId: Int = frame.frameId

  def pathsDecl: FeltRoutePathsDecl

  final override def reference: FeltRouteRef = refName.referenceGetOrThrow[FeltRouteRef]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PLANNING
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def createPlan: FeltRoutePlan =
    global.binding.collectors.routes.collectorPlan(this).initialize

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // rules
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ pathsDecl.treeApply(rule) ++ graph.treeApply(rule) ++ parameters.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] =
    pathsDecl.asArray ++ graph.asArray ++ parameters

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override def canInferTypes: Boolean = true

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltRouteDecl = new FeltRouteDecl {
    final override val location: FeltLocation = FeltRouteDecl.this.location
    final override val graph: FeltRouteGraph = FeltRouteDecl.this.graph.reduceStatics.resolveTypes
    final override val parameters: Array[FeltRouteParameter] = FeltRouteDecl.this.parameters.map(_.reduceStatics.resolveTypes)
    final override val pathsDecl: FeltRoutePathsDecl = FeltRouteDecl.this.pathsDecl
    this.sync(FeltRouteDecl.this)
  }

  final
  def sync(route: FeltRouteDecl): Unit = {
    super.sync(route)
    this.pathsDecl.sync(route.pathsDecl)
    this.pathsDecl.stepsDecl.sync(route.pathsDecl.stepsDecl)
  }


  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Helper
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def maxPartialPathsValue: Int = {
    parameters.find(_.parameterName == maxPartialPathsParameterName) match {
      case None => -1
      case Some(p) => p.value.reduceToFixAtom.getOrElse(throw VitalsException(s"$maxPartialPathsParameterName value not a fixed constant")).value.toInt
    }
  }

  final
  def maxCompletePathsValue: Int = {
    parameters.find(_.parameterName == maxCompletePathsParameterName) match {
      case None => -1
      case Some(p) => p.value.reduceToFixAtom.getOrElse(throw VitalsException(s"$maxCompletePathsParameterName value not a fixed constant")).value.toInt
    }
  }

  final
  def maxStepsValue: Int = {
    parameters.find(_.parameterName == maxStepsPerRouteParameterName) match {
      case None => -1
      case Some(p) => p.value.reduceToFixAtom.getOrElse(throw VitalsException(s"$maxStepsPerRouteParameterName value not a fixed constant")).value.toInt
    }
  }
  final
  def maxPathTimeValue: Int = {
    parameters.find(_.parameterName == maxPathTimeParameterName) match {
      case None => -1
      case Some(p) => p.value.reduceToFixAtom.getOrElse(throw VitalsException(s"$maxPathTimeParameterName value not a fixed constant")).value.toInt
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    val graphSource = if (graph == null) "" else graph.normalizedSource(index + 1)
    s"""|
        |${S}route {${printParameters(parameters)}$graphSource\n$S}""".stripMargin
  }

  private
  def printParameters(parameters: Array[FeltRouteParameter])(implicit index: Int): String = {
    if (parameters.nonEmpty)
      s"\n${parameters.map(_.normalizedSource(index + 1).singleLineEnd).stringify.singleLineEnd}"
    else ""
  }

}

