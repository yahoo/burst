/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.gather

import org.burstsys.fabric.execution.FabricResourceHolder
import org.burstsys.fabric.execution.model.gather.metrics.FabricOutcome

/**
 * These are the merge operations that happen in the wave/scan/merge cycle.
 */
trait FabricMerge extends FabricOutcome with FabricResourceHolder {

  /////////////////////////////////////////////////////////////////////////////////
  // WORKER SIDE OPERATIONS
  /////////////////////////////////////////////////////////////////////////////////

  /**
   * merge blobs in a region on a worker
   *
   * @param merge
   */
  def regionMerge(merge: FabricMerge): Unit

  /**
   * merge regions in a slice on a worker
   *
   * @param merge
   */
  def sliceMerge(merge: FabricMerge): Unit

  /**
   * called on the last merge on the worker node
   *
   * @return
   */
  def sliceFinalize(): Unit

  /////////////////////////////////////////////////////////////////////////////////
  // Supervisor SIDE OPERATIONS
  /////////////////////////////////////////////////////////////////////////////////

  /**
   * merge slices in a scatter on a supervisor
   *
   * @param merge
   */
  def waveMerge(merge: FabricMerge): Unit

  /**
   * called on the supervisor for the very last merge of the very last in the list of results into a final result
   *
   * @return
   */
  def waveFinalize(): Unit

}
