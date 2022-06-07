/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.topology

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.fabric
import org.burstsys.fabric.net.client.FabricNetClientListener
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.message.assess.{FabricNetAssessReqMsg, FabricNetAssessRespMsg}
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.test.FabricMasterWorkerBaseSpec
import org.scalatest.Ignore

class FabricAssessSpec extends FabricMasterWorkerBaseSpec with FabricNetServerListener with FabricNetClientListener {

  override def wantsContainers = true

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
    masterContainer.netServer.talksTo(this)
    workerContainer1.netClient.talksTo(this)
  }


  val serverGate = new CountDownLatch(2)
  val clientGate = new CountDownLatch(2)

  it should "assess" in {
    serverGate.await(20, TimeUnit.SECONDS) should equal(true)
    clientGate.await(20, TimeUnit.SECONDS) should equal(true)
  }

  override
  def onNetServerAssessRespMsg(c: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = serverGate.countDown()

  override
  def onNetClientAssessReqMsg(c: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = clientGate.countDown()

}
