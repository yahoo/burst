/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route.machine

import org.burstsys.felt.model.collectors.route._
import org.burstsys.zap.route._
import org.burstsys.zap.route.state.ZapRouteState

trait ZapRouteMachine extends Any with ZapRoute with ZapRouteState {

  //////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////

  @inline final override
  def routeFsmStepAssert(builder: FeltRouteBuilder, candidateStepKey: FeltRouteStepKey,
                         candidateStepTime: FeltRouteStepTime): Boolean = {
    routeFsmStepAssert(builder, candidateStepKey, FeltRouteInvalidTag, candidateStepTime)
  }

  @inline final override
  def routeFsmStepAssert(builder: FeltRouteBuilder, candidateStepKey: FeltRouteStepKey,
                         stepTag: FeltRouteStepTag, candidateStepTime: FeltRouteStepTime): Boolean = {

    // log info s"stepAssert(key=$candidateStepKey, tag=$stepTag, time=$candidateStepTime) ${entryToString(dirtyEntry)}"
    // read the current path
    val cp = currentPathOrdinal

    // read the current step
    val cs = currentStepKey

    // read the current time
    val ct: Long = currentTime

    val elapsedTime: Long = candidateStepTime - ct

    // RULE #7
    if (rule7(builder, cp, cs)) {
      return false
    }

    // Path timeouts
    if (inPath && builder.maxPathTime != -1) {
      // now check for global path timeout
      if (candidateStepTime - routePathStartTime  > builder.maxPathTime) {
        routeFsmBackFill(builder) // make sure we are up to date so we can test count of complete paths
        // path is aborted
        routeFsmEndPath()
        return false // (stop processing assertion)
      }
    }

    // RULE #1
    if (!inPath && !builder.isEntranceStep(candidateStepKey)) {
      return false
    }

    // RULE #2
    if (!inPath && builder.isEntranceStep(candidateStepKey)) {
      recordNewStep(builder, cp + 1, candidateStepKey, stepTag, candidateStepTime)
      if (itemLimited) {
        return false
      }
      dirtyEntry.stepOrdinal = 0
      rule4(builder, candidateStepKey)
      return true
    }

    // Look for possible transitions inside an active path
    val transitions = builder.transitions(cs - 1)
    var i = 0
    var foundValidStepTransition = false
    var possibleStepTransitionStillExists = false // track possible next steps
    while (transitions != null && i < transitions.length && !foundValidStepTransition) {
      val transition = transitions(i)
      if (transition != null)
        if (
          (transition.minTime == FeltRouteForever || elapsedTime > transition.minTime) &&
            (transition.maxTime == FeltRouteForever || elapsedTime < transition.maxTime)
        ) {
          possibleStepTransitionStillExists = true

          // RULE #3
          if (transition.stepKey == candidateStepKey) {
            foundValidStepTransition = true
            recordNewStep(builder, cp, candidateStepKey, stepTag, candidateStepTime)
            if (itemLimited) {
              return false
            }

            // RULE #4
            rule4(builder, candidateStepKey)
          }
        }
      i += 1
    }

    // RULE #5
    if (!possibleStepTransitionStillExists) {
      closePath()
      dirtyEntry.isLastStepInPath = true
    }

    foundValidStepTransition

  }

  @inline final override
  def routeFsmInStep(step: FeltRouteStepKey): Boolean = currentStepKey == step

  @inline final override
  def routeFsmEndPath(): Unit = {
    currentStepKey = FeltRouteNotInPathStep
    // log info s"routeFsmEndPath${entryToString(dirtyEntry)}"
  }

  @inline final override
  def routeFsmAssertTime(automata: FeltRouteBuilder, stepKey: FeltRouteStepKey, stepTime: FeltRouteStepTime): Boolean = {
    if (isEmpty) return false
    if (scopeDirty) {
      if (dirtyEntry.stepKey == stepKey) {
        dirtyEntry.stepTime = stepTime
        return true
      }
    } else {
      if (commitEntry.stepKey == stepKey) {
        commitEntry.stepTime = stepTime
        return true
      }
    }
    false
  }

  @inline final override
  def routeFsmInPath(): Boolean = currentStepKey != FeltRouteNotInPathStep

  //////////////////////////////////////////////////////////////////////
  // Implementation
  //////////////////////////////////////////////////////////////////////

  @inline private
  def closePath(): Unit = currentStepKey = FeltRouteNotInPathStep

  @inline private
  def inPath: Boolean = currentStepKey != FeltRouteNotInPathStep

  @inline private
  def rule4(builder: FeltRouteBuilder, candidateStep: FeltRouteStepKey): Unit = {
    if (builder.isExitStep(candidateStep)) {
      routeCompletePaths = routeCompletePaths + 1
      closePath()
      dirtyEntry.isComplete = true
      dirtyEntry.isLastStepInPath = true
    }
  }

  /**
   * {{{
   * 1) iff both maxCompletePaths AND maxPartialPaths is set to -1 (not specified) then return false
   * 2) iff already in a path return false
   * }}}
   *
   * @param builder     builder
   * @param currentPath current path ordinal
   * @param currentStep current step key
   * @return
   */
  @inline private
  def rule7(builder: FeltRouteBuilder, currentPath: FeltRoutePathOrdinal, currentStep: FeltRouteStepKey): Boolean = {
    val iPath = inPath

    // if already in a path, continue
    if (iPath)
      return false

    // if no limits specified return false (keep processing assertion)
    if (builder.maxCompletePaths == -1 && builder.maxPartialPaths == -1)
      return false

    // if there are too many partial paths return true (stop processing assertion)
    if (builder.maxPartialPaths != -1 && currentPath >= builder.maxPartialPaths)
      return true

    routeFsmBackFill(builder) // make sure we are up to date so we can test count of complete paths

    // if there are too many complete paths return true (stop processing assertion)
    if (builder.maxCompletePaths != -1 && routeCompletePaths >= builder.maxCompletePaths)
      return true

    false // return false (keep processing assertion)
  }

  @inline private
  def recordNewStep(builder: FeltRouteBuilder, pathOrdinal: FeltRoutePathOrdinal, stepKey: FeltRouteStepKey,
                    stepTag: FeltRouteStepTag, stepTime: FeltRouteStepTime): Unit = {

    // if this is the first step in the path, then record that step as the path start time
    if (!inPath)
      routePathStartTime = stepTime

    // handle tacit steps
    val isTacit = builder.tacitSteps.contains(stepKey)

    // handle complete trait discover
    if (builder.completeSteps.contains(stepKey))
      rewriteNeeded = true

    createNewJournalEntry(isTacit)
    if (itemLimited) {
      return
    }
    currentPathOrdinal = pathOrdinal
    currentStepKey = stepKey
    currentTime = stepTime
    dirtyEntry.pathOrdinal = pathOrdinal
    dirtyEntry.stepKey = stepKey
    dirtyEntry.stepTag = stepTag
    dirtyEntry.stepTime = stepTime
    dirtyEntry.isComplete = false

    // log info s"recordNewStep ${entryToString(dirtyEntry)}"
  }


}
