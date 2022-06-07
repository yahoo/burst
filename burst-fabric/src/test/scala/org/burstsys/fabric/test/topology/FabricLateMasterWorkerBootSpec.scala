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
import org.scalatest.Ignore

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * basic unit test for single master / multiple worker
 */
@Ignore   // Random Address In Use errors on Linux
class FabricLateMasterWorkerBootSpec extends FabricMasterWorkerBaseSpec
  with FabricNetServerListener with FabricNetClientListener with FabricTopologyListener {

  override def wantsContainers = true

  override def workerCount = 10

  var workersF: Array[Future[MockWorkerContainer]] = _
  var workers: Array[MockWorkerContainer] = _

  override def configureMaster(master: MockMasterContainer): Unit = {
    master.netServer.talksTo(this)
    master.topology.talksTo(this)
  }

  override def configureWorker(worker: MockWorkerContainer): Unit = {
    worker.netClient.talksTo(this)
  }

  var gate = new CountDownLatch(workerCount)

  override protected
  def beforeAll(): Unit = {
    masterContainer.metadata withLookup this

    configureMaster(masterContainer)
    workers = (1 until workerCount + 1).indices.map({ i =>
      val worker = MockWorkerContainer(logFile = "fabric", containerId = i)
      configureWorker(worker)
      worker.start
    }).toArray
  }

  override protected
  def afterAll(): Unit = {
    workers.foreach(_.stop)
    masterContainer.stop
  }


  /**
   * Start the master after the workers and then bring the master down and back up
   */
  it should "start multiple workers waiting for a flaky master" in {

    Thread.sleep(30000)
    gate.await(3, TimeUnit.SECONDS) should equal(false)

    masterContainer.start

    gate.await(20, TimeUnit.SECONDS) should equal(true)

    masterContainer.topology.healthyWorkers.length should equal(workerCount)

    /* stop container */
    masterContainer.stop

    /* start new one in it's place */
    masterContainer = MockMasterContainer(logFile = "fabric", containerId = 1)
    masterContainer.metadata withLookup this
    configureMaster(masterContainer)
    gate = new CountDownLatch(workerCount)
    gate.await(3, TimeUnit.SECONDS) should equal(false)
    Thread.sleep(10000)
    masterContainer.start

    /* wait for workers to reconnect */
    gate.await(20, TimeUnit.SECONDS) should equal(true)
  }

  override def onTopologyWorkerGain(worker: FabricWorkerNode): Unit = {
    gate.countDown()
  }
}

