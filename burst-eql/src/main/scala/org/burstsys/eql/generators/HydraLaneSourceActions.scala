/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.temporaries.TemporaryExpressionContext
import org.burstsys.eql.actions.{ControlTestTemporary, DimensionInsert, Temporary}
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.DeclarationScope.DeclarationScope
import org.burstsys.eql.generators.hydra.visits.{PhasedLaneSourceGenerator, SituLaneSourceGenerator}
import org.burstsys.eql.paths.VisitPath
import org.burstsys.eql.planning.lanes.{LaneActions, LaneControl, LaneName}

import scala.collection.mutable

abstract class HydraLaneSourceActions(val name: LaneName, val path: VisitPath, steps: LaneActions)
  extends LaneSourceGenerator {

  val blockSourceGenerator: LaneActionsSourceGenerator = steps
  val control: LaneControl = steps.control
  val phasedControlledActions: Map[ActionPhase, mutable.Queue[ActionSourceGenerator]] = Map (
    ActionPhase.Before -> mutable.Queue[ActionSourceGenerator](),
    ActionPhase.Pre -> mutable.Queue[ActionSourceGenerator](),
    ActionPhase.Post -> mutable.Queue[ActionSourceGenerator](),
    ActionPhase.After -> mutable.Queue[ActionSourceGenerator]()
  )

  def visitControlTest: Option[ControlTestTemporary] = control.getVisitControlTest

  for (step <- steps.actions) {
    phasedControlledActions(step.phase()).enqueue(step)
  }
  if (steps.doesDimensionWrite && steps.actions.forall(_.providesDimensionWrite == false))
    phasedControlledActions(ActionPhase.Post).enqueue(DimensionInsert())

  override
  def getDeclarations(scope: DeclarationScope)(implicit context: GlobalContext): Array[Declaration] = {
    // extract control variables
    val controlVars: Array[Declaration] = if (visitControlTest.isDefined) {
      if (visitControlTest.get.needsSummary)
        Array(visitControlTest.get.summaryVar, visitControlTest.get.tempVar)
      else
        Array(visitControlTest.get.tempVar)
    } else
      Array.empty

    // extract temporary variables
    val tempVars = phasedControlledActions.values.flatMap{_.collect {
      case t: Temporary => t.tempVar
    }.toArray[Declaration]}
    (controlVars ++ tempVars).filter(_.scope ==scope)
  }
}

class HydraSituLaneSourceActions(name: LaneName, path: VisitPath, steps: LaneActions)
  extends HydraLaneSourceActions(name, path, steps) with SituLaneSourceGenerator

class HydraPhasedLaneSourceActions(name: LaneName, path: VisitPath, steps: LaneActions)
  extends HydraLaneSourceActions(name, path, steps) with PhasedLaneSourceGenerator
