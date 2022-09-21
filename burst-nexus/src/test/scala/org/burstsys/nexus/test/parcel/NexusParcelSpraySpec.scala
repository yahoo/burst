/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel

import org.burstsys.brio.flurry.provider.unity.BurstUnityMockData
import org.burstsys.nexus.server.NexusServer
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.NexusSpec
import org.burstsys.nexus.client
import org.burstsys.nexus._
import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.parcel.factory.TeslaParcelPool
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.net.getPublicHostName

import java.util
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.Random

//@Ignore
class NexusParcelSpraySpec extends NexusSpec {

  it should "spray a random number of parcels across multiple clients" in {

    val streamCounts = new util.HashMap[String, Int]()

    TeslaRequestCoupler {
      val server = grabServer(getPublicHostAddress)
      server fedBy new NexusStreamFeeder {
        override def feedStream(stream: NexusStream): Unit = {
          val itemCount: Int = math.abs(Random.nextInt % 100)
          streamCounts.put(stream.guid, itemCount)
          log info s"spraying $itemCount items(s) to ${stream.guid}"

          BurstUnityMockData().pressToBuffers.take(itemCount).foreach(stream put)
          stream.complete(itemCount, expectedItemCount = itemCount, potentialItemCount = itemCount, rejectedItemCount = 0)
        }

        override def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
          fail("we should not get a parcel abort")
        }

      }

      try {
        val connectionOutcomes = for (_ <- 0 until 10) yield {
          TeslaRequestFuture {
            val stream = testClient(server)
            stream.itemCount shouldEqual streamCounts.get(stream.guid)
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
  def testClient(server: NexusServer): NexusStream = {
    val client = grabClientFromPool(getPublicHostAddress, server.serverPort)

    try {
      val guid = newNexusUid
      val suid = newNexusUid
      val pipe = TeslaParcelPipe(name = "mock", guid = guid, suid = suid).start
      val stream = client.startStream(guid, suid, Map("someKey" -> "someValue"), "unity", Some("someMotifFilter"), pipe, 0, getPublicHostName, getPublicHostName)

      var parcelCount = 0

      var continue = true
      while (continue) {
        val parcel = stream.take
        if (parcel == TeslaEndMarkerParcel) {
          continue = false
        } else {
          tesla.parcel.factory.releaseParcel(parcel)
          parcelCount += 1
        }
      }

      log info s"received ${parcelCount} parcels(s) from ${stream.guid}"

      Await.result(stream.receipt, 60 seconds)
      stream
    } finally {
      releaseClientToPool(client)
    }
  }
}
