/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.topology

import org.burstsys.fabric.container.supervisor.{FabricSupervisorListener, MockSupervisorContainer}
import org.burstsys.fabric.container.worker.{FabricWorkerListener, MockWorkerContainer}
import org.burstsys.fabric.test.{FabricSupervisorWorkerBaseSpec, MockTestSupervisorContainer, MockTestWorkerContainer}
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.scalatest.Ignore

import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.concurrent.Future
import scala.language.postfixOps

/**
 * basic unit test for single supervisor / multiple worker
 */
@Ignore   // Random Address In Use errors on Linux
class FabricLateSupervisorWorkerBootSpec extends FabricSupervisorWorkerBaseSpec
  with FabricSupervisorListener with FabricWorkerListener with FabricTopologyListener {

  override def wantsContainers = true

  override def workerCount = 10

  var workersF: Array[Future[MockTestWorkerContainer]] = _
  var workers: Array[MockTestWorkerContainer] = _

  override def configureSupervisor(supervisor: MockTestSupervisorContainer): Unit = {
    supervisor.talksTo(this)
    supervisor.topology.talksTo(this)
  }

  override def configureWorker(worker: MockTestWorkerContainer): Unit = {
    worker.talksTo(this)
  }

  var gate = new CountDownLatch(workerCount)

  override protected
  def beforeAll(): Unit = {

    configureSupervisor(supervisorContainer)
    workers = (1 until workerCount + 1).indices.map({ i =>
      val worker = MockWorkerContainer[FabricWorkerListener](logFile = "fabric", containerId = i)
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
    supervisorContainer = MockSupervisorContainer[FabricSupervisorListener](logFile = "fabric", containerId = 1)
    configureSupervisor(supervisorContainer)
    gate = new CountDownLatch(workerCount)
    gate.await(3, TimeUnit.SECONDS) should equal(false)
    Thread.sleep(10000)
    supervisorContainer.start

    /* wait for workers to reconnect */
    gate.await(20, TimeUnit.SECONDS) should equal(true)
  }

  override def onTopologyWorkerGain(worker: FabricTopologyWorker): Unit = {
    gate.countDown()
  }
}

