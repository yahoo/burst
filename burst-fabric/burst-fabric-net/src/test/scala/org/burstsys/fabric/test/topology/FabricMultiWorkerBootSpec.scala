/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.topology

import org.burstsys.fabric.container.supervisor.FabricSupervisorListener
import org.burstsys.fabric.container.worker.FabricWorkerListener
import org.burstsys.fabric.test.{FabricSupervisorWorkerBaseSpec, MockTestSupervisorContainer, MockTestWorkerContainer}
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener

import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.language.postfixOps

/**
  * basic unit test for single supervisor / multiple worker
  */
class FabricMultiWorkerBootSpec extends FabricSupervisorWorkerBaseSpec
  with FabricSupervisorListener with FabricWorkerListener with FabricTopologyListener {

  override def wantsContainers = true

  override def workerCount = 10

  override def configureSupervisor(supervisor: MockTestSupervisorContainer): Unit = {
    supervisor.talksTo(this)
    supervisor.topology.talksTo(this)
  }

  override def configureWorker(worker: MockTestWorkerContainer): Unit = {
    worker.talksTo(this)
  }

  val gate = new CountDownLatch(workerCount)

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
  }

  override protected
  def afterAll(): Unit = {
    workerContainers.foreach(_.stop)
    supervisorContainer.stop
  }


  it should "start multiple workers" in {

    log debug "awaiting worker starts"
    gate.await(20, TimeUnit.SECONDS) should equal(true)

    log debug "asserting lengths"
    supervisorContainer.topology.healthyWorkers.length should equal(workerCount)

  }

  override def onTopologyWorkerGained(worker: FabricTopologyWorker): Unit = {
    log debug s"worker ${worker.nodeId} gain"
    gate.countDown()
  }
}
