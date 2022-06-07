/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route.course

/** The Brio Course builder is used to follow a sequence of steps and support
 * finding courses to construct.
 * ==Different Types of Courses==
 * {{{
 *   Type 1. Courses that have a start id but no end id. These start when a start id is seen and
 *   end when max ordinal is reached (or when the scan stops?)
 *   Type 2. Courses that have an end id but no start id. These start right away and end when the end is reached
 *   (or when the scan stops?)
 *   Type 3. Courses that have both a start id and an end id. These start when a start id is seen and end when the
 *   end id is seen
 * }}}
 * ==Semantic Rules==
 * Where A is a start point and C is an end point in a 6 step sequence.
 * {{{
 *   1. We keep track of multiple start points to handle 'overlapping' sequences like AABBC
 *   e.g. AABBC would produce AABBC and ABBC as output.
 *   2. We keep track of multiple end points for multiple sequences that can fit into a single sequence
 *   e.g. ABCADC would produce ABCADC, ABC, and ADC.
 *   3. How about when we have both say as in AABCBC. We would create AABC, ABC, AABC, AABCBC, ABCBC.
 *   2. Sequences start where we see one of a set of `start` ids
 *   3. Sequences end where we reach one of a set of `end` ids, or if no `end` ids are specified, when a sequence
 *   reaches `maxOrdinal`
 * }}}
 */
trait ZapRouteCourseIterator extends Any {


  /*
    @inline final protected
    def processRouteCourses(step: GistZapTraverseStep, preJoinParentCube: ZapCubeContext): ZapCubeContext = {
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

      val route = routeRuntime.routeForPathKey(step.pathKey)
      val schema = routeRuntime.routeSchemaForPathKey(step.pathKey)

      val minCourseLength = schema.minCourse
      val maxCourseLength = schema.maxCourse

      val builder = step.contentTypeKey match {
        case BrioCourse32Key =>
          if (maxCourseLength > 32)
            throw ZapException(ZapInvalidCourseVisit, s"course type 32 will not hold maxCourseLength=$maxCourseLength")
          CourseBuilder32(minCourseLength, maxCourseLength)
        case BrioCourse16Key =>
          if (maxCourseLength > 16)
            throw ZapException(ZapInvalidCourseVisit, s"course type 16 will not hold maxCourseLength=$maxCourseLength")
          CourseBuilder32(minCourseLength, maxCourseLength)
        case BrioCourse8Key =>
          if (maxCourseLength > 8)
            throw ZapException(ZapInvalidCourseVisit, s"course type 8 will not hold maxCourseLength=$maxCourseLength")
          CourseBuilder32(minCourseLength, maxCourseLength)
        case BrioCourse4Key =>
          if (maxCourseLength > 4)
            throw ZapException(ZapInvalidCourseVisit, s"course type 4 will not hold maxCourseLength=$maxCourseLength")
          CourseBuilder32(minCourseLength, maxCourseLength)
        case x => throw ZapException(ZapInvalidCourseVisit, s"$x unknown course type for visit")
      }

      /**
        * run through the entire set of steps in journal, generating courses as we find them
        */
      var currentEntry = route.firstEntry
      while (!currentEntry.validEntry) {

        // place step key into course builder
        // test to see if that finished a course
        // if so output that course
        if (builder.addStep(schema, currentEntry)) {

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

          // place course into proper place
          routeRuntime.rteCourseVisit(builder.course)

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
        }

        currentEntry = currentEntry.next

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

}
