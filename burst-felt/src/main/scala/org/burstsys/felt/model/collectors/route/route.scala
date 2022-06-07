/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors

import org.burstsys.felt.model.collectors.decl.FeltCollectorProvider
import org.burstsys.felt.model.collectors.route.decl.{FeltRouteDecl, FeltRouteRef}

package object route {

  ///////////////////////////////////////////////////////////////////
  // CONSTRAINTS
  ///////////////////////////////////////////////////////////////////

  final val maxPartialPathsParameterName = "maxPartialPaths"

  final val maxCompletePathsParameterName = "maxCompletePaths"

  final val maxStepsPerRouteParameterName = "maxSteps"

  final val maxPathTimeParameterName = "maxPathTime"

  final val MaxStepsInGraph = 2000

  ///////////////////////////////////////////////////////////////////
  // PATH ORDINAL
  ///////////////////////////////////////////////////////////////////

  type FeltRoutePathOrdinal = Int

  ///////////////////////////////////////////////////////////////////
  // PATH ORDINAL
  ///////////////////////////////////////////////////////////////////

  type FeltRouteStepOrdinal = Int
  final val FeltRouteNotInPathOrdinal: FeltRouteStepOrdinal = -1

  ///////////////////////////////////////////////////////////////////
  // STEP KEY
  ///////////////////////////////////////////////////////////////////

  type FeltRouteStepKey = Int

  final val FeltRouteNotInPathStep: FeltRouteStepKey = -1

  ///////////////////////////////////////////////////////////////////
  // STEP TAG
  ///////////////////////////////////////////////////////////////////

  type FeltRouteStepTag = Int

  final val FeltRouteInvalidTag: FeltRouteStepTag = -1

  ///////////////////////////////////////////////////////////////////
  // STEP TIME
  ///////////////////////////////////////////////////////////////////

  type FeltRouteStepTime = Long

  type FeltRouteMaxTime = Long

  final val FeltRouteForever: FeltRouteStepTime = 0L

  final val FeltRouteNoTime: FeltRouteStepTime = -1L

  ///////////////////////////////////////////////////////////////////
  //  TYPES
  ///////////////////////////////////////////////////////////////////

  trait FeltRouteProvider
    extends FeltCollectorProvider[FeltRouteCollector, FeltRouteBuilder, FeltRouteRef, FeltRouteDecl, FeltRoutePlan]

  /**
   * Each STEP can have one or more STEP TRAITs
   */
  sealed case class StepTrait(code: Int) {
    override def toString: String = {
      getClass.getSimpleName.stripSuffix("StepTrait$").toLowerCase
    }
  }

  object EnterStepTrait extends StepTrait(1)

  object EndStepTrait extends StepTrait(2)

  object ExitStepTrait extends StepTrait(3)

  object BeginStepTrait extends StepTrait(4)

  object TacitStepTrait extends StepTrait(5)

  object CompleteStepTrait extends StepTrait(6)

}
