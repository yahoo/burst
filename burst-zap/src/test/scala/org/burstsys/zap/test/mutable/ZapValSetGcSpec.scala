/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.mutable

import java.util.concurrent.CountDownLatch

import org.burstsys.tesla.thread.worker.TeslaWorkerFuture
import org.burstsys.zap.mutable.valset
import org.burstsys.zap.mutable.valset.ZapValSetBuilder
import org.burstsys.zap.route
import org.burstsys.zap.route.ZapRouteBuilder
import org.burstsys.zap.test.ZapAbstractSpec
import org.scalatest.Ignore

/**
 * used to measure GC  under a profiler - not for generic functional testing
 * generally this should be [[Ignore]]
 */
@Ignore
class ZapValSetGcSpec extends ZapAbstractSpec {

  val builder: ZapValSetBuilder = ZapValSetBuilder()

  it should "not GC churn val sets" in {
      for (i <- 0 until 1e8.toInt) {
        val countDownLatch = new CountDownLatch(8)
        for (j <- 0 until 8) {
          TeslaWorkerFuture {
            var part = valset.factory.grabValSet(builder)
            valset.factory.releaseValSet(part)
            countDownLatch.countDown()
            log info s"PASS $i THREAD $j DONE"
          }
        }
        countDownLatch.await()
        log info s"PASS $i DONE"
      }
  }

}
