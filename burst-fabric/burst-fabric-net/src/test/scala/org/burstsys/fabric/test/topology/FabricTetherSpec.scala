/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.topology

import org.burstsys.fabric.container.supervisor.FabricSupervisorListener
import org.burstsys.fabric.container.worker.FabricWorkerListener
import org.burstsys.fabric.net.message.assess.FabricNetHeartbeatMsg
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.test.FabricSupervisorWorkerBaseSpec

import java.util.concurrent.{CountDownLatch, TimeUnit}

class FabricTetherSpec extends FabricSupervisorWorkerBaseSpec with FabricSupervisorListener with FabricWorkerListener {

  override def wantsContainers = true

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
    supervisorContainer.talksTo(this)
    workerContainer1.talksTo(this)
  }

  val gate = new CountDownLatch(1)

  it should "start and tether" in {
    gate.await(15, TimeUnit.SECONDS) should equal(true)
  }

  override
  def onNetServerTetherMsg(c: FabricNetServerConnection, msg: FabricNetHeartbeatMsg): Unit = gate.countDown()
}
