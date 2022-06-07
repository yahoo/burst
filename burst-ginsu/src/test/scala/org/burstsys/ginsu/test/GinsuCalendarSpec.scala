/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.test

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.ginsu.functions.GinsuFunctions
import org.burstsys.vitals.time.VitalsTimeZones._
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GinsuCalendarSpec extends AnyFlatSpec with Matchers with GinsuFunctions with BrioThreadRuntime {
  final implicit def threadRuntime: BrioThreadRuntime = this

  final val formatString = "YYYY.MM.dd KK:mm:ss aa"

  var timeZone: DateTimeZone = BurstDefaultTimeZone

  "Burst Metrics Calendar Day" should "calculate correctly in default time zone" in {
    timeZone = BurstDefaultTimeZone
    prepareBrioThreadRuntime(BurstDefaultTimeZone)
    val time = new DateTime(2014, 10, 9, 10, 7, 0, timeZone)
    val ts = new DateTime(time.getMillis, timeZone).toString(formatString)
    ts should equal("2014.10.09 10:07:00 AM")

    val cd = dayGrain(time.getMillis)
    val cds = new DateTime(cd, timeZone).toString(formatString)
    cds should equal("2014.10.09 00:00:00 AM")
  }

  "Burst Metrics Calendar Day" should "calculate correctly in Australia/Sydney" in {
    timeZone = DateTimeZone.forID("Australia/Sydney")
    prepareBrioThreadRuntime(timeZone)
    val time = new DateTime(2014, 10, 9, 10, 7, 0, timeZone)
    val ts = new DateTime(time.getMillis, timeZone).toString(formatString)
    ts should equal("2014.10.09 10:07:00 AM")

    val cd = dayGrain(time.getMillis)
    val cds = new DateTime(cd, timeZone).toString(formatString)
    cds should equal("2014.10.09 00:00:00 AM")
  }

  "Burst Metrics Calendar Day" should "calculate correctly in Africa/Dakar" in {
    timeZone = DateTimeZone.forID("Africa/Dakar")
    prepareBrioThreadRuntime(timeZone)
    val time = new DateTime(2014, 10, 9, 10, 7, 0, timeZone)
    val ts = new DateTime(time.getMillis, timeZone).toString(formatString)
    ts should equal("2014.10.09 10:07:00 AM")

    val cd = dayGrain(time.getMillis)
    val cds = new DateTime(cd, timeZone).toString(formatString)
    cds should equal("2014.10.09 00:00:00 AM")
  }

  "Burst Metrics Time" should "calculate zones correctly" in {
    prepareBrioThreadRuntime(BurstDefaultTimeZone)
    val x = keyForZone(VitalsDefaultTimeZoneName).get
    val y = zoneNameForKey(x).get
    y should equal(VitalsDefaultTimeZoneName)
  }

}
