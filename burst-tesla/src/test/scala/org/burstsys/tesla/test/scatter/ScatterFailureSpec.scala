/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.scatter

import org.burstsys.tesla.scatter.{TeslaScatterFail, TeslaScatterSucceed, slot}
import org.burstsys.tesla.test.support.TeslaScatterAbstractSpec

import scala.concurrent.duration._
import scala.language.postfixOps

class ScatterFailureSpec extends TeslaScatterAbstractSpec {

  var propagateSlotFailure = true

  override def beforeEach(): Unit = {
    super.beforeEach()
    propagateSlotFailure = true
  }

  override def onSlotFailed(update: slot.TeslaScatterSlotFail): Unit = {
    super.onSlotFailed(update)
    if (propagateSlotFailure)
      scatter.scatterFail(update.throwable)
  }

  override def onScatterFail(update: TeslaScatterFail): Unit = finish(scatterFailCount)

  override def onScatterSucceed(update: TeslaScatterSucceed): Unit = finish(scatterSucceedCount)

  "scatters" should "fail if there are failed slots when the scatter ends" in {
    propagateSlotFailure = false
    runScatter(scatter => {
      scatter.addRequestSlot(MockRequest(succeeds = true, succeedAfter = 10 millis))
      scatter.addRequestSlot(MockRequest(fails = true, failAfter = 10 millis))
    })

    assertSlotCounts(begin = 2, succeed = 1, fail = 1)
    assertScatterCounts(begin = 1, fail = 1)
  }

  they should "cancel running slots when the scatter fails" in {
    runScatter(scatter => {
      scatter.addRequestSlot(MockRequest(succeeds = true, succeedAfter = 20 millis))
      scatter.addRequestSlot(MockRequest(fails = true, failAfter = 10 millis))
    })

    assertSlotCounts(begin = 2, fail = 1, cancel = 1)
    assertScatterCounts(begin = 1, fail = 1)
  }

   they should "not forget about successful slots if the scatter fails" in {
     runScatter(scatter => {
       scatter.addRequestSlot(MockRequest(succeeds = true, succeedAfter = 10 millis))
       scatter.addRequestSlot(MockRequest(fails = true, failAfter = 20 millis))
     })

     assertSlotCounts(begin = 2, fail = 1, succeed = 1)
     assertScatterCounts(begin = 1, fail = 1)

     scatter.successes.length should equal(1)
     scatter.failures.length should equal(1)
   }

}
