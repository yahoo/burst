/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.views.unity

import org.burstsys.brio.flurry.provider.unity._
import org.burstsys.alloy.BurstUnitRepeatingValue
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.vitals.time.VitalsTimeZones.BurstDefaultTimeZone
import org.joda.time.DateTime
import org.joda.time.Duration

import java.util.NoSuchElementException
import java.util.concurrent.atomic.AtomicLong

object UnityGenerator {

  val defaultGeneratorControls: GeneratorControls = GeneratorControls()

  case
  class GeneratorControls(
                           userIdGenerator: AtomicLong = new AtomicLong(),
                           interests: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(1L to 10L: _*),
                           applicationInstallTimeGenerator: AtomicLong = new AtomicLong(new DateTime(2017, 1, 1, 1, 1, BurstDefaultTimeZone).getMillis),
                           sessionStartTimeGenerator: AtomicLong = new AtomicLong(new DateTime(2018, 1, 1, 1, 1, BurstDefaultTimeZone).getMillis),
                           sessionIdGenerator: AtomicLong = new AtomicLong(),
                           parameterIdGenerator: AtomicLong = new AtomicLong(),
                           applicationId: Long = 12345L,
                           eventIds: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(1L to 11L: _*),
                           deviceModelIds: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(555666L, 666777L, 888999L),
                           osVersionIds: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(232323L, 454545L, 676767L, 898989L),
                           languageIds: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(111222L, 333444L, 555666L, 777888L, 888999L),
                           appVersionIds: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(101010101L, 12121212L, 13131313L, 1414141414L, 1515151515L, 1616161616L),
                           providedOrigins: BurstUnitRepeatingValue[String] = BurstUnitRepeatingValue("origin1", "origin2", "origin3", "origin4", "origin5"),
                           mappedOrigins: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(9876L, 54321L, 54329L),
                           originSourceTypes: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(987L, 986L, 985L, 984L, 983L),
                           originMethodTypes: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L),
                           campaignIds: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(22L, 33L, 44L),
                           localeCountryIds: BurstUnitRepeatingValue[Long] = BurstUnitRepeatingValue(10000L, 20000L, 30000L, 40000L, 50000L, 60000L),
                           sessionParameters: BurstUnitRepeatingValue[Map[String, String]] = BurstUnitRepeatingValue(
                             Map("SK1" -> "SV1"), Map("SK2" -> "SV2"), Map("SK3" -> "SV3"), Map("SK4" -> "SV4"), Map("SK5" -> "SV5"), Map("SK6" -> "SV6"), Map("SK7" -> "SV7")
                           ),
                           eventParameters: BurstUnitRepeatingValue[Map[String, String]] = BurstUnitRepeatingValue(
                             Map("EK1" -> "EV1"), Map("EK2" -> "EV2"), Map("EK3" -> "EV3"), Map("EK4" -> "EV4"), Map("EK5" -> "EV5"), Map("EK6" -> "EV6"), Map("EK7" -> "EV7")
                           ),
                           crashes: BurstUnitRepeatingValue[Boolean] = BurstUnitRepeatingValue(false, true)
                         )

  def generated(userCount: Int, sessionCount: Int, eventCount: Int, parameterCount: Int = 0, controls: GeneratorControls = defaultGeneratorControls): Array[BrioPressInstance] = {
    (1 to userCount).map {
      _ =>
        generateUser(sessionCount,eventCount,parameterCount, controls)
    }.toArray
  }

  def generateIterator(userCount: Int, sessionCount: Int, eventCount: Int, parameterCount: Int, controls: GeneratorControls = defaultGeneratorControls): Iterator[BrioPressInstance] = {
    new Iterator[BrioPressInstance]() {
      val count = new AtomicLong(userCount)
      def hasNext: Boolean = count.get() > 0

      def next(): BrioPressInstance = {
        if (count.decrementAndGet() < 0) {
          throw new NoSuchElementException()
        }
        generateUser(sessionCount,eventCount,parameterCount, controls)
      }
    }
  }

  def generateUser(sessionCount: Int, eventCount: Int, parameterCount: Int, controls: GeneratorControls = defaultGeneratorControls): BrioPressInstance = {
    val it: Array[Long] = if (controls.interests == null) Array.empty else
      Array(controls.interests.next, controls.interests.next)
    val usr =
      UnityMockUser(
        id = s"User${controls.userIdGenerator.incrementAndGet()}",
        deviceModelId = controls.deviceModelIds.next,
        interests = it,
        application = UnityMockApplication(
          id = controls.applicationId,
          channels = if (controls.campaignIds != null)
            Array(UnityMockChannel(campaignId = controls.campaignIds.next)) else Array.empty,
          firstUse = UnityMockUse(
            sessionTime = controls.applicationInstallTimeGenerator.incrementAndGet(),
            osVersionId = controls.osVersionIds.next,
            appVersion = UnityMockAppVersion(id = controls.appVersionIds.next),
            localeCountryId = controls.localeCountryIds.next,
            languageId = controls.languageIds.next
          ),
          lastUse = UnityMockUse(
            sessionTime = controls.applicationInstallTimeGenerator.get() + Duration.standardDays(30).getMillis,
            osVersionId = controls.osVersionIds.value,
            appVersion = UnityMockAppVersion(id = controls.appVersionIds.value),
            localeCountryId = controls.localeCountryIds.value,
            languageId = controls.languageIds.value
          )
        ),
        sessions = (1 to sessionCount).map {
          _ =>
            UnityMockSession(
              id = controls.sessionIdGenerator.incrementAndGet,
              startTime = controls.sessionStartTimeGenerator.incrementAndGet,
              osVersionId = controls.osVersionIds.next,
              appVersion = UnityMockAppVersion(id = controls.appVersionIds.next),
              providedOrigin = controls.providedOrigins.next,
              mappedOriginId = controls.mappedOrigins.next,
              originSourceTypeId = controls.originSourceTypes.next,
              originMethodTypeId = controls.originMethodTypes.next,
              parameters = if (controls.sessionParameters != null)
                (1 to parameterCount).flatMap { _ => controls.sessionParameters.next }.toMap
              else Map.empty,
              events = (1 to eventCount).map {
                _ =>
                  UnityMockEvent(
                    id = controls.eventIds.next,
                    startTime = controls.sessionStartTimeGenerator.incrementAndGet,
                    parameters = if (controls.eventParameters != null)
                      (1 to parameterCount).flatMap { _ => controls.eventParameters.next }.toMap
                    else Map.empty
                  )
              }.toArray,
              crashed = controls.crashes.next,
              duration = 10
            )
        }.toArray
      )
    usr
  }
}
