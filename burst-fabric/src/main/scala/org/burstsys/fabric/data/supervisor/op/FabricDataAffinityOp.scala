/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.supervisor.op

import org.burstsys.fabric.data.supervisor.{FabricSupervisorData, FabricSupervisorDataContext}
import org.burstsys.fabric.data.model.slice.FabricSlice
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode

/**
  * determine zero or more appropriate locations for a given slice
 * TODO SOMEONE PLEASE IMPLEMENT ME!
  */
trait FabricDataAffinityOp extends FabricSupervisorData {

  self: FabricSupervisorDataContext =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TODO keep track of loads and where a given slice is likely to be

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def affineWorkers(slice: FabricSlice): Array[FabricWorkerNode] = {
    ???
  }

}
