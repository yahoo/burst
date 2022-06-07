/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.wheel

import java.util.concurrent.CountDownLatch

import org.burstsys.tesla.thread.worker.TeslaWorkerFuture
import org.burstsys.zap.route.ZapRouteBuilder
import org.burstsys.zap.{route, wheel}
import org.burstsys.zap.test.ZapAbstractSpec
import org.burstsys.zap.wheel.ZapWheelBuilder
import org.scalatest.Ignore

/**
 * used to measure GC  under a profiler - not for generic functional testing
 * generally this should be [[Ignore]]
 */
@Ignore
class ZapWheelGcSpec extends ZapAbstractSpec {

  val builder: ZapWheelBuilder = ZapWheelBuilder()

  it should "not GC churn wheels" in {
      for (i <- 0 until 1e8.toInt) {
        val countDownLatch = new CountDownLatch(8)
        for (j <- 0 until 8) {
          TeslaWorkerFuture {
            var part = wheel.factory.grabZapWheel(builder)
            wheel.factory.releaseZapWheel(part)
            countDownLatch.countDown()
            log info s"PASS $i THREAD $j DONE"
          }
        }
        countDownLatch.await()
        log info s"PASS $i DONE"
      }
  }

}
