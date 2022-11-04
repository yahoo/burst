/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.supervisor

import org.burstsys.fabric.topology.FabricTopologyWorker

/**
  * changes in the state of the overall topology of workers
  */
trait FabricTopologyListener extends Any {

  /**
    * a new worker in connected and ready for work
    */
  def onTopologyWorkerGain(worker: FabricTopologyWorker): Unit = {}

  /**
   * called to let listeners know that other listeners now know about the worker
   */
  def onTopologyWorkerGained(worker: FabricTopologyWorker): Unit = {}

  /**
    * a existing worker has been lost
    */
  def onTopologyWorkerLoss(worker: FabricTopologyWorker): Unit = {}

  /**
   * called to let listeners know that other listeners know the worker was lost
   */
  def onTopologyWorkerLost(worker: FabricTopologyWorker): Unit = {}

  /**
    * a significant change has happened to a worker other than gain or loss
    */
  def onTopologyWorkerChange(worker: FabricTopologyWorker): Unit = {}

}
