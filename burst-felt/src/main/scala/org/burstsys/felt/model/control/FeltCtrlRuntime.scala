/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control

import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.felt.model.sweep.runtime.FeltRuntimeComponent

/**
 * Sweep runtime code generated routines for control verb management
 */
trait FeltCtrlRuntime extends Any with FeltRuntimeComponent {

  /**
   * return true if all queries in an analysis agree that
   * no more members of a given vector should be iterated at
   * a given `currentPath`
   *
   * @param currentPath the current path in the traversal
   * @return
   */
  @inline
  def skipControlMemberPath(currentPath: BrioPathKey): Boolean = false

  /**
   * return true if all queries in an analysis agree that
   * no more child relations of a given relation should be explored at
   * a given `currentPath`.
   *
   * @param currentPath the current path in the traversal
   * @return
   */
  @inline
  def skipControlRelationPath(currentPath: BrioPathKey): Boolean = false

  /**
   * return true if a given point in the traversal is in an active abort scope.
   * Get out of this traversal as fast as possible and throw out all collector data
   *
   * @param path
   * @return
   */
  @inline
  def activeAbortScope(path: BrioPathKey): Boolean = false

  /**
   * return true if a given point in the traversal is in an active commit scope.
   * Get out of this traversal as fast as possible keeping all collector data
   *
   * @param path
   * @return
   */
  @inline
  def activeCommitScope(path: BrioPathKey): Boolean = false

  /**
   * return true if a given point in the traversal is the root of the abort scope.
   * reset abort processing and return to normal traversal processing
   *
   * @param path
   * @return
   */
  @inline
  def rootAbortScope(path: BrioPathKey): Boolean = false

  /**
   * return true if a given point in the traversal is the root of the commit scope.
   * reset commit processing and return to normal traversal processing
   *
   * @param path
   * @return
   */
  @inline
  def rootCommitScope(path: BrioPathKey): Boolean = false

}
