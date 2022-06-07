/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.scatter

import org.burstsys.tesla.scatter.TeslaScatterTimeout
import org.burstsys.tesla.test.support.TeslaScatterAbstractSpec
import org.scalatest.Ignore

import scala.concurrent.duration._
import scala.language.postfixOps

@Ignore
class ScatterTimeoutSpec extends TeslaScatterAbstractSpec {

  override def pollWaitDuration: Duration = 15 milliseconds

  override def onScatterTimeout(update: TeslaScatterTimeout): Unit = finish(scatterTimeoutCount)

  "scatters" should "timeout when work never completes" in {
    val foreverRequest = MockRequest(updates = true, updateInterval = 10 millis)
    runScatter(scatter => {
      scatter.addRequestSlot(foreverRequest)
      scatter.timeout = 30.millis
    })

    assertSlotCounts(begin = 1, progress = 3, cancel = 1)
    assertScatterCounts(begin = 1, timeout = 1)

    scatter.zombies.length should equal(1)
    foreverRequest.slot.slotSuccess() // remove this slot from the zombie list
  }

  they should "not forget completed or failed slots on timeout" in {
    val foreverRequest = MockRequest(updates = true, updateInterval = 10 millis)
    runScatter(scatter => {
      scatter.addRequestSlot(foreverRequest)
      scatter.addRequestSlot(MockRequest(succeeds = true, succeedAfter = 10 millis))
      scatter.addRequestSlot(MockRequest(fails = true, failAfter = 10 millis))
      scatter.timeout = 30.millis
    })

    assertSlotCounts(begin = 3, progress = 2, succeed = 1, fail = 1, cancel = 1)
    assertScatterCounts(begin = 1, timeout = 1)

    scatter.zombies.length should equal(1)
    scatter.successes.length should equal(1)
    scatter.failures.length should equal(1)
    foreverRequest.slot.slotSuccess() // remove this slot from the zombie list
  }

  they should "timeout when no updates are received" in {
    val foreverRequest = MockRequest()
    runScatter(scatter => {
      scatter.addRequestSlot(foreverRequest)
      scatter.timeout = 30.millis
    })

    assertSlotCounts(begin = 1, cancel = 1)
    assertScatterCounts(begin = 1, timeout = 1)

    scatter.zombies.length should equal(1)
    foreverRequest.slot.slotSuccess() // remove this slot from the zombie list
  }

}
