/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.runtime

import org.burstsys.brio.blob.BrioBlob
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.execution.model.execute.invoke.FabricInvocation
import org.burstsys.fabric.wave.execution.model.gather.plane.FabricPlaneGather
import org.burstsys.felt.model.collectors.runtime.FeltCollectorPlane
import org.burstsys.felt.model.sweep.FeltSweep

/**
 * runtime object for a [[FeltSweep]] that includes collectors
 * This is a reusable (pooled) resources that holds the state for a
 * specific mutable and non shared per blob traversal runtime instance (all other structures are immutable and shared)
 */
trait FeltPlaneRuntime extends FeltRuntime {

  /**
   * at the beginning of a scan/traversal, make this re-useable runtime object ready for a new pass
   * (this is only on the worker side)
   *
   * @param snap
   * @param blob
   * @param gather
   * @return
   */
  def prepare(snap: FabricSnap, blob: BrioBlob, gather: FabricPlaneGather): FeltPlaneRuntime

  /**
   * at the end of a scan/traversal, deal with any outstanding allocated resources
   * in this re-useable runtime object before putting back in the pool
   *
   * @param gather
   * @return
   */
  def release(gather: FabricPlaneGather): FeltPlaneRuntime

}

abstract
class FeltPlaneRuntimeContext(invocation: FabricInvocation)
  extends FeltRuntimeContext(invocation) with FeltPlaneRuntime {

  final override
  def prepare(snap: FabricSnap, blob: BrioBlob, gather: FabricPlaneGather): FeltPlaneRuntime = {
    super.prepare(snap, blob)
    /**
     * at the start of the traversal, for each of the parallel planes,
     * we take the plane gather collectors and dictionaries and write them
     * into the runtime frame
     */
    var i = 0
    while (i < gather.activePlanes) {
      val plane = gather.planes(i).asInstanceOf[FeltCollectorPlane[_, _]]
      // write the plane collector into the frame
      frameCollector(i, plane.planeCollector)
      // write the plane dictionary into the frame
      frameDictionary(i, plane.planeDictionary)
      i += 1
    }
    this
  }

  final override
  def release(gather: FabricPlaneGather): FeltPlaneRuntime = {
    super.release
    /**
     * at the end of the traversal, for each of the parallel planes,
     * we take the runtime frame collector and dictionaries and write them
     * into the plane
     */
    var i = 0
    while (i < gather.activePlanes) {
      val plane = gather.planes(i).asInstanceOf[FeltCollectorPlane[_, _]]
      // write the frame collector into the plane
      plane.planeCollector = frameCollector(i)
      // write the frame dictionary into the plane
      plane.planeDictionary = frameDictionary(i)
      i += 1
    }
    this
  }

}
