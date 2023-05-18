/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.gather.control

import org.burstsys.fabric.wave.execution.model.gather.{FabricGather, FabricGatherContext, FabricMerge}

/**
 * Gathers that do not return data/results - used for utility functions and exceptional conditions
 */
trait FabricControlGather extends FabricGather

/**
 * base implementations for all control gathers. mostly noops
 */
abstract
class FabricControlGatherContext() extends FabricGatherContext with FabricControlGather {

  /////////////////////////////////////////////////////////////////////////
  // merge
  /////////////////////////////////////////////////////////////////////////

  final override def regionMerge(merge: FabricMerge): Unit = {}

  final override def sliceMerge(merge: FabricMerge): Unit = {}

  final override def waveMerge(merge: FabricMerge): Unit = {}

  /////////////////////////////////////////////////////////////////////////
  // finalize
  /////////////////////////////////////////////////////////////////////////

  final override def sliceFinalize(): Unit = {}

  final override def waveFinalize(): Unit = {}

}
