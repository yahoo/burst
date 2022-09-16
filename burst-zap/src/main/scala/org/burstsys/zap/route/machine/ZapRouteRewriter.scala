/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route.machine

import org.burstsys.felt.model.collectors.route._
import org.burstsys.zap.route._
import org.burstsys.zap.route.state.ZapRouteState

/**
 * all route functions that backtrack/rewrite (not this violates
 * mechanical sympathy which impacts large routes) If this is a problem
 * then a side lookup table of 'completed paths' would allow this to be avoided.
 */
trait ZapRouteRewriter extends Any with ZapRoute with ZapRouteState {

  @inline final override
  def routeFsmBackFill(builder: FeltRouteBuilder): Unit = {

    // must have at least one step
    if (isEmpty) {
      return
    }

    //    log info s"routeFsmCompletePath(basePtr=$basePtr) rewriteNeeded=$rewriteNeeded"
    if (!rewriteNeeded) {
      return
    }

    /**
     * backtrack back from end of journal to beginning of the journal.
     * At each 'end' step of each path test to see if that step has a 'complete' trait,
     * If so then as you go backwards mark each step in that path 'complete'.
     */
    var currentEntry = dirtyEntry
    var currentPathOrdinal = currentEntry.pathOrdinal
    var previousPathOrdinal = -1
    var currentStepOrdinal = currentEntry.stepOrdinal
    var currentStepKey = currentEntry.stepKey
    var currentStepTag = currentEntry.stepTag
    var currentStepTacit = currentEntry.isTacit
    var inCompletePath = false
    var inLastStep = (currentStepOrdinal == 0 && !currentStepTacit && currentPathOrdinal == 1)

    def inNewPath: Boolean = previousPathOrdinal == -1 || (previousPathOrdinal != currentPathOrdinal)

    def stepProcess(): Unit = {
      if (inNewPath) { // is this a new path?
        currentEntry.isLastStepInPath = true
        // it the last step in a path has a complete trait then mark the entire path complete?
        if (builder.isCompleteStep(currentStepKey)) {
          if (!currentEntry.isComplete) { // if not already recorded as full path
            routeCompletePaths += 1 // make sure we record this as a new full path
          }
          inCompletePath = true
        } else {
          inCompletePath = false
        }
        currentEntry.isComplete = inCompletePath
      } else { // continuation of existing path
        currentEntry.isComplete = inCompletePath
      }
      currentEntry.isRewritten = true // mark the journal entry that we have rewritten up to this point
    }

    def print(msg: String): Unit = log info
      s"""|-------------------------------------------
          |$msg
          |   basePtr=$basePtr
          |   currentPathOrdinal=$currentPathOrdinal
          |   previousPathOrdinal=$previousPathOrdinal
          |   currentStepOrdinal=$currentStepOrdinal
          |   currentStepKey=$currentStepKey
          |   currentStepTag=$currentStepTag
          |   inCompletePath=$inCompletePath
          |   inLastStep=$inLastStep
          |   tacit=$currentStepTacit
          |   routeCompletePath$routeCompletePaths
          |-------------------------------------------""".stripMargin

    // this is the loop that backtracks through the entire journal
    do {
      inLastStep = (currentStepOrdinal == 0 && !currentStepTacit && currentPathOrdinal == 1) || currentEntry.isRewritten
      //print("BEFORE")
      stepProcess()
      // print("AFTER")
      if (!inLastStep) {
        previousPathOrdinal = currentPathOrdinal
        currentEntry = currentEntry.previous
        currentPathOrdinal = currentEntry.pathOrdinal
        currentStepOrdinal = currentEntry.stepOrdinal
        currentStepTacit = currentEntry.isTacit
        currentStepTag = currentEntry.stepTag
        currentStepKey = currentEntry.stepKey
      }
    } while (!inLastStep) // stop at beginning

    rewriteNeeded = false // mark the route as being up to date wrt to rewrites

  }


}
