/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.topology

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.fabric
import org.burstsys.fabric.container.master.MockMasterContainer
import org.burstsys.fabric.container.worker.MockWorkerContainer
import org.burstsys.fabric.net.client.FabricNetClientListener
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.test.FabricMasterWorkerBaseSpec
import org.burstsys.fabric.topology.master.FabricTopologyListener
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode

import scala.language.postfixOps

/**
  * basic unit test for single master / multiple worker
  */
class FabricMultiWorkerBootSpec extends FabricMasterWorkerBaseSpec
  with FabricNetServerListener with FabricNetClientListener with FabricTopologyListener {

  override def wantsContainers = true

  override def workerCount = 10

  override def configureMaster(master: MockMasterContainer): Unit = {
    master.netServer.talksTo(this)
    master.topology.talksTo(this)
  }

  override def configureWorker(worker: MockWorkerContainer): Unit = {
    worker.netClient.talksTo(this)
  }

  val gate = new CountDownLatch(workerCount)

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
  }

  override protected
  def afterAll(): Unit = {
    workerContainers.foreach(_.stop)
    masterContainer.stop
  }


  it should "start multiple workers" in {

    gate.await(20, TimeUnit.SECONDS) should equal(true)
    masterContainer.topology.healthyWorkers.length should equal(workerCount)

  }

  override def onTopologyWorkerGained(worker: FabricWorkerNode): Unit = {
    log info s"worker ${worker.nodeId} gain"
    gate.countDown()
  }
}
