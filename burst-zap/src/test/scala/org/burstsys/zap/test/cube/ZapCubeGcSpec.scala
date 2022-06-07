/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube

import java.util.concurrent.CountDownLatch

import org.burstsys.brio
import org.burstsys.brio.dictionary.factory._
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.worker.{TeslaWorkerCoupler, TeslaWorkerFuture}
import org.burstsys.zap.cube.ZapCubeContext
import org.burstsys.zap.cube.factory.{grabZapCube, releaseZapCube}
import org.burstsys.zap.{cube, route}
import org.burstsys.zap.test.ZapAbstractSpec
import org.scalatest.Ignore

/**
 * used to measure GC  under a profiler - not for generic functional testing
 * generally this should be [[Ignore]]
 */
@Ignore
class ZapCubeGcSpec extends ZapAbstractSpec {

  import ZapTestData._

  it should "not GC churn cubes" in {
    for (i <- 0 until 1e8.toInt) {
      val countDownLatch = new CountDownLatch(8)
      for (j <- 0 until 8) {
        TeslaWorkerFuture {
          val dictionary = brio.dictionary.factory.grabMutableDictionary()
          val part = cube.factory.grabZapCube(s/*, dictionary*/)
          cube.factory.releaseZapCube(part)
          brio.dictionary.factory.releaseMutableDictionary(dictionary)
          countDownLatch.countDown()
          log info s"PASS $i THREAD $j DONE"
        }
      }
      countDownLatch.await()
      log info s"PASS $i DONE"
    }
  }

  it should "not GC churn cubes with flex dictionaries" in {
    for (i <- 0 until 1e8.toInt) {
      val countDownLatch = new CountDownLatch(8)
      for (j <- 0 until 8) {
        TeslaWorkerFuture {
          val dictionary = brio.dictionary.flex.grabFlexDictionary()
          val part = cube.factory.grabZapCube(s/*, dictionary*/)
          cube.factory.releaseZapCube(part)
          brio.dictionary.flex.releaseFlexDictionary(dictionary)
          countDownLatch.countDown()
          log info s"PASS $i THREAD $j DONE"
        }
      }
      countDownLatch.await()
      log info s"PASS $i DONE"
    }
  }

}
