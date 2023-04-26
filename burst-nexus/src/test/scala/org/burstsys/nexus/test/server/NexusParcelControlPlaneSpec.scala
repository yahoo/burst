/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.server

import org.burstsys.nexus._
import org.burstsys.nexus.client.NexusClientListener
import org.burstsys.nexus.message._
import org.burstsys.nexus.server.{NexusServerListener, NexusStreamFeeder}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.{NexusParcelStreamSpec, NexusSpec, NoAbortStreamFeeder}
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}
import org.burstsys.vitals.properties._

import java.util.concurrent.CountDownLatch
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class NexusParcelControlPlaneSpec extends NexusParcelStreamSpec {

  private val guid = newNexusUid
  private val gate = new CountDownLatch(2)

  override def feeder: NexusStreamFeeder = new NoAbortStreamFeeder {
    override def feedStream(stream: NexusStream): Unit = {
      stream.complete(0, 0, 0, 0)
    }
  }

  override def serverListener: NexusServerListener = new NexusServerListener {
    override def onStreamInitiate(stream: NexusStream, request: NexusStreamInitiateMsg): Unit = {
      stream.guid should equal(guid)
      gate.countDown()
    }
  }


  override def clientListener: NexusClientListener = new NexusClientListener {
    override def onStreamInitiated(response: NexusStreamInitiatedMsg): Unit = {
      response.guid should equal(guid)
      gate.countDown()
    }
  }

  it should "start parcel stream" in {
    TeslaRequestCoupler {
      val stream = startStream(guid)
      gate.await()
      Await.result(stream.completion, 5 seconds)
    }
  }
}
