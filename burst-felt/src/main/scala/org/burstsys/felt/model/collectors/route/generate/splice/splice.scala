/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.generate

import org.burstsys.vitals.logging.VitalsLogger

package object splice extends VitalsLogger {


  /*
    def processRouteSteps(step: GistZapTraverseStep, preJoinParentCube: ZapCubeContext): ZapCubeContext = {
      val cubeCursor = step.cubeCursors(ZapDefaultFrameId)

      ////////////////////////////
      FeltVectorAllocPlace
      ////////////////////////////
      val vectorCube = zapCubeGrabNewOp(
        builder = cubeBuilder,
        cubeId = cubeCursor.cubeId,
        parentCubeId = cubeCursor.parentCubeId,
        preJoinInstanceCube = preJoinParentCube
      )
      cubeAccessor.currentCube = vectorCube

      ////////////////////////////
      FeltVectorBeforePlace
      ////////////////////////////
      executePreActions(step)

      // and we will have one for each member of the vector
      var memberCube: ZapCubeContext = null

      val route = routeRuntime.routeForPathKey(step.pathKey)
      val schema = routeRuntime.routeSchemaForPathKey(step.pathKey)

      // run through the entire set of steps
      var currentEntry = route.firstEntry
      while (currentEntry.validEntry) {
        routeRuntime.rteStepVisitPathOrdinal(currentEntry.pathOrdinal)
        routeRuntime.rteStepVisitStepKey(currentEntry.stepKey)
        routeRuntime.rteStepVisitStepTime(currentEntry.stepTime)

        ////////////////////////////
        FeltVectorMemberAllocPlace
        ////////////////////////////
        memberCube = zapCubeGrabNewOrReuseOp(
          builder = cubeBuilder,
          cubeId = cubeCursor.cubeId,
          parentCubeId = cubeCursor.parentCubeId,
          currentCube = memberCube,
          parentCube = preJoinParentCube
        )
        cubeAccessor.currentCube = memberCube

        ////////////////////////////
        FeltVectorMemberSituPlace
        ////////////////////////////
        executeSituActions(step)

        ////////////////////////////
        FeltVectorMemberMergePlace
        ////////////////////////////
        zapCubeMergeOp(
          builder = cubeBuilder,
          cubeId = cubeCursor.cubeId,
          parentCubeId = cubeCursor.parentCubeId,
          sourceCube = memberCube,
          cubeAccessor.currentCubeDictionary,
          destinationCube = vectorCube
        )

        currentEntry = currentEntry.next
      }

      ////////////////////////////
      FeltVectorAfterPlace
      ////////////////////////////

      ////////////////////////////
      FeltVectorFreePlace
      ////////////////////////////
      zapCubeReleaseNonNullOp(cube = memberCube)

      vectorCube
    }
  */

}
