/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.route

import java.util.concurrent.CountDownLatch

import org.burstsys.tesla.thread.worker.TeslaWorkerFuture
import org.burstsys.zap.route
import org.burstsys.zap.route.ZapRouteBuilder
import org.burstsys.zap.test.ZapAbstractSpec
import org.scalatest.Ignore

/**
 * used to measure GC  under a profiler - not for generic functional testing
 * generally this should be [[Ignore]]
 */
@Ignore
class ZapRouteGcSpec extends ZapAbstractSpec {

  val builder: ZapRouteBuilder = ZapRouteBuilder().init(0, "gc_spec", null)

  it should "not GC churn routes" in {
    for (i <- 0 until 1e8.toInt) {
      val countDownLatch = new CountDownLatch(8)
      for (j <- 0 until 8) {
        TeslaWorkerFuture {
          var part = route.factory.grabZapRoute(builder)
          route.factory.releaseZapRoute(part)
          countDownLatch.countDown()
          log info s"PASS $i THREAD $j DONE"
        }
      }
      countDownLatch.await()
      log info s"PASS $i DONE"
    }
  }

}
