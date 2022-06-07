/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route

/**
 * manipulation and questions about the route state machine
 * ==Semantic Rules for Routes, Steps, and Paths==
 * As the FSM fields incoming assertions, it conforms to the following rules. These rules
 * attempt to be '''unambiguous''' and '''complete''' for all edge cases but probably fall
 * short. Improvements and/or better documentation welcome.
 * ===Assertion Rules===
 * {{{
 * 1) IFF not in path and we do not get an entrance step, no transition happens
 * 2) IFF not in path and we do get an entrance step, then transition to a new path and step
 * 3) IFF we are in a path, and we get a valid transition, then record new step in existing path
 * 4) IFF #2 or #3 and new step is an exit step, then close the path
 * 5) IFF we are in a path, and no valid next step transition found, then close path
 * 6) IFF there is more than one valid transition, a valid transition will be chosen at random.
 * 7) IFF current path ordinal has reached maxPartialPaths, and the path has ended, record no more steps
 *          OR
 *    IFF current path ordinal has reached maxCompletePaths, and the path has completed, record no more steps
 * 8) IFF an assert succeeds AND the stepKey has a 'tacit' trait, then do not actually record that step
 * }}}
 * ===Visit Rules===
 * {{{
 * 1) IFF at start of a visit of the route, the ''current'' or last
 * step has a [[CompleteStepTrait]] trait, then backtrack/rewrite all steps in that path to be of
 * status ''complete''
 * }}}
 */
trait FeltRouteFsm extends Any {

  /**
   * assert a possible new step in a route. Returns true if a transition occurred.
   * record step and optionally a time
   *
   * @param schema
   * @param stepKey
   * @param stepTime
   * @return
   */
  def routeFsmStepAssert(builder: FeltRouteBuilder, stepKey: FeltRouteStepKey, stepTime: FeltRouteStepTime): Boolean

  /**
   * assert a possible new step in a route. Returns true if a transition occurred.
   * record step key, step tag, and step time
   *
   * @param schema
   * @param stepKey
   * @param stepTag
   * @param stepTime
   * @return
   */
  def routeFsmStepAssert(builder: FeltRouteBuilder, stepKey: FeltRouteStepKey, stepTag: FeltRouteStepTag, stepTime: FeltRouteStepTime): Boolean

  /**
   * record a time (or new time) into the current step if the given step is the last recorded step
   *
   * @param schema
   * @param stepKey
   * @param stepTime
   * @return
   */
  def routeFsmAssertTime(builder: FeltRouteBuilder, stepKey: FeltRouteStepKey, stepTime: FeltRouteStepTime = -1L): Boolean

  /**
   * Return true if the FSM is in a specific step.
   *
   * @param step
   * @return
   */
  def routeFsmInStep(step: FeltRouteStepKey): Boolean

  /**
   * Tell the FSM to ''end'' whatever path you are in. Reset the current step to be 'no current step'
   */
  def routeFsmEndPath(): Unit

  /**
   * Returns true if Fsm is currently in an active path
   */
  def routeFsmInPath(): Boolean

  /**
   * Tell the FSM to ''complete'' (make the path a 'full' path)
   * if the current step if it has a [[CompleteStepTrait]] otherwise this
   * does the same thing as the `routeFsmEndPath()` call.
   * This creates a full path. This is called either manually or ''before'' a visit.
   */
  def routeFsmBackFill(builder: FeltRouteBuilder): Unit

}
