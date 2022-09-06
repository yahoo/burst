/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.generate

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.route._
import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.route.decl.graph.{FeltRouteEdge, FeltRouteGraph, FeltRouteTransition}
import org.burstsys.felt.model.tree.code.{C2, FeltCode, FeltCodeCursor, I, I2, generateIntArrayCode}

import scala.collection.mutable

final case
class FeltRouteBuilderGenerator(route: FeltRouteDecl) {

  def graph: FeltRouteGraph = route.graph

  val binding: FeltRouteProvider = route.global.binding.collectors.routes

  private[this]
  var _builder: FeltRouteBuilder = _

  def initialize(): Unit = {

    type RouteStepArray = mutable.ArrayBuilder.ofInt

    val routeSteps = graph.steps
    val stepKeys = routeSteps.map(_.stepKey)
    val maxStepKey = stepKeys.max

    if (maxStepKey > MaxStepsInGraph)
      throw FeltException(graph.location, s"max steps per graph exceeded")

    val transitions = new Array[FeltRouteTransition](maxStepKey) // 8-3463000030969}.

    val entranceSteps = new RouteStepArray
    val exitSteps = new RouteStepArray
    val beginSteps = new RouteStepArray
    val tacitSteps = new RouteStepArray
    val completeSteps = new RouteStepArray
    val emitCodes = new Array[Int](maxStepKey)
    val endSteps = new RouteStepArray


    routeSteps sortBy (_.stepKey) foreach {
      step =>
        step.traits foreach {
          case BeginStepTrait =>
            beginSteps += step.stepKey
          case EndStepTrait =>
            endSteps += step.stepKey
          case EnterStepTrait =>
            entranceSteps += step.stepKey
          case ExitStepTrait =>
            exitSteps += step.stepKey
          case TacitStepTrait =>
            tacitSteps += step.stepKey
          case CompleteStepTrait =>
            completeSteps += step.stepKey
          case _ => ???
        }

        val edgeKeys = step.tos.map(
          t =>
            t.stepKey.reduceToFixAtom.getOrElse(
              throw FeltException(t.location, s"stepKey in 'to' clause did not reduce to a fixed atom")
            ).value
        )
        if (edgeKeys.nonEmpty) {
          transitions(step.stepKey - 1) = FeltRouteTransition(new Array[FeltRouteEdge](edgeKeys.max.toInt + 1))
          var edgeIndex = 0
          step.tos foreach {
            edge =>
              transitions(step.stepKey - 1)(edgeIndex) =
                FeltRouteEdge(edge.reducedStepKey, edge.reducedMaxTime, edge.reducedMinTime)
              edgeIndex += 1
          }
        }

    }

    val tSteps = tacitSteps.result()
    tSteps foreach {
      step =>
        if (beginSteps.result().contains(step)) throw FeltException(graph, s"FELT_ROUTE_TACIT_OVERLAP_WITH_BEGIN_STEP step=$step")
        if (entranceSteps.result().contains(step)) throw FeltException(graph, s"FELT_ROUTE_TACIT_OVERLAP_WITH_ENTRANCE_STEP step=$step")
        if (endSteps.result().contains(step)) throw FeltException(graph, s"FELT_ROUTE_TACIT_OVERLAP_WITH_END_STEP step=$step")
        if (completeSteps.result().contains(step)) throw FeltException(graph, s"FELT_ROUTE_TACIT_OVERLAP_WITH_COMPLETE_STEP step=$step")
    }

    var key = 1
    val missingTransitions = (for (k <- transitions) yield {
      val emit = if (k == null) key else -1
      key += 1
      emit
    }).filter(_ != -1).filter(s => !exitSteps.result().contains(s) && !completeSteps.result().contains(s))
    if (missingTransitions.nonEmpty)
      throw FeltException(graph, s"FELT_ROUTE_MISSING_TRANSITIONS: step keys=${missingTransitions.mkString("{", ", ", "}")}")

    _builder = binding.newBuilder

    _builder.init(
      frameId = route.frame.frameId,
      frameName = route.frame.frameName,
      binding = route.global.binding,
      maxPartialPaths = route.maxPartialPathsValue,
      maxCompletePaths = route.maxCompletePathsValue,
      maxStepsPerPath = route.maxStepsValue,
      maxPathTime = route.maxPathTimeValue,
      minCourse = -1, maxCourse = -1, // no courses yet
      entranceSteps = entranceSteps.result(),
      exitSteps = exitSteps.result(),
      beginSteps = beginSteps.result(),
      tacitSteps = tSteps,
      emitCodes = emitCodes,
      endSteps = endSteps.result(),
      completeSteps = completeSteps.result(),
      transitions = transitions
    )

  }

  def genBuilder(implicit cursor: FeltCodeCursor): FeltCode = {

    def transitionsCode(implicit cursor: FeltCodeCursor): FeltCode = _builder.transitions.map {
      case null =>
        s"""|
            |${I2}null""".stripMargin
      case e =>
        e.generateCode(cursor indentRight 1)
    }.mkString(",")

    s"""|$I${binding.builderClassName}(
        |$I2${_builder.maxPartialPaths},    // max partial paths per route
        |$I2${_builder.maxCompletePaths},   // max complete paths per route
        |$I2${_builder.maxStepsPerGraph},   // max steps per graph
        |$I2${_builder.maxPathTime},        // max time per path (path timeout)
        |$I2${_builder.minCourse},          // min course
        |$I2${_builder.maxCourse},          // max course
        |${C2(s"entrance steps")}${generateIntArrayCode(_builder.entranceSteps)(cursor indentRight 1)},
        |${C2(s"exit steps")}${generateIntArrayCode(_builder.exitSteps)(cursor indentRight 1)},
        |${C2(s"begin steps")} ${generateIntArrayCode(_builder.beginSteps)(cursor indentRight 1)},
        |${C2(s"tacit steps")}${generateIntArrayCode(_builder.tacitSteps)(cursor indentRight 1)},
        |${C2(s"emit codes")}${generateIntArrayCode(_builder.emitCodes)(cursor indentRight 1)},
        |${C2(s"end steps")}${generateIntArrayCode(_builder.endSteps)(cursor indentRight 1)},
        |${C2(s"complete steps")}${generateIntArrayCode(_builder.completeSteps)(cursor indentRight 1)},
        |${I2}Array[${FeltRouteTransition.transitionClassName}](${transitionsCode(cursor indentRight 1)}
        |$I2)
        |$I).init(${_builder.frameId}, "${_builder.frameName}", feltBinding)""".stripMargin
  }

}
