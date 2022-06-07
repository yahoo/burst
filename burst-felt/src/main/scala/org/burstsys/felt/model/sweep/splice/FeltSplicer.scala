/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep.splice

import org.burstsys.felt.model.sweep._

/**
 * marker trait for felt nodes that are a source of possible ''static'' splices.
 * To generate a [[FeltSweep]] you need to
 * find and collect all instances of this (as well as instances of [[FeltDynamicSplicer]]
 */
trait FeltSplicer extends Any {

  /**
   * collect splices that are to be tied to static i.e. brio schema traversal points
   *
   * @return
   */
  def collectSplices: Array[FeltSplice]

}
