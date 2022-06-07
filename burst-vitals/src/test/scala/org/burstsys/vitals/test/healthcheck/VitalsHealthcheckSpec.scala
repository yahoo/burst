/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.healthcheck

import org.burstsys.vitals.healthcheck.VitalsHealthHealthy
import org.burstsys.vitals.healthcheck.VitalsHealthLifetimeComponent
import org.burstsys.vitals.healthcheck.VitalsHealthUnhealthy
import org.burstsys.vitals.test.VitalsAbstractSpec
import org.joda.time.DateTime
import org.joda.time.Period

class VitalsHealthcheckSpec extends VitalsAbstractSpec {

  private val now = DateTime.now().withMillisOfSecond(0)
  private val tenMinutes = Period.parse("PT10M")
  private val oneHour = Period.parse("PT1H")
  private val twoHours = Period.parse("PT2H")
  private val oneDay = Period.parse("P1D")
  private val twoDays = Period.parse("P2D")
  private val fourtyDays = Period.parse("P40D")
  private val oneWeek = Period.parse("P1W")
  private val fiveWeeks = Period.parse("P5W")
  private val oneMonth = Period.parse("P1M")
  private val oneYear = Period.parse("P1Y")

  behavior of "component health"

  it should "report healthy when not expired" in {
    val hc = VitalsHealthLifetimeComponent(null, oneHour)(now)
    hc.componentHealth.status should equal(VitalsHealthHealthy)
  }

  it should "report unhealthy when expired" in {
    val hc = VitalsHealthLifetimeComponent(null, oneHour)(now.minus(twoHours))
    hc.componentHealth.status should equal(VitalsHealthUnhealthy)
  }

  behavior of "period only"

  Array(oneHour, twoHours, oneDay, twoDays, oneWeek, oneMonth, oneYear) foreach { p =>
    it should s"expire after $p when no time of day provided" in {
      VitalsHealthLifetimeComponent(null, p)(now).expireTime should equal(now.plus(p))
    }
  }

  behavior of "fixed duration periods"

  Array(oneHour, twoHours, oneDay, twoDays, fourtyDays, oneWeek, fiveWeeks) foreach { p =>
    it should s"expire today at `time`, when `time` is more than $p in the future" in {
      val timeOfDay = now.plus(tenMinutes).toLocalTime
      VitalsHealthLifetimeComponent(timeOfDay, p)(now).expireTime should equal(now.withTime(timeOfDay))
    }
  }
  Array(oneHour, twoHours, oneDay, twoDays, fourtyDays, oneWeek, fiveWeeks) foreach { p =>
    it should s"expire at `time`, when that is less than $p in the future" in {
      val timeOfDay = now.minus(tenMinutes).toLocalTime
      val afterPeriod = now.plus(p)
      val expireTime =
        if (afterPeriod.withTime(timeOfDay).getMillis > afterPeriod.getMillis) now.withTime(timeOfDay)
        else afterPeriod.withTime(timeOfDay)
      VitalsHealthLifetimeComponent(timeOfDay, p)(now).expireTime should equal(expireTime)
    }
  }

  behavior of "variable duration periods"

  Array(oneMonth, oneYear) foreach { p =>
    it should s"expire after $p, at the specified time of day" in {
      val timeOfDay = now.plus(tenMinutes).toLocalTime
      VitalsHealthLifetimeComponent(timeOfDay, p)(now).expireTime should equal(now.plus(p).withTime(timeOfDay))
    }
  }

}
