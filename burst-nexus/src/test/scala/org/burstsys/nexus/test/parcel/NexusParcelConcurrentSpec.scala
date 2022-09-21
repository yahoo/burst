/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel

import org.burstsys.nexus._
import org.burstsys.nexus.client.NexusClientListener
import org.burstsys.nexus.message.NexusStreamInitiateMsg
import org.burstsys.nexus.message.NexusStreamInitiatedMsg
import org.burstsys.nexus.server.NexusServerListener
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.NexusSpec
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.net.getPublicHostName
import org.burstsys.vitals.properties._

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

//@Ignore
class NexusParcelConcurrentSpec extends NexusSpec {

  it should "start concurrent parcel streams on current clients" in {
    TeslaRequestCoupler {
      val guid = newNexusUid

      val concurrency = 50
      val streamInitiateCount = new CountDownLatch(concurrency)
      val clientFinishedCount = new CountDownLatch(concurrency)

      val server = grabServer(getPublicHostAddress)
      server talksTo new NexusServerListener {
        override def onStreamInitiate(stream: NexusStream, request: NexusStreamInitiateMsg): Unit = {
          stream.guid should equal(guid)
          streamInitiateCount.countDown()
        }
      }
      server fedBy new NexusStreamFeeder {
        override def feedStream(stream: NexusStream): Unit = {
          Thread.sleep(5)
          stream.complete(0, 0, 0, 0)
        }

        override def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
          fail("we should not get a parcel abort")
        }
      }

      try {

        val futures = new ArrayBuffer[Future[Boolean]]

        val clientListener = new NexusClientListener {
          override def onStreamInitiated(response: NexusStreamInitiatedMsg): Unit = {
            response.guid should equal(guid)
            streamInitiateCount.countDown()
          }
        }

        for (_ <- 0 until concurrency) {

          futures += TeslaRequestFuture {

            val client = grabClientFromPool(getPublicHostAddress, server.serverPort) talksTo clientListener
            try {
              val suid = newNexusUid
              val pipe = TeslaParcelPipe(name = "mock", guid = guid, suid = suid).start

              val streamProperties: VitalsPropertyMap = Map("someKey" -> "someValue")
              val motifFilter: BurstMotifFilter = Some("someMotifFilter")

              val stream = client.startStream(guid, suid, streamProperties, "quo", motifFilter, pipe, 0, getPublicHostName, getPublicHostName)

              clientFinishedCount.countDown()

              Await.result(stream.receipt, 5 seconds)

            } finally releaseClientToPool(client)
            true
          }
        }

        val results = Await.result(Future.sequence(futures), 60 seconds)

        results.length should equal(concurrency)

        results foreach { result => result should equal(true) }

        streamInitiateCount.await(60, TimeUnit.SECONDS) should equal(true)
        clientFinishedCount.await(60, TimeUnit.SECONDS) should equal(true)

      } finally {
        client.shutdownPool
        releaseServer(server)
      }
    }
  }


}
