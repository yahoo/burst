/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity

import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.appVersionIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.applicationIdKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.channelIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.crashPercentKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.deviceModelIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventCountKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventDurationKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventIntervalKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventParametersCountKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventParametersPerEventKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.installDateIntervalKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.installDateStartKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.languageIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.localeCountryIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.mappedOriginsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.originMethodIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.originSourceIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.osVersionIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.providedOriginsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionCountKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionDurationKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionIntervalKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionParameterCountKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionParametersPerSessionKey
import org.burstsys.brio.flurry.provider.unity.press.SyntheticAppVersionData
import org.burstsys.brio.flurry.provider.unity.press.SyntheticApplicationData
import org.burstsys.brio.flurry.provider.unity.press.SyntheticApplicationUseData
import org.burstsys.brio.flurry.provider.unity.press.SyntheticChannelData
import org.burstsys.brio.flurry.provider.unity.press.SyntheticEventData
import org.burstsys.brio.flurry.provider.unity.press.SyntheticSessionData
import org.burstsys.brio.flurry.provider.unity.press.SyntheticUserData
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.press.BrioPressSource
import org.burstsys.brio.provider.BrioSyntheticDataProvider
import org.burstsys.brio.provider.BrioSyntheticDataProvider.SyntheticRepeatedValue
import org.burstsys.vitals.properties._

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.temporal.ChronoField
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.duration.DurationInt

object BurstUnitySyntheticDataProvider {
  /** `Long` - The application id */
  val applicationIdKey = "synthetic.unity.applicationId"
  /** `String` - The ISO 8601 string for the first date of app installs  */
  val installDateStartKey = "synthetic.unity.installDateStart"
  /** `Duration` - The interval between installs (across users) */
  val installDateIntervalKey = "synthetic.unity.installDateInterval"
  /** `Array[Long]` - The device model ids */
  val deviceModelIdsKey = "synthetic.unity.deviceModelIds"
  /** `Array[Long]` - The app version ids */
  val appVersionIdsKey = "synthetic.unity.appVersionIds"
  /** `Array[Long]` - The os version ids */
  val osVersionIdsKey = "synthetic.unity.osVersionIds"
  /** `Array[Long]` - The country ids */
  val localeCountryIdsKey = "synthetic.unity.localeCountryIds"
  /** `Array[Long]` - The language ids */
  val languageIdsKey = "synthetic.unity.languageIds"
  /** `Array[Long]` - The channel ids */
  val channelIdsKey = "synthetic.unity.channelIds"
  /** `Array[Long]` - The interest ids */
  val interestsKey = "synthetic.unity.interests"
  /** `Int` - The number of sessions each user has */
  val sessionCountKey = "synthetic.unity.sessionCount"
  /** `Duration` - The interval between sessions */
  val sessionIntervalKey = "synthetic.unity.sessionInterval"
  /** `Duration` - The length of a session */
  val sessionDurationKey = "synthetic.unity.sessionDuration"
  /** `Array[String]` - The origin names */
  val providedOriginsKey = "synthetic.unity.providedOrigins"
  /** `Array[Long]` - The origin ids */
  val mappedOriginsKey = "synthetic.unity.mappedOrigins"
  /** `Array[Long]` - The origin source ids */
  val originSourceIdsKey = "synthetic.unity.originSourceIds"
  /** `Array[Long]` - The origin method ids */
  val originMethodIdsKey = "synthetic.unity.originMethodIds"
  /** `Int` - The percentage of sessions that should be marked as crashed [0, 100] */
  val crashPercentKey = "synthetic.unity.crashPercent"
  /** `Int` - The number of parameters in the session */
  val sessionParameterCountKey = "synthetic.unity.sessionParameterCount"
  /** `Array[Int]` - The number of parameters to for each event */
  val sessionParametersPerSessionKey = "synthetic.unity.sessionParametersPerSession"
  /** `Int` - The number of events in the session */
  val eventCountKey = "synthetic.unity.eventCount"
  /** `Array[Long]` - The event ids */
  val eventIdsKey = "synthetic.unity.eventIds"
  /** `Duration` - The interval between events in a session */
  val eventIntervalKey = "synthetic.unity.eventInterval"
  /** `Duration` - The duration of events in the session */
  val eventDurationKey = "synthetic.unity.eventDuration"
  /** `Int` - The number of event parameters that exist */
  val eventParametersCountKey = "synthetic.unity.eventParametersCount"
  /** `Array[Int]` - The number of parameters to for each event */
  val eventParametersPerEventKey = "synthetic.unity.eventParametersPerEvent"
}

/**
 * A simple synthetic data model for unity schema
 */
case class BurstUnitySyntheticDataProvider() extends BrioSyntheticDataProvider {

  override def data(itemCount: Int, properties: VitalsPropertyMap): Iterator[BrioPressInstance] = {
    val extended: VitalsRichExtendedPropertyMap = properties.extend
    val applicationId = extended.getValueOrDefault(applicationIdKey, 12345L)
    val installDateStart = Instant.from(ISO_DATE_TIME.parse(extended.getValueOrDefault(installDateStartKey, "2022-01-01T08:45:00Z"))).toEpochMilli
    val installDateInterval = extended.getValueOrDefault(installDateIntervalKey, 1.day).toMillis
    val deviceModelIds = propertyAsRepeatedValue(extended, deviceModelIdsKey, Array(555666L, 666777L, 888999L))
    val appVersionIds = propertyAsRepeatedValue(extended, appVersionIdsKey, Array(101010101L, 12121212L, 13131313L, 1414141414L, 1515151515L, 1616161616L))
    val osVersionIds = propertyAsRepeatedValue(extended, osVersionIdsKey, Array(232323L, 454545L, 676767L, 898989L))
    val localeCountryIds = propertyAsRepeatedValue(extended, localeCountryIdsKey, Array(10000L, 20000L, 30000L, 40000L, 50000L, 60000L))
    val languageIds = propertyAsRepeatedValue(extended, languageIdsKey, Array(111222L, 333444L, 555666L, 777888L, 888999L))
    val channelIds = propertyAsRepeatedValue(extended, channelIdsKey, Array(22L, 33L, 44L))
    val interests = SyntheticRepeatedValue((1L to 10L).map(i => Array(i to 10L: _*)): _*)

    val sessionCount: Int = extended.getValueOrDefault(sessionCountKey, 10)
    val sessionInterval = extended.getValueOrDefault(sessionIntervalKey, 1.day).toMillis
    val sessionDuration = extended.getValueOrDefault(sessionDurationKey, 10.minutes).toMillis
    val providedOrigins = propertyAsRepeatedValue(extended, providedOriginsKey, Array("origin1", "origin2", "origin3", "origin4", "origin5"))
    val mappedOrigins = propertyAsRepeatedValue(extended, mappedOriginsKey, Array(9876L, 54321L, 54329L))
    val originSourceIds = propertyAsRepeatedValue(extended, originSourceIdsKey, Array(987L, 986L, 985L, 984L, 983L))
    val originMethodIds = propertyAsRepeatedValue(extended, originMethodIdsKey, Array(12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L))
    val crashPercent: Int = extended.getValueOrDefault(crashPercentKey, 10)
    val didCrash = SyntheticRepeatedValue((1 to (100 - crashPercent)).map(_ => false) ++ (1 to crashPercent).map(_ => true): _*)
    val sessionParameterCount: Int = extended.getValueOrDefault(sessionParameterCountKey, 7)
    val sessionParameters = SyntheticRepeatedValue((1 to sessionParameterCount).map(i => Map(s"SK$i" -> s"SV$i")): _*)
    val sessionParametersPerSession = propertyAsRepeatedValue(extended, sessionParametersPerSessionKey, Array(1 to sessionParameterCount: _*))

    val eventCount: Int = extended.getValueOrDefault(eventCountKey, 10)
    val eventIds = propertyAsRepeatedValue(extended, eventIdsKey, Array(1L to 11L: _*))
    val eventInterval = extended.getValueOrDefault(eventIntervalKey, 3.minutes).toMillis
    val eventDuration = extended.getValueOrDefault(eventDurationKey, 1.minute).toMillis
    val eventParametersCount: Int = extended.getValueOrDefault(eventParametersCountKey, 5)
    val eventParameters = SyntheticRepeatedValue((1 to eventParametersCount).map(i => Map(s"EK$i" -> s"EV$i")): _*)
    val eventParametersPerEvent = propertyAsRepeatedValue(extended, eventParametersPerEventKey, Array(1 to eventParametersCount: _*))

    val unspecified = -1

    LazyList.from(1).take(itemCount)
      .map(userIdx => {
        val installTime: Long = installDateStart + userIdx * installDateInterval
        val appVersion = SyntheticAppVersionData(appVersionIds.value(userIdx))
        val sessionIds = new AtomicLong()

        SyntheticUserData(
          id = s"User#$userIdx",
          deviceModelId = deviceModelIds.value(userIdx),
          deviceSubmodelId = Option.empty,
          deviceFormat = unspecified.toByte,
          estimatedAgeBucket = Option.empty,
          estimatedGender = unspecified.toByte,
          application = SyntheticApplicationData(
            id = applicationId,
            firstUse = SyntheticApplicationUseData(
              appVersion, Option(installTime), Option(osVersionIds.value(userIdx)),
              timeZone = null,
              localeCountryId = Option(localeCountryIds.value(userIdx)),
              languageId = Option(languageIds.value(userIdx)),
            ),
            lastUse = SyntheticApplicationUseData(
              appVersion, Option(installTime + TimeUnit.DAYS.toMillis(30)), Option(osVersionIds.value(userIdx)),
              timeZone = null,
              localeCountryId = Option(localeCountryIds.value(userIdx)),
              languageId = Option(languageIds.value(userIdx)),
            ),
            mostUse = SyntheticApplicationUseData(
              appVersion,
              osVersionId = Option(osVersionIds.value(userIdx)),
              timeZone = null,
              localeCountryId = Option(localeCountryIds.value(userIdx)),
              languageId = Option(languageIds.value(userIdx)),
            ),
            channels = Array(SyntheticChannelData(
              sourceId = unspecified,
              campaignId = unspecified,
              channelId = Option(channelIds.value(userIdx)),
              parameters = Map.empty)).iterator,
            parameters = Map.empty
          ),
          sessions = LazyList.from(1).take(sessionCount).map(sessionIdx =>
            SyntheticSessionData(
              id = sessionIds.incrementAndGet(),
              appVersion = SyntheticAppVersionData(appVersionIds.next),
              events = LazyList.from(1).take(eventCount).map(eventIdx =>
                SyntheticEventData(
                  id = eventIds.value(eventIdx),
                  eventType = unspecified.toByte,
                  startTime = installTime + sessionIdx * sessionInterval + eventIdx * eventInterval,
                  duration = eventDuration,
                  standardEventId = unspecified,
                  parameters = (1 to eventParametersPerEvent.value((eventIdx))).flatMap(eventParameters.value).toMap
                )).iterator,
              variants = Array.empty.iterator,
              sessionType = unspecified.toByte,
              applicationUserId = null,
              pushTokenStatus = unspecified.toByte,
              limitAdTracking = false,
              osVersionId = osVersionIds.value(sessionIdx),
              startTime = installTime + sessionIdx * sessionInterval,
              timeZone = null,
              cityId = unspecified,
              geoAreaId = unspecified,
              countryId = unspecified,
              regionId = unspecified,
              localeId = unspecified,
              carrierId = unspecified,
              agentVersionId = unspecified,
              duration = sessionDuration,
              providedOrigin = providedOrigins.value(sessionIdx),
              mappedOriginId = mappedOrigins.value(sessionIdx),
              originSourceTypeId = originSourceIds.value(sessionIdx),
              originMethodTypeId = originMethodIds.value(sessionIdx),
              reportedBirthDate = Option.empty,
              reportedAgeBucket = Option.empty,
              reportedGender = unspecified.byteValue,
              reportingDelay = unspecified,
              crashed = didCrash.next,
              parameters = (1 to sessionParametersPerSession.value(sessionIdx)).flatMap(sessionParameters.value).toMap
            )).iterator,
          traits = Array.empty.iterator,
          parameters = Map.empty,
          interests = interests.value(userIdx)
        )
      }).iterator
  }

  override def datasetName: String = "simple-unity"

  override def schemaName: String = "unity"

  override def pressSource(root: BrioPressInstance): BrioPressSource = UnityPressSource(root)

}
