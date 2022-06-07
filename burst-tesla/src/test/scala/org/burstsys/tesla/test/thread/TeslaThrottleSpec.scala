/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.thread

import org.burstsys.tesla.test.support.TeslaSpecLog
import org.burstsys.tesla.thread.TeslaThreadThrottle
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.request._
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps


@Ignore
class TeslaThrottleSpec extends AnyFlatSpec with Matchers with TeslaSpecLog {

  it should "throttle fairly" in {

    val entrance = new ArrayBuffer[Int]
    val exit = new ArrayBuffer[Int]

    val throttle = TeslaThreadThrottle("mock", 1)
    val futures = for(i <- 0 until 60) yield {
      Thread.sleep(1)
      TeslaRequestFuture {
        Thread.currentThread.setName(f"T-$i%02d")
        entrance += i
        throttle {
          log info s"thread $i body"
        }
        exit += i
      }
    }
    Await.ready(Future.sequence(futures), 10 minutes)
    true

  }

}
