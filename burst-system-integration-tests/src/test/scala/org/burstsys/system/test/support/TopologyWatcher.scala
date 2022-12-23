/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.support

import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener

import java.util.concurrent.CountDownLatch

case class TopologyWatcher(workerCount: Int = 1)
  extends FabricTopologyListener
{
  val workerGainGate = new CountDownLatch(workerCount)

  override def onTopologyWorkerGain(worker: FabricTopologyWorker): Unit = {
    log info s"worker ${worker.nodeId} gain"
    workerGainGate.countDown()
  }
}
