/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.topology

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.fabric
import org.burstsys.fabric.container.master.MockMasterContainer
import org.burstsys.fabric.container.worker.MockWorkerContainer
import org.burstsys.fabric.net.client.FabricNetClientListener
import org.burstsys.fabric.net.message.assess.FabricNetTetherMsg
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.test.FabricMasterWorkerBaseSpec
import org.burstsys.fabric.topology.master.FabricTopologyListener
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode

class FabricTopologySpec extends FabricMasterWorkerBaseSpec
  with FabricNetServerListener with FabricNetClientListener with FabricTopologyListener {


  override protected def wantsContainers = true

  override protected def workerCount = 10

  override def configureMaster(master: MockMasterContainer): Unit = {
    master.netServer.talksTo(this)
    master.topology.talksTo(this)
  }

  override def configureWorker(worker: MockWorkerContainer): Unit = {
    worker.netClient.talksTo(this)
  }

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
  }

  def latch(count: Int = workerCount): CountDownLatch = new CountDownLatch(count)

  private var tether = latch()
  private var disconnect = latch()
  private var workerGain = latch()
  private var workerLoss = latch()

  it should "initiate a topology" in {
    tether.await(15, TimeUnit.SECONDS) shouldEqual true
    workerGain.await(15, TimeUnit.SECONDS) shouldEqual true
    masterContainer.topology.healthyWorkers.length should equal(workerCount)

    workerContainers.foreach(_.stop)
    Thread.sleep(100)

    disconnect.await(15, TimeUnit.SECONDS) shouldEqual true
    workerLoss.await(15, TimeUnit.SECONDS) shouldEqual true
    masterContainer.topology.healthyWorkers.length should equal(0)

    tether = latch()
    workerGain = latch()
    workerContainers.foreach(_.start)
    Thread.sleep(100)

    tether.await(15, TimeUnit.SECONDS) shouldEqual true
    workerGain.await(15, TimeUnit.SECONDS) shouldEqual true
    masterContainer.topology.healthyWorkers.length should equal(workerCount)

    disconnect = latch(workerCount / 2)
    workerLoss = latch(workerCount / 2)
    workerContainers.indices.foreach {
      case i if i % 2 == 0 => workerContainers(i).stop
      case _ =>
    }
    Thread.sleep(100)

    disconnect.await(15, TimeUnit.SECONDS) shouldEqual true
    workerLoss.await(15, TimeUnit.SECONDS) shouldEqual true
    masterContainer.topology.healthyWorkers.length should equal(workerCount / 2)
  }

  override
  def onNetServerTetherMsg(c: FabricNetServerConnection, msg: FabricNetTetherMsg): Unit = tether.countDown()

  override
  def onNetServerDisconnect(c: FabricNetServerConnection): Unit = disconnect.countDown()

  override
  def onTopologyWorkerGained(worker: FabricWorkerNode): Unit = workerGain.countDown()

  override
  def onTopologyWorkerLoss(worker: FabricWorkerNode): Unit = workerLoss.countDown()
}
