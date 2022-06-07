/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree.source

import org.burstsys.vitals.strings._

/**
 * a FELT AST normalized source code ''source''
 */
trait FeltSource extends Any {

  /**
   * These routines recursively generate a Felt Tree as a ``normalized`` version of the source that created it.
   * This normalized form contains any tree transformations applied such as static optimizations. It also generates a
   * consistent deterministic white space usage. The normalized for can uniquely identify a unique set of ``equivalent``
   * queries.
   *
   * @param index
   * @return
   */
  def normalizedSource(implicit index: Int = 0): String

  final
  def printSource: String = {
    val condensed = this.normalizedSource.condensed.noMultiSpace.trim
    if (condensed.length < columnWidth) {
      condensed
    } else {
      s"'${condensed.trim.take(columnWidth - 3)}...'"
    }
  }


}
