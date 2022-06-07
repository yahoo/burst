/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.mutable

import java.util.concurrent.CountDownLatch

import org.burstsys.tesla.thread.worker.TeslaWorkerFuture
import org.burstsys.zap.mutable.valarray.ZapValArrayBuilder
import org.burstsys.zap.mutable.{valarray, valset}
import org.burstsys.zap.mutable.valset.ZapValSetBuilder
import org.burstsys.zap.test.ZapAbstractSpec
import org.scalatest.Ignore

/**
 * used to measure GC  under a profiler - not for generic functional testing
 * generally this should be [[Ignore]]
 */
@Ignore
class ZapValArrayGcSpec extends ZapAbstractSpec {

  val builder: ZapValArrayBuilder = ZapValArrayBuilder()

  it should "not GC churn val array" in {
    for (i <- 0 until 1e8.toInt) {
      val countDownLatch = new CountDownLatch(8)
      for (j <- 0 until 8) {
        TeslaWorkerFuture {
          var part = valarray.factory.grabValArray(builder)
          valarray.factory.releaseValArray(part)
          countDownLatch.countDown()
          log info s"PASS $i THREAD $j DONE"
        }
      }
      countDownLatch.await()
      log info s"PASS $i DONE"
    }
  }
  it should "not GC churn flex val array" in {
    for (i <- 0 until 1e8.toInt) {
      val countDownLatch = new CountDownLatch(8)
      for (j <- 0 until 8) {
        TeslaWorkerFuture {
          var part = valarray.flex.grabValArray(builder)
          valarray.flex.releaseValArray(part)
          countDownLatch.countDown()
          log info s"PASS $i THREAD $j DONE"
        }
      }
      countDownLatch.await()
      log info s"PASS $i DONE"
    }
  }

}
