/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits.runtime

import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.sweep.FeltSweepComponent
import org.burstsys.felt.model.sweep.splice.FeltPlacementKey

/**
 * A series of immutable decisions (its in the Sweep, not the Runtime) about which static
 * schema paths to follow in this analysis.
 */
trait FeltVisitSweep extends Any with FeltSweepComponent {

  /**
   * return true if a given felt brio schema subtree path should not be scanned. This is a performance
   * optimization.
   *
   * @param path
   * @return
   */
  @inline
  def skipVisitPath(path: BrioPathKey): Boolean = false

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // which reference scalar relations tunnels to skip
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * return true if a given scalar reference path should not be 'tunneled'.
   *
   * @param path
   * @return
   */
  @inline
  def skipTunnelPath(path: BrioPathKey): Boolean = false

  /**
   * process dynamic relation splices
   * at a given traversal point defined as a path key
   * @param runtime
   * @param pathKey
   * @param placement
   */
  @inline
  def dynamicRelationSplices(runtime: FeltRuntime, pathKey: BrioPathKey, placement: FeltPlacementKey): Unit

}
