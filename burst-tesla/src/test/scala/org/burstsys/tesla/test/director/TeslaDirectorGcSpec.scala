/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.director

import java.util.concurrent.CountDownLatch

import org.burstsys.tesla.director
import org.burstsys.tesla.parcel.factory
import org.burstsys.tesla.test.support.TeslaAbstractSpec
import org.burstsys.tesla.thread.worker.TeslaWorkerFuture
import org.scalatest.Ignore

/**
 * used to measure GC  under a profiler - not for generic functional testing
 * generally this should be [[Ignore]]
 */
@Ignore
class TeslaDirectorGcSpec extends TeslaAbstractSpec {

  it should "not GC churn directors" in {
      for (i <- 0 until 1e8.toInt) {
        val countDownLatch = new CountDownLatch(8)
        for (j <- 0 until 8) {
          TeslaWorkerFuture {
            var part = director.factory.grabDirector(1e6.toInt)
            director.factory.releaseDirector(part)
            countDownLatch.countDown()
            log info s"PASS $i THREAD $j DONE"
          }
        }
        countDownLatch.await()
        log info s"PASS $i DONE"
      }
  }

}
