/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.scatter

import org.burstsys.tesla.scatter.slot.TeslaScatterSlot
import org.burstsys.tesla.scatter.{TeslaScatterFail, TeslaScatterSucceed}
import org.burstsys.tesla.test.support.TeslaScatterAbstractSpec
import org.burstsys.vitals.errors.VitalsException

import scala.concurrent.duration._
import scala.language.postfixOps

class SlotLifecycleSpec extends TeslaScatterAbstractSpec {

  override def onScatterSucceed(update: TeslaScatterSucceed): Unit = finish(scatterSucceedCount)
  override def onScatterFail(update: TeslaScatterFail): Unit = finish(scatterFailCount)

  "scatters" should "provide exactly one update per-TTL when slots are tardy" in {
    runScatter(scatter => {
      scatter.addRequestSlot(MockRequest(tardyInterval = 100 millis, succeeds = true, succeedAfter = 850 millis))
    })

    assertScatterCounts(begin = 1, succeed = 1)
    assertSlotCounts(begin = 1, succeed = 1, tardy = 8)

    scatter.successes.length should equal(1)
    scatter.zombies.length should equal(0)
  }

  they should "not allow slots to succeed twice" in {
    runScatter(scatter => {
      scatter.addRequestSlot(
        MockRequest(succeeds = true, succeedAfter = 10 millis, after = Some(req => req.slot.slotSuccess()))
      )
    })

    assertScatterCounts(begin = 1, succeed = 1)
    assertSlotCounts(begin = 1, succeed = 1)

    // slot would be in here twice if we succeed twice
    scatter.successes.length should equal(1)
  }

  they should "not allow slots to fail twice" in {
    runScatter(scatter => {
      scatter.addRequestSlot(
        MockRequest(fails = true, failAfter = 10 millis, after = Some(req => req.fail("second failure")))
      )
    })

    assertScatterCounts(begin = 1, fail = 1)
    assertSlotCounts(begin = 1, fail = 1)

    // slot would be in here twice if we fail twice
    scatter.failures.length should equal(1)
  }

  they should "not allow slots to both succeed and fail" in {
    var failSuccess: TeslaScatterSlot = null
    var successFail: TeslaScatterSlot = null
    runScatter(scatter => {
      failSuccess = scatter.addRequestSlot(
        MockRequest(fails = true, failAfter = 10 millis, after = Some(req => req.slot.slotSuccess()))
      )
      successFail = scatter.addRequestSlot(
        MockRequest(succeeds =  true, succeedAfter = 10 millis, after = Some(req => req.slot.slotFailed(VitalsException("Never received"))))
      )
    })

    assertScatterCounts(begin = 1, fail = 1)
    assertSlotCounts(begin = 2, succeed = 1, fail = 1)

    scatter.successes.length should equal(1)
    scatter.successes should contain(successFail)

    scatter.failures.length should equal(1)
    scatter.failures should contain(failSuccess)
  }
}
