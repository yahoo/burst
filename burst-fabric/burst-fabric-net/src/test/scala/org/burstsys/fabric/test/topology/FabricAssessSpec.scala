/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.topology

import org.burstsys.fabric.container.supervisor.FabricSupervisorListener
import org.burstsys.fabric.container.worker.FabricWorkerListener
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.message.assess.{FabricNetAssessReqMsg, FabricNetAssessRespMsg}
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.test.FabricSupervisorWorkerBaseSpec

import java.util.concurrent.{CountDownLatch, TimeUnit}

class FabricAssessSpec extends FabricSupervisorWorkerBaseSpec with FabricSupervisorListener with FabricWorkerListener {

  override def wantsContainers = true

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
    supervisorContainer.talksTo(this)
    workerContainer1.talksTo(this)
  }


  val serverGate = new CountDownLatch(2)
  val clientGate = new CountDownLatch(2)

  val timeout = 30

  it should "assess" in {
    serverGate.await(timeout, TimeUnit.SECONDS) should equal(true)
    clientGate.await(timeout, TimeUnit.SECONDS) should equal(true)
  }

  override
  def onNetServerAssessRespMsg(c: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = serverGate.countDown()

  override
  def onNetClientAssessReqMsg(c: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = clientGate.countDown()

}
