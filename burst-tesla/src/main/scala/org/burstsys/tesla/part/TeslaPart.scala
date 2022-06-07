/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.part

import org.burstsys.vitals.errors.VitalsException

object TeslaPart {
  final val MAX_PERMISSIBLE_REFS: Int = 1
}

/**
 * For off heap objects, we need to do a very simple reference counting approach. Note that currently
 * this is only either '''1''' ''allocated'' or '''0''' ''free'd'' (these parts are never ''shared''
 * across contexts and/or threads)
 */
trait TeslaPart extends Any {

  /**
   * increment the reference count for this part iff the reference count is at least 1 less than [[TeslaPart.MAX_PERMISSIBLE_REFS]]
   * otherwise throw an exception
   */
  def validateAndIncrementReferenceCount(msg: String = ""): Unit

  /**
   * decrement the reference count for this part iff the reference count is positive,
   * otherwise throw an exception to mark a excess free of the part
   */
  def validateAndDecrementReferenceCount(msg: String = ""): Unit

  /**
   * return the current value for the reference count
   *
   * @return
   */
  def referenceCount: Int

}
