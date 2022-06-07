/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.generate

package object splice {
  /*
    /*
      @inline final protected
      def processTabletVector(step: GistZapTraverseStep, preJoinParentCube: ZapCubeContext): ZapCubeContext = {
        val cubeCursor = step.cubeCursors(ZapDefaultHyperCubeIndex)

        ////////////////////////////
        FeltVectorAllocPlace
        ////////////////////////////
        val vectorCube = zapCubeGrabNewOp(
          schema = cubeSchema,
          cubeId = cubeCursor.cubeId,
          parentCubeId = cubeCursor.parentCubeId,
          preJoinInstanceCube = preJoinParentCube
        )
        cubeAccessor.currentZapCube(vectorCube)

        ////////////////////////////
        FeltVectorBeforePlace
        ////////////////////////////
        executePreActions(step)

        // and we will have one for each member of the vector
        var memberCube: ZapCubeContext = null

        /**
          * keep track of which member is the last. This is used by query code where interesting.
          */
        axisRuntime lastVectorMember false

        val tablet = tabletRuntime.tabletForPathKey(step.pathKey)

        /**
          * setup to scan the vector members.
          */
        val memberCount: Int = tablet.tabletSize

        var i = 0
        while (i < memberCount) {

          if (i == memberCount - 1) axisRuntime.lastVectorMember(true)

          ////////////////////////////
          FeltVectorMemberAllocPlace
          ////////////////////////////
          memberCube = zapCubeGrabNewOrReuseOp(
            schema = cubeSchema,
            cubeId = cubeCursor.cubeId,
            parentCubeId = cubeCursor.parentCubeId,
            currentCube = memberCube,
            parentCube = preJoinParentCube
          )
          cubeAccessor.currentZapCube(memberCube)

          /**
            * set up vector value fields
            */
          step.contentTypeKey match {
            case BrioBooleanKey => tabletRuntime.tabletVectorVisitBoolean(tablet.tabletBooleanAt(i))
            case BrioByteKey => tabletRuntime.tabletVectorVisitByte(tablet.tabletByteAt(i))
            case BrioShortKey => tabletRuntime.tabletVectorVisitShort(tablet.tabletShortAt(i))
            case BrioIntegerKey => tabletRuntime.tabletVectorVisitInt(tablet.tabletIntegerAt(i))
            case BrioLongKey => tabletRuntime.tabletVectorVisitLong(tablet.tabletLongAt(i))
            case BrioDoubleKey => tabletRuntime.tabletVectorVisitDouble(tablet.tabletDoubleAt(i))
            case BrioStringKey =>
              tabletRuntime.tabletVectorVisitString(tablet.tabletStringAt(i)(threadRuntime, itemRuntime.dictionary))
          }

          ////////////////////////////
          FeltVectorMemberSituPlace
          ////////////////////////////
          executeSituActions(step)

          ////////////////////////////
          FeltVectorMemberMergePlace
          ////////////////////////////
          zapCubeMergeOp(
            schema = cubeSchema,
            cubeId = cubeCursor.cubeId,
            parentCubeId = cubeCursor.parentCubeId,
            sourceCube = memberCube,
            destinationCube = vectorCube
          )

          i += 1
          ////////////////////////////
          FeltVectorMemberFreePlace
          ////////////////////////////
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
   */
}
