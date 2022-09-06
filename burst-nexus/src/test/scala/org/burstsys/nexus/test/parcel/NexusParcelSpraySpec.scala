/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel

import org.burstsys.brio.flurry.provider.unity.BurstUnityMockData
import org.burstsys.nexus.server.NexusServer
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.NexusSpec
import org.burstsys.nexus.client
import org.burstsys.nexus._
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.net.getPublicHostName

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.Random

//@Ignore
class NexusParcelSpraySpec extends NexusSpec {

  it should "spray a random number of parcels across multiple clients" in {

    TeslaRequestCoupler {

      val items = new ArrayBuffer[TeslaMutableBuffer]

      val server = grabServer(getPublicHostAddress) fedBy new NexusStreamFeeder {

        override
        def feedStream(stream: NexusStream): Unit = {
          val parcels = new ArrayBuffer[TeslaParcel]()
          val itemCount: Int = math.abs(Random.nextInt() % 10)
          parcels ++= BurstUnityMockData().pressToInflatedParcels.take(itemCount)
          log info s"spraying $itemCount parcel(s) to ${stream.guid}"

          TeslaRequestFuture {
            try {
              parcels.foreach(stream put)
              stream put TeslaEndMarkerParcel
            } catch safely {
              case t: Throwable => throw t
            }
          }
        }

        override
        def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
          fail("we should not get a parcel abort")
        }

      }
      try {

        val connectionOutcomes = for (i <- 0 until 10) yield {
          TeslaRequestFuture {
            testClient(server)
          }
        }

        Await.result(Future.sequence(connectionOutcomes), 10 minutes)

      } finally {
        client.shutdownPool
        releaseServer(server)
      }
    }
  }

  private
  def testClient(server: NexusServer): Seq[TeslaParcel] = {
    val client = grabClientFromPool(getPublicHostAddress, server.serverPort)

    try {
      val guid = newNexusUid
      val suid = newNexusUid
      val pipe = TeslaParcelPipe(name = "mock", guid = guid, suid = suid).start
      val stream = client.startStream(guid, suid, Map("someKey" -> "someValue"), "unity", Some("someMotifFilter"), pipe, 0, getPublicHostName, getPublicHostName)

      val results = new ArrayBuffer[TeslaParcel]

      var continue = true
      while (continue) {
        val parcel = stream.take
        if (parcel == TeslaEndMarkerParcel) {
          continue = false
        } else {
          results += parcel
        }
      }

      log info s"received ${results.size} parcels(s) from ${stream.guid}"

      Await.result(stream.receipt, 60 seconds)
      results.toSeq
    } finally {
      releaseClientToPool(client)
    }
  }
}
