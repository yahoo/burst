/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route

import org.burstsys.felt.model.collectors.runtime.FeltCollector

/**
 * = routes =
 * A route is an off heap data structure that stores zero or more sequences of steps stored as integer keys.
 * You can go forward or backwards within each sequence. You can ask for counts for each step.
 * ==requirements==
 * {{{
 *    -capture a sequence of step keys, one for each path through this route
 *    -know how many times a path has been recorded
 *    -know how many times a step has been satisfied in each route
 *    -be able to back up a route to an earlier step
 *    -be able to reset a path to the beginning
 *    -be able to clear all satisfied paths in this route
 *    -uses a provided automation plan that knows which transitions are valid and which cause resets or backsteps
 * }}}
 *
 */
trait FeltRouteCollector extends Any
  with FeltCollector with FeltRouteIterator with FeltRouteFsm with FeltRouteScopes {

  /**
   * a count of full paths recorded (committed)
   *
   * @return
   */
  def routeCompletePaths: Int

  /**
   * total steps recorded in route
   * @return
   */
  def routeStepCount: Int

  /**
   * was the last path recorded (committed) complete?
   * a runtime error if this route is empty
   *
   * @return
   */
  def routeLastPathIsComplete: Boolean

  /**
   * what was the path ordinal of the last path recorded (committed)?
   * a runtime error if this route is empty
   *
   * @return
   */
  def routeLastPathOrdinal: FeltRoutePathOrdinal

  /**
   * what was the step key of the last route entry recorded (committed)?
   * a runtime error if this route is empty
   *
   * @return
   */
  def routeLastStepKey: FeltRouteStepKey

  /**
   * what was the step tag of the last route entry recorded (committed)?
   * a runtime error if this route is empty
   *
   * @return
   */
  def routeLastStepTag: FeltRouteStepTag

  /**
   * what was the step time of the last route entry recorded (committed)?
   * a runtime error if this route is empty
   *
   * @return
   */
  def routeLastStepTime: FeltRouteStepTime

  /**
   * EPOCH time the path was started
   * @return
   */
  def routePathStartTime: FeltRouteStepTime

  def clear(): Unit = {}

}
