/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel.streams

import org.burstsys.nexus
import org.burstsys.nexus._
import org.burstsys.nexus.client.NexusClientListener
import org.burstsys.nexus.message.{NexusStreamInitiateMsg, NexusStreamInitiatedMsg}
import org.burstsys.nexus.server.{NexusServerListener, NexusStreamFeeder}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.{NexusParcelStreamSpec, NexusSpec, NoAbortStreamFeeder}
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.{TeslaRequestCoupler, TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}
import org.burstsys.vitals.properties._

import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

//@Ignore
class NexusParcelConcurrentSpec extends NexusParcelStreamSpec {

  private val guid = newNexusUid
  private val concurrency = 50
  private val streamInitiateCount = new CountDownLatch(concurrency)
  private val streamInitiatedCount = new CountDownLatch(concurrency)
  private val clientFinishedCount = new CountDownLatch(concurrency)

  override def feeder: NexusStreamFeeder = new NoAbortStreamFeeder {
    override def feedStream(stream: NexusStream): Unit = {
      Thread.sleep(15)
      stream.complete(0, 0, 0, 0)
    }
  }
  override def serverListener: NexusServerListener = new NexusServerListener {
    override def onStreamInitiate(stream: NexusStream, request: NexusStreamInitiateMsg): Unit = {
      stream.guid should equal(guid)
      streamInitiateCount.countDown()
    }
  }

  override def clientListener: NexusClientListener = new NexusClientListener {
    override def onStreamInitiated(response: NexusStreamInitiatedMsg): Unit = {
      response.guid should equal(guid)
      streamInitiatedCount.countDown()
    }
  }

  it should "start concurrent parcel streams on current clients" in {
    TeslaRequestCoupler {
      val futures = new ArrayBuffer[Future[Boolean]]

      for (_ <- 0 until concurrency) {
        futures += TeslaRequestFuture {
          withClient(client => {
            val stream = startStream(guid, client = client)
            clientFinishedCount.countDown()
            Await.result(stream.completion, 15 seconds)
          })
          true
        }
      }

      val results = Await.result(Future.sequence(futures), 30 seconds)

      results.length shouldEqual concurrency
      results.foreach(_ shouldEqual true)

      streamInitiateCount.await(30, TimeUnit.SECONDS) shouldEqual true
      streamInitiatedCount.await(30, TimeUnit.SECONDS) shouldEqual true
      clientFinishedCount.await(30, TimeUnit.SECONDS) shouldEqual true
    }
  }


}
