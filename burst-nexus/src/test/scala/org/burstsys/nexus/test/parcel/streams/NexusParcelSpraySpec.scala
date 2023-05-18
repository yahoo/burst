/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel.streams

import org.burstsys.brio.flurry.provider.unity.BurstUnityMockData
import org.burstsys.nexus._
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.{NexusParcelStreamSpec, NoAbortStreamFeeder}
import org.burstsys.tesla
import org.burstsys.tesla.thread.request.{TeslaRequestCoupler, TeslaRequestFuture, teslaRequestExecutor}

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.Random

class NexusParcelSpraySpec extends NexusParcelStreamSpec {

  private val streamCounts = new ConcurrentHashMap[String, Int]()

  override def feeder: NexusStreamFeeder = new NoAbortStreamFeeder {
    override def feedStream(stream: NexusStream): Unit = {
      TeslaRequestFuture {
        val itemCount: Int = math.abs(Random.nextInt().abs % 100)
        streamCounts.put(stream.guid, itemCount)
        log info s"spraying $itemCount items(s) to ${stream.guid}"

        BurstUnityMockData(itemCount).pressToBuffers.foreach(stream put)
        stream.complete(itemCount, expectedItemCount = itemCount, potentialItemCount = itemCount, rejectedItemCount = 0)
      }
    }
  }

  it should "spray a random number of parcels across multiple clients" in {
    TeslaRequestCoupler {
      val connectionOutcomes = for (i <- 0 until 10) yield {
        TeslaRequestFuture {
          withClient { client =>
            val guid = newNexusUid
            val stream = startStream(guid, client = client)

            var parcelCount = 0

            var continue = true
            while (continue) {
              val parcel = stream.take
              if (parcel.status.isHeartbeat) {
                // skip this
              } else if (parcel.status.isMarker) {
                log info s"received marker status=${parcel.status} ending stream $i"
                continue = false
              } else {
                tesla.parcel.factory.releaseParcel(parcel)
                parcelCount += 1
              }
            }

            log info s"received all parcels stream=${stream.guid} count=$parcelCount"
            Await.result(stream.completion, 10 seconds)
            log info s"stream complete stream=${stream.guid}"
            stream.itemCount shouldEqual streamCounts.get(stream.guid)
          }
        }
      }
      log info "Generated all futures"
      Await.result(Future.sequence(connectionOutcomes), 10 seconds)
      log info "All futures complete"
    }
  }

}
