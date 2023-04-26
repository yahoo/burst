/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel.streams

import org.burstsys.brio.flurry.provider.unity.BurstUnityMockData
import org.burstsys.nexus._
import org.burstsys.nexus.message.NexusStreamInitiateMsg
import org.burstsys.nexus.server.{NexusServerListener, NexusStreamFeeder}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.{NexusParcelStreamSpec, NoAbortStreamFeeder}
import org.burstsys.tesla
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.thread.request.{TeslaRequestCoupler, TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.errors.VitalsException

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

//@Ignore
class NexusParcelPepperSpec extends NexusParcelStreamSpec {


  private val bigClientUid: NexusUid = "BIG_STREAM"
  private val threadId = new AtomicInteger()

  override def feeder: NexusStreamFeeder = new NoAbortStreamFeeder {
    override def feedStream(stream: NexusStream): Unit = {
      TeslaRequestFuture {
        val name = Thread.currentThread.getName
        Thread.currentThread setName s"test-feeder-${threadId.getAndIncrement()}"
        val itemCount = if (stream.guid == bigClientUid) 10e3.toInt else 5e1.toInt
        log info s"feedStream ${stream.guid} with $itemCount items"

        val buffers = BurstUnityMockData(itemCount).pressToBuffers
        buffers.foreach(stream put)
        stream.complete(buffers.length, buffers.length, buffers.length, rejectedItemCount = 0)
        Thread.currentThread.setName(name)
      }
    }
  }

  override def serverListener: NexusServerListener = new NexusServerListener {
    override def onStreamInitiate(stream: NexusStream, request: NexusStreamInitiateMsg): Unit = {
      log info s"onStreamInitiate ${stream.guid}"
    }
  }


  it should "start big transfer and pepper with small transfers" in {
    TeslaRequestCoupler {
      log info s"start big transfer"
      val bigClient = TeslaRequestFuture {
        val name = Thread.currentThread.getName
        Thread.currentThread setName s"BIG_TRANSFER"
        streamFromServer(bigClientUid)
        Thread.currentThread.setName(name)
      }

      log info s"pepper with small transfer(s)"
      val smallClientOutcomes = for (i <- 0 until 10 by 4) yield {
        val futures = for (j <- 0 until 4) yield {
          TeslaRequestFuture {
            val name = Thread.currentThread.getName
            Thread.currentThread setName s"SMALL_TRANSFER_${i + j}"
            streamFromServer(s"small-transfer-${i + j}")
            Thread.currentThread.setName(name)
          }
        }
        Thread.sleep(math.abs(Random.nextInt() % 2000))
        futures
      }

      log info s"wait for small transfer"
      Await.result(Future.sequence(smallClientOutcomes.flatten), 10 minutes)

      log info s"wait for big transfer"
      Await.result(bigClient, 10 minutes)
    }
  }

  private def streamFromServer(guid: String): Unit = {
    withClient { client =>
      val stream = startStream(guid, client = client)

      var continue = true
      while (continue) {
        val parcel = stream.take
        if (parcel == TeslaEndMarkerParcel) {
          continue = false
        } else if (parcel.status.isHeartbeat) {
          // ignore this
        } else if (parcel.status.isMarker) {
          throw VitalsException(s"Got unexpected marker parcel status=${parcel.status}")
        } else {
          tesla.parcel.factory releaseParcel parcel
        }
        log info s"read data from pipe finished guid=$guid"

        Await.result(stream.completion, 60 seconds)
        log info s"stream finished guid=$guid"
      }
    }
  }

}
