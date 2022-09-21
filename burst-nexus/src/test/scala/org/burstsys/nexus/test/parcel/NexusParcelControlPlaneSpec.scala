/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel

import java.util.concurrent.CountDownLatch

import org.burstsys.nexus._
import org.burstsys.nexus.client.NexusClientListener
import org.burstsys.nexus.message._
import org.burstsys.nexus.server.{NexusServerListener, NexusStreamFeeder}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.NexusSpec
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}
import org.burstsys.vitals.properties._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

//@Ignore
class NexusParcelControlPlaneSpec extends NexusSpec {


  it should "start parcel stream" in {
    TeslaRequestCoupler {
      val guid = newNexusUid

      val gate = new CountDownLatch(2)

      val server = grabServer(getPublicHostAddress)
      server talksTo new NexusServerListener {
        override def onStreamInitiate(stream: NexusStream, request: NexusStreamInitiateMsg): Unit = {
          stream.guid should equal(guid)
          gate.countDown()
        }
      }
      server fedBy new NexusStreamFeeder {
        override def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
          fail("we should not get a parcel abort")
        }

        override def feedStream(stream: NexusStream): Unit = {
          stream.complete(0, 0, 0, 0)
        }
      }

      try {

        val client = grabClientFromPool(getPublicHostAddress, server.serverPort) talksTo new NexusClientListener {
          override
          def onStreamInitiated(response: NexusStreamInitiatedMsg): Unit = {
            response.guid should equal(guid)
            gate.countDown()
          }
        }

        try {
          val suid = newNexusUid
          val pipe = TeslaParcelPipe(name = "mock", guid = guid, suid = suid).start

          val streamProperties: VitalsPropertyMap = Map("someKey" -> "someValue")
          val motifFilter: BurstMotifFilter = Some("someMotifFilter")
          val stream = client.startStream(guid, suid, streamProperties, "quo", motifFilter, pipe, 0, getPublicHostName, getPublicHostName)

          gate.await()

          Await.result(stream.receipt, 60 seconds)
        } finally releaseClientToPool(client)

      } finally {
        client.shutdownPool
        releaseServer(server)
      }
    }
  }
}
