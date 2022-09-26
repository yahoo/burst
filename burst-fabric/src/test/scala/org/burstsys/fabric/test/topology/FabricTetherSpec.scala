/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.topology

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.fabric
import org.burstsys.fabric.net.client.FabricNetClientListener
import org.burstsys.fabric.net.message.assess.FabricNetTetherMsg
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.test.FabricSupervisorWorkerBaseSpec
import org.scalatest.Ignore

class FabricTetherSpec extends FabricSupervisorWorkerBaseSpec with FabricNetServerListener with FabricNetClientListener {

  override def wantsContainers = true

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
    supervisorContainer.netServer.talksTo(this)
    workerContainer1.netClient.talksTo(this)
  }

  val gate = new CountDownLatch(1)

  it should "start and tether" in {
    gate.await(15, TimeUnit.SECONDS) should equal(true)
  }

  override
  def onNetServerTetherMsg(c: FabricNetServerConnection, msg: FabricNetTetherMsg): Unit = gate.countDown()
}
