/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route.course

import org.burstsys.brio.types.BrioCourse.BrioCourse32
import org.burstsys.zap.route.ZapRoute
import org.burstsys.zap.route.state.ZapRouteState

/**
 * For reading out unique keys for 'courses' through a route. A course is a single sequence of steps through
 * a route. There is a 1:1 correspondence between a path and a course. Each course has a path-identity
 * which is a byte array (or string?) that is a sequence of up to 16 4 bit step keys from a set of 15 possible steps
 * encoded in a single 8 byte long. This long value would then presumably be used as a dimension in a result set.
 * a zero is a special step that would indicate the end of the path. Reaching 16 steps is also defined as a complete
 * course.
 * e.g. 12345678, 020103, 030201 which you can
 * place into a dimension in your cube.
 * Each of these keys presumably is visited in a special 'Course' visit (as opposed to a 'Step' visit)
 * so you can show e.g. ' most common course through a route grouped by gender and region'
 * Helpful would be a 'top-k' (really a frequent-item algorithm since it is top-k for each item which is merged
 * globally and hence is not a guaranteed accurate topK model globally) model.
 * This allows the query writer to significantly reduce query cardinality  while still getting useful insight into
 * the most common courses.
 *
 *
 * {{{
 * resetCourseReader()
 * while (hasAnotherCourse) {
 * val courseKey = readCourse
 * }
 * }}}
 */
trait ZapRouteCourseReader extends Any with ZapRoute with ZapRouteState {

  //////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////

  @inline final
  def resetCourseReader(): Unit = {
    ???
    //    resetStepReader()
  }

  /**
   * Read one path at a time encoded into a 8 byte Course key
   *
   * @return a valid course or an empty course if there are no more courses
   */
  @inline final
  def readNextCourse32: BrioCourse32 = {
    ???
    /*
        var c = BrioCourse() // value class means no object allocation - this is really a long on the stack
        var continue = hasAnotherStep
        var currentStep = readJournalEntryStep
        var currentPath = readJournalEntryPath

        var stepIndex = 0
        while (continue) {
          // add this step into our 64 bit course key
          c = c.mergeStep(stepIndex, currentStep)
          nextStep()
          stepIndex += 1
          currentStep = readJournalEntryStep
          val newPath = readJournalEntryPath
          continue = newPath == currentPath && hasAnotherStep
        }
        c
    */
  }

}
