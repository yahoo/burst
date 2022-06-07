/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.server

import org.burstsys.nexus
import org.burstsys.nexus.server.NexusServer
import org.burstsys.nexus.test.NexusSpec
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.net.getPublicHostAddress
import org.scalatest.BeforeAndAfterEach

class NexusLifecycleSpec extends NexusSpec with BeforeAndAfterEach {

  var server: NexusServer = _

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    server = nexus.grabServer(getPublicHostAddress)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    if (server != null) {
      server.stopIfNotAlreadyStopped
    }
  }

  "NexusClient" should "server.stop should cause clients to become invalid" in {
    TeslaRequestCoupler {
      var client = nexus.grabClientFromPool(getPublicHostAddress, server.serverPort)
      val firstClientId = client.clientId

      client.isConnected shouldBe true

      server.stop
      Thread.sleep(250)

      client.isConnected shouldBe false
      client.isRunning shouldBe false
      nexus.releaseClientToPool(client)

      server = nexus.grabServer(getPublicHostAddress)
      client = nexus.grabClientFromPool(getPublicHostAddress, server.serverPort)

      client.clientId should not equal firstClientId
      client.isConnected shouldBe true
      nexus.releaseClientToPool(client)
    }
  }

  "NexusServer" should "client.stop should not cause the server to shutdown" in {
    TeslaRequestCoupler {
      val client1 = nexus.grabClientFromPool(getPublicHostAddress, server.serverPort)
      val client2 = nexus.grabClientFromPool(getPublicHostAddress, server.serverPort)

      client1.isConnected shouldBe true
      client2.isConnected shouldBe true

      val client1Id = client1.clientId
      client1.stop
      Thread.sleep(250)

      client1.isConnected shouldBe false
      client2.isConnected shouldBe true
      server.isRunning shouldBe true

      nexus.releaseClientToPool(client1)
      val client3 = nexus.grabClientFromPool(getPublicHostAddress, server.serverPort)
      client3.clientId should not equal client1Id
    }
  }

}
