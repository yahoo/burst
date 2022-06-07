/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.tablet

import java.util.concurrent.CountDownLatch

import org.burstsys.felt.model.collectors.tablet.FeltTabletBuilder
import org.burstsys.tesla.thread.worker.TeslaWorkerFuture
import org.burstsys.zap.tablet
import org.burstsys.zap.tablet.ZapTabletBuilder
import org.burstsys.zap.test.ZapAbstractSpec
import org.scalatest.Ignore

/**
 * used to measure GC  under a profiler - not for generic functional testing
 * generally this should be [[Ignore]]
 */
@Ignore
class ZapTabletGcSpec extends ZapAbstractSpec {

  val builder: FeltTabletBuilder = ZapTabletBuilder()

  it should "not GC churn tablets" in {
    for (i <- 0 until 1e8.toInt) {
      val countDownLatch = new CountDownLatch(8)
      for (j <- 0 until 8) {
        TeslaWorkerFuture {
          var part = tablet.factory.grabZapTablet(builder)
          tablet.factory.releaseZapTablet(part)
          countDownLatch.countDown()
          log info s"PASS $i THREAD $j DONE"
        }
      }
      countDownLatch.await()
      log info s"PASS $i DONE"
    }
  }

}
