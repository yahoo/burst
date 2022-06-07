/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.dictionary

import java.util.concurrent.CountDownLatch

import org.burstsys.brio.dictionary._
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.worker.{TeslaWorkerCoupler, TeslaWorkerFuture}
import org.scalatest.Ignore

/**
 * used to measure GC  under a profiler - not for generic functional testing
 * generally this should be [[Ignore]]
 */
@Ignore
class BrioDictionaryGcSpec extends BrioAbstractSpec {

  it should "not GC churn normal dictionaries" in {
      for (i <- 0 until 1e8.toInt) {
        val countDownLatch = new CountDownLatch(8)
        for (j <- 0 until 8) {
          TeslaWorkerFuture {
            var mutableDictionary = factory.grabMutableDictionary()
            factory.releaseMutableDictionary(mutableDictionary)
            countDownLatch.countDown()
            log info s"PASS $i THREAD $j DONE"
          }
        }
        countDownLatch.await()
        log info s"PASS $i DONE"
      }
  }

  it should "not GC churn flex dictionaries" in {
    for (i <- 0 until 1e8.toInt) {
      val countDownLatch = new CountDownLatch(8)
      for (j <- 0 until 8) {
        TeslaWorkerFuture {
          var flexDictionary = flex.grabFlexDictionary()
          flex.releaseFlexDictionary(flexDictionary)
          countDownLatch.countDown()
          log info s"PASS $i THREAD $j DONE"
        }
      }
      countDownLatch.await()
      log info s"PASS $i DONE"
    }
  }
}
