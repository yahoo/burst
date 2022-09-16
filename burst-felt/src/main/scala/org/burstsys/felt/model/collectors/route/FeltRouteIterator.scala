/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route

/**
 * universal trait supporting iteration over route journal (log) entries
 */
trait FeltRouteIterator extends Any {

  /**
   * start the route iteration at the beginning
   *
   * @return
   */
  def startIteration: Unit

  /**
   * The Route stores a single cursor iteration pointer to a journal entry
   * use #nextIteration or #firstOrNextIterable to initialize this
   *
   * @return
   */
  def currentIteration: FeltRouteEntry

  /**
   * get the first or the next iteration. Returns false if the route is empty
   * or if there are no more entries available.
   *
   * @return
   */
  def firstOrNextIterable: Boolean

}
