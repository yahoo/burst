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
import org.scalatest._

import scala.language.postfixOps

/**
  * basic unit test for multiple master / multiple worker (in progress)
  * TODO multiple master unit tests require multiple master ports in a single JVM - this implies the master port
  * should be in the master metadata object as part of configuration.
  */
@Ignore
class FabricMultiMasterBootSpec extends FabricMasterWorkerBaseSpec
  with FabricNetServerListener with FabricNetClientListener with FabricTopologyListener {

  private val logName = "fabric"

  val masterContainer2 = MockMasterContainer(logFile = logName, containerId = 2)

  override def workerCount = 10

  val gate = new CountDownLatch(workerCount)

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
    masterContainer.netServer.talksTo(this)
    masterContainer.topology.talksTo(this)

    workerContainers.foreach(_.netClient.talksTo(this))
  }

  it should "start multiple masters and multiple workers" in {

    gate.await(20, TimeUnit.SECONDS) should equal(true)

    masterContainer.topology.healthyWorkers.length should equal(workerCount)

  }

  override def onTopologyWorkerGain(worker: FabricWorkerNode): Unit = {
    log info s"worker ${worker.nodeId} gain"
    gate.countDown()
  }
}
