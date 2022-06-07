/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.background

import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.test.VitalsAbstractSpec
import org.burstsys.vitals.test.log

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._
import scala.language.postfixOps

class VitalsBackgrounderSpec extends VitalsAbstractSpec {

  "VitalsBackgrounder" should "be normal startup" in {
    val counter = new AtomicInteger
    val foo = new VitalsBackgroundFunction("test", 1 seconds, 1 seconds, {
      log info s"XXXXXXXXXXXX"
      counter.incrementAndGet()
    })

    foo.start
    Thread.sleep((4 seconds).toMillis)
    foo.stop

    // we expect ~3 calls to the closure, but the number is variable thanks to scheduling jitter
    counter.get should be > 2
    counter.get should be < 5
  }

  "VitalsBackgrounder" should "do delayed startup" in {
    val counter = new AtomicInteger
    val foo = new VitalsBackgroundFunction("test", 10 seconds, 1 seconds, {
      log info s"YYYYYYYY"
      counter.incrementAndGet()
    })

    foo.start
    foo.stop

    // the closure shouldn't have been called thanks to the delayed startup
    counter.get should equal(0)
  }

  "VitalsBackgrounder" should "complete delayed body" in {
    val counter = new AtomicInteger
    val foo = new VitalsBackgroundFunction("test", 1 seconds, 1 seconds, {
      log info s"AAAA"
      counter.incrementAndGet()
      Thread.sleep(5000)
      log info s"BBBB"
      counter.incrementAndGet()
    })

    foo.start
    Thread.sleep(2000)
    counter.get should equal(1)

    foo.stop
    counter.get should equal(2)
  }


}
