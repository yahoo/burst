/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route

/**
 * universal trait capturing a single route journal (log) entry
 * These are successfully asserted 'steps'
 */
trait FeltRouteEntry extends Any {

  //////////////////////////////////////////////////////////////////////////////
  // ENTRY STATE
  //////////////////////////////////////////////////////////////////////////////

  /**
   * the current path ordinal
   *
   * @return
   */
  def pathOrdinal: FeltRoutePathOrdinal

  def pathOrdinal_=(po: FeltRoutePathOrdinal): Unit

  /**
   * the current step ordinal
   *
   * @return
   */
  def stepOrdinal: FeltRouteStepOrdinal

  def stepOrdinal_=(po: FeltRouteStepOrdinal): Unit

  /**
   * the current step key
   *
   * @return
   */
  def stepKey: FeltRouteStepKey

  def stepKey_=(key: FeltRouteStepKey): Unit

  /**
   * the current step tag
   *
   * @return
   */
  def stepTag: FeltRouteStepTag

  def stepTag_=(tag: FeltRouteStepTag): Unit

  /**
   * the current step time
   *
   * @return
   */
  def stepTime: FeltRouteStepTime

  def stepTime_=(time: FeltRouteStepTime): Unit

  /**
   * did this step record a 'full' (not partial) path?
   *
   * @return
   */
  def isComplete: Boolean

  def isComplete_=(state: Boolean): Unit

  def isLastStepInPath: Boolean

  def isLastStepInPath_=(state: Boolean): Unit

  /**
   * is this step tacit?
   *
   * @return
   */
  def isTacit: Boolean

  def isTacit_=(state: Boolean): Unit


  /**
   * has this step been rewritten?
   *
   * @return
   */
  def isRewritten: Boolean

  def isRewritten_=(state: Boolean): Unit

  //////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  //////////////////////////////////////////////////////////////////////////////

  /**
   * initialize (reset) this entry
   */
  def initialize(): Unit

  /**
   * get the journal entry after this one
   *
   * @deprecated this should NOT be a public method
   *             When GIST goes away, make this private.
   * @return
   */
  def next: FeltRouteEntry

  def previous: FeltRouteEntry

  /**
   * is this journal entry present?
   *
   * @deprecated this should NOT be a public method
   *             When GIST goes away, make this private.
   * @return
   */
  def validEntry: Boolean

}
