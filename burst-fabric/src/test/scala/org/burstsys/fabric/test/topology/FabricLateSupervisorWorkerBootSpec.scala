/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.topology

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.fabric
import org.burstsys.fabric.container.supervisor.MockSupervisorContainer
import org.burstsys.fabric.container.worker.MockWorkerContainer
import org.burstsys.fabric.net.client.FabricNetClientListener
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.test.FabricSupervisorWorkerBaseSpec
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.scalatest.Ignore

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * basic unit test for single supervisor / multiple worker
 */
@Ignore   // Random Address In Use errors on Linux
class FabricLateSupervisorWorkerBootSpec extends FabricSupervisorWorkerBaseSpec
  with FabricNetServerListener with FabricNetClientListener with FabricTopologyListener {

  override def wantsContainers = true

  override def workerCount = 10

  var workersF: Array[Future[MockWorkerContainer]] = _
  var workers: Array[MockWorkerContainer] = _

  override def configureSupervisor(supervisor: MockSupervisorContainer): Unit = {
    supervisor.netServer.talksTo(this)
    supervisor.topology.talksTo(this)
  }

  override def configureWorker(worker: MockWorkerContainer): Unit = {
    worker.netClient.talksTo(this)
  }

  var gate = new CountDownLatch(workerCount)

  override protected
  def beforeAll(): Unit = {
    supervisorContainer.metadata withLookup this

    configureSupervisor(supervisorContainer)
    workers = (1 until workerCount + 1).indices.map({ i =>
      val worker = MockWorkerContainer(logFile = "fabric", containerId = i)
      configureWorker(worker)
      worker.start
    }).toArray
  }

  override protected
  def afterAll(): Unit = {
    workers.foreach(_.stop)
    supervisorContainer.stop
  }


  /**
   * Start the supervisor after the workers and then bring the supervisor down and back up
   */
  it should "start multiple workers waiting for a flaky supervisor" in {

    Thread.sleep(30000)
    gate.await(3, TimeUnit.SECONDS) should equal(false)

    supervisorContainer.start

    gate.await(20, TimeUnit.SECONDS) should equal(true)

    supervisorContainer.topology.healthyWorkers.length should equal(workerCount)

    /* stop container */
    supervisorContainer.stop

    /* start new one in it's place */
    supervisorContainer = MockSupervisorContainer(logFile = "fabric", containerId = 1)
    supervisorContainer.metadata withLookup this
    configureSupervisor(supervisorContainer)
    gate = new CountDownLatch(workerCount)
    gate.await(3, TimeUnit.SECONDS) should equal(false)
    Thread.sleep(10000)
    supervisorContainer.start

    /* wait for workers to reconnect */
    gate.await(20, TimeUnit.SECONDS) should equal(true)
  }

  override def onTopologyWorkerGain(worker: FabricWorkerNode): Unit = {
    gate.countDown()
  }
}

