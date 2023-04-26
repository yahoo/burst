/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.parcel

import java.util.concurrent.atomic.LongAdder
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.tesla.parcel._
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.test.support.TeslaAbstractSpec
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.reporter.instrument._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

//@Ignore
class TeslaParcelPipeConcurrencySpec extends TeslaAbstractSpec {

  val sep = "-----------------------------------------------------------------------------"

  //////////////////////////////////////////////////////////////////////////////////////////////
  // THREADING
  //////////////////////////////////////////////////////////////////////////////////////////////

  behavior of "TeslaParcelPipe"

  Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 18, 24) foreach { concurrency =>
    it should s"handle concurrency of $concurrency" in {
      pushParcelsThroughPipe(concurrency, packets = 1e6)
    }
  }

  private def pushParcelsThroughPipe(concurrency: Int, packets: Double): Unit = {
    val start = System.nanoTime()
    var packetSentTally = new LongAdder
    var packetRecvTally = new LongAdder
    val gate = new CountDownLatch(concurrency)
    val pipe = TeslaParcelPipe(name = "nexus.concurrency.tests", guid = "g1", suid = "s1", depth = 100, timeout = 5 minute).start
    val outcomes = for (_ <- 0 until concurrency) yield {
      TeslaRequestFuture {
        while (packetSentTally.sum < packets) {
          pipe.put(TeslaHeartbeatMarkerParcel)
          packetSentTally.increment()
        }
        pipe.put(TeslaEndMarkerParcel)
      }
    }
    var finishes = 0
    while (finishes != concurrency) {
      pipe.take match {
        case TeslaEndMarkerParcel =>
          gate.countDown()
          finishes += 1
        case TeslaHeartbeatMarkerParcel =>
          packetRecvTally.increment()
        case TeslaTimeoutMarkerParcel =>
          throw VitalsException(s"timeout!!!")
      }
    }

    Await.result(Future.sequence(outcomes), 1 minute)

    log info
      s"""
         |---------------------------------------------------------------------------------------------------
         |  concurrency=$concurrency ,
         |  items/thread = ${packets / concurrency} ,
         |  packets=$packets ,
         |  packetSentTally=${packetSentTally.sum()} ,
         |  packetRecvTally=${packetRecvTally.sum()} ,
         |  ${prettyRateString("packet", packetSentTally.sum(), System.nanoTime - start)} ,
         |---------------------------------------------------------------------------------------------------
       """.stripMargin

    gate.await(120, TimeUnit.SECONDS) should equal(true)
  }
}
