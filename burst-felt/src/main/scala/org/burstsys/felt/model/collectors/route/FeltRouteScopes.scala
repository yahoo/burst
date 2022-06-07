/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route

/**
 * manipulation and questions about scopes
 */
trait FeltRouteScopes extends Any {

  /**
   * Start a route transaction, all operations have a well defined semantic within this scope, including the
   * ability to roll back all operations removing all route specific side effects.
   */
  def routeScopeStart(schema: FeltRouteBuilder): Unit

  /**
   * commit a route transaction, making all route operations permanent
   */
  def routeScopeCommit(schema: FeltRouteBuilder): Unit

  /**
   * abort a route transaction, making all route updates in scope revert leaving no side effects.
   */
  def routeScopeAbort(schema: FeltRouteBuilder): Unit

  /**
   * What is the current path ordinal within this scope
   *
   * @return
   */
  def routeScopeCurrentPath: FeltRoutePathOrdinal

  /**
   * what was the path ordinal when this scope was started?
   *
   * @return
   */
  def routeScopePreviousPath: FeltRoutePathOrdinal

  /**
   * Had the path ordinal changed within this scope?
   *
   * @return
   */
  def routeScopePathChanged: Boolean

  /**
   * what is the current step key?
   *
   * @return
   */
  def routeScopeCurrentStep: FeltRouteStepKey

  /**
   * what was the step key when this scope was started?
   *
   * @return
   */
  def routeScopePreviousStep: FeltRouteStepKey

  /**
   * Had the step key changed within this scope?
   *
   * @return
   */
  def routeScopeStepChanged: Boolean

}
