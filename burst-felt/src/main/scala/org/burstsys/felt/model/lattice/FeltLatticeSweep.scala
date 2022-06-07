/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice

import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.sweep.FeltSweepComponent
import org.burstsys.felt.model.sweep.splice._

/**
 * API to be code generated
 */
trait FeltLatticeSweep extends Any with FeltSweepComponent {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Root Item Processing
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * The code generated splice at the root of the object tree
   *
   * @param runtime
   */
  @inline
  def rootSplice(runtime: FeltRuntime, path: BrioPathKey, placement: FeltPlacementKey): Unit

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Reference Scalar Processing
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * a splice for a reference scala relationship
   *
   * @param path
   * @param runtime
   */
  @inline
  def referenceScalarSplice(runtime: FeltRuntime, path: BrioPathKey, placement: FeltPlacementKey): Unit = {}

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Reference Vector Processing
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * a splice for a reference vector relationship
   *
   * @param path
   * @param runtime
   */
  @inline
  def referenceVectorSplice(runtime: FeltRuntime, path: BrioPathKey, placement: FeltPlacementKey): Unit = {}

  /**
   * a splice for each member of a reference vector relationship
   *
   * @param path
   * @param runtime
   */
  @inline
  def referenceVectorMemberSplice(runtime: FeltRuntime, path: BrioPathKey, placement: FeltPlacementKey): Unit = {}

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Value Map Processing
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * a splice for a value map relationship
   *
   * @param path
   * @param runtime
   */
  @inline
  def valueMapSplice(runtime: FeltRuntime, path: BrioPathKey, placement: FeltPlacementKey): Unit = {}

  /**
   * a splice for each member of a value map relationship
   *
   * @param path
   * @param runtime
   */
  @inline
  def valueMapMemberSplice(runtime: FeltRuntime, path: BrioPathKey, placement: FeltPlacementKey): Unit = {}

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Value Vector Processing
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * a splice for a value vector relationship
   *
   * @param path
   * @param runtime
   */
  @inline
  def valueVectorSplice(runtime: FeltRuntime, path: BrioPathKey, placement: FeltPlacementKey): Unit = {}

  /**
   * a splice for each member of a value vector relationship
   *
   * @param path
   * @param runtime
   */
  @inline
  def valueVectorMemberSplice(runtime: FeltRuntime, path: BrioPathKey, placement: FeltPlacementKey): Unit = {}

}
