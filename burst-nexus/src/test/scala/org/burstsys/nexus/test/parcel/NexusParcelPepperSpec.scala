/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel

import org.burstsys.brio.flurry.provider.unity.BurstUnityMockData
import org.burstsys.nexus.message.NexusStreamInitiateMsg
import org.burstsys.nexus.server.NexusServer
import org.burstsys.nexus.server.NexusServerListener
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.NexusSpec
import org.burstsys.nexus.client
import org.burstsys.nexus._
import org.burstsys.tesla
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.net.getPublicHostName
import org.burstsys.vitals.properties._

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.Random

//@Ignore
class NexusParcelPepperSpec extends NexusSpec {

  def print(msg: String): String =
    s"""
       |-----------------------------------------------------------------------------------
       |  $msg
       |-----------------------------------------------------------------------------------""".stripMargin


  val bigClientUid: NexusUid = "BIG_STREAM"
  val threadId = new AtomicInteger()

  it should "start big xfer and pepper with small parcels" in {

    TeslaRequestCoupler {
      val feeder = new NexusStreamFeeder {
        override def feedStream(stream: NexusStream): Unit = {
          TeslaRequestFuture {
            Thread.currentThread setName s"test-feeder${threadId.getAndIncrement()}"
            val itemCount = if (stream.guid == bigClientUid) 10e3.toInt else 5e1.toInt
            log info print(s"feedStream ${stream.guid} with $itemCount items")

            BurstUnityMockData(itemCount).pressToInflatedParcels.foreach(stream put)
            stream put TeslaEndMarkerParcel
          }
        }

        override def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
          fail("we should not get a parcel abort")
        }
      }

      val listener = new NexusServerListener {
        override def onStreamInitiate(stream: NexusStream, request: NexusStreamInitiateMsg): Unit = {
          log info print(s"onStreamInitiate ${stream.guid}")
        }
      }

      log info print(s"starting server")
      val server = grabServer(getPublicHostAddress) fedBy feeder talksTo listener

      try {
        log info print(s"start big xfer")
        val bigClient = TeslaRequestFuture {
          Thread.currentThread setName s"BIG_XFER_WAIT"
          startBigXfer(server)
        }

        log info print(s"pepper with small xfer(s)")
        val smallClientOutcomes = for (i <- 0 until 10 by 4) yield {
          val futures = for (j <- 0 until 4) yield {
            TeslaRequestFuture {
              Thread.currentThread setName s"SMALL_XFER${i + j}_WAIT"
              startSmallXfer(i + j, server)
            }
          }
          Thread.sleep(math.abs(Random.nextInt() % 2000))
          futures
        }

        log info print(s"wait for small xfer")
        Await.result(Future.sequence(smallClientOutcomes.flatten), 10 minutes)

        log info print(s"wait for big xfer")
        Await.result(bigClient, 10 minutes)

      } finally {
        client.shutdownPool
        releaseServer(server)
      }
    }
  }

  private
  def startSmallXfer(xferId: Int, server: NexusServer): Unit = {
    val client = grabClientFromPool(getPublicHostAddress, server.serverPort)
    try {
      val guid = newNexusUid
      val suid = newNexusUid
      val pipe = TeslaParcelPipe(name = "mock", guid = guid, suid = suid).start
      val streamProperties: VitalsPropertyMap = Map("someKey" -> "someValue")
      val motifFilter: BurstMotifFilter = Some("someMotifFilter")
      val stream = client.startStream(guid, suid, streamProperties, "quo", motifFilter, pipe, 0, getPublicHostName, getPublicHostName)
      Await.result(stream.receipt, 60 seconds)

      var continue = true
      while (continue) {
        val parcel = pipe.take
        if (parcel == TeslaEndMarkerParcel) {
          continue = false
        } else if (parcel == TeslaTimeoutMarkerParcel) {
          throw VitalsException(s"TeslaParcelPipeTimeout")
        } else {
          tesla.parcel.factory releaseParcel parcel
        }

      }
      log info print(s"small xfer $xferId recieved")

    } finally releaseClientToPool(client)
  }

  private
  def startBigXfer(server: NexusServer): Unit = {
    val client = grabClientFromPool(getPublicHostAddress, server.serverPort)
    try {
      val suid = newNexusUid
      val pipe = TeslaParcelPipe(name = "mock", guid = bigClientUid, suid = suid).start
      val streamProperties: VitalsPropertyMap = Map("someKey" -> "someValue")
      val motifFilter: BurstMotifFilter = Some("someMotifFilter")
      val stream = client.startStream(bigClientUid, suid, streamProperties, "quo", motifFilter, pipe, 0, getPublicHostName, getPublicHostName)

      var continue = true
      while (continue) {
        val parcel = pipe.take
        if (parcel == TeslaEndMarkerParcel) {
          continue = false
        } else if (parcel == TeslaTimeoutMarkerParcel) {
          throw VitalsException(s"TeslaParcelPipeTimeout")
        } else {
          tesla.parcel.factory releaseParcel parcel
        }
      }
      log info print(s"large xfer received...")

      Await.result(stream.receipt, 60 seconds)
    } finally releaseClientToPool(client)
  }

}
