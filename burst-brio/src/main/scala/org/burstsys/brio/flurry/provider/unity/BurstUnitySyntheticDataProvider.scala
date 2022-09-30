/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity

import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.appVersionIdsDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.appVersionIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.applicationIdDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.applicationIdKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.channelIdsDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.channelIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.crashPercentDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.crashPercentKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.deviceModelIdsDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.deviceModelIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventCountDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventCountKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventDurationDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventDurationKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventIdsDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventIntervalDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventIntervalKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventParametersCountDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventParametersCountKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventParametersPerEventDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.eventParametersPerEventKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.installDateIntervalDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.installDateIntervalKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.installDateStartDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.installDateStartKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.languageIdsDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.languageIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.localeCountryIdsDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.localeCountryIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.mappedOriginsDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.mappedOriginsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.originMethodIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.originSourceIdsDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.originSourceIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.osVersionIdsDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.osVersionIdsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.providedOriginsDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.providedOriginsKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionCountDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionCountKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionDurationDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionDurationKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionIntervalDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionIntervalKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionParameterCountDefault
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionParameterCountKey
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider.sessionParametersPerSessionDefault
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
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt

object BurstUnitySyntheticDataProvider {
  /** `Long` - The application id */
  val applicationIdKey = "synthetic.unity.applicationId"

  /** Default value for `applicationIdKey`: 12345L */
  val applicationIdDefault: Long = 12345L

  /** `String` - The ISO 8601 string for the first date of app installs */
  val installDateStartKey = "synthetic.unity.installDateStart"

  /** Default value for `installDateStartKey`: 2022-01-01T08:45:00Z */
  val installDateStartDefault: String = "2022-01-01T08:45:00Z"

  /** `Duration` - The interval between installs (across users) */
  val installDateIntervalKey = "synthetic.unity.installDateInterval"

  /** Default value for `installDateIntervalKey`: 1 day */
  val installDateIntervalDefault: Duration = 1.day

  /** `Array[Long]` - The device model ids */
  val deviceModelIdsKey = "synthetic.unity.deviceModelIds"

  /** Default value for `deviceModelIdsKey`: [555666L, 666777L, 888999L] */
  val deviceModelIdsDefault: Array[Long] = Array(555666L, 666777L, 888999L)

  /** `Array[Long]` - The app version ids */
  val appVersionIdsKey = "synthetic.unity.appVersionIds"

  /** Default value for `appVersionIdsKey`: [101010101L, 12121212L, 13131313L, 1414141414L, 1515151515L, 1616161616L] */
  val appVersionIdsDefault: Array[Long] = Array(101010101L, 12121212L, 13131313L, 1414141414L, 1515151515L, 1616161616L)

  /** `Array[Long]` - The os version ids */
  val osVersionIdsKey = "synthetic.unity.osVersionIds"

  /** Default value for `osVersionIdsKey`: [232323L, 454545L, 676767L, 898989L] */
  val osVersionIdsDefault: Array[Long] = Array(232323L, 454545L, 676767L, 898989L)

  /** `Array[Long]` - The country ids */
  val localeCountryIdsKey = "synthetic.unity.localeCountryIds"

  /** Default value for `localeCountryIdsKey`: [10000L, 20000L, 30000L, 40000L, 50000L, 60000L] */
  val localeCountryIdsDefault: Array[Long] = Array(10000L, 20000L, 30000L, 40000L, 50000L, 60000L)

  /** `Array[Long]` - The language ids */
  val languageIdsKey = "synthetic.unity.languageIds"

  /** Default value for `languageIdsKey`: [111222L, 333444L, 555666L, 777888L, 888999L] */
  val languageIdsDefault: Array[Long] = Array(111222L, 333444L, 555666L, 777888L, 888999L)

  /** `Array[Long]` - The channel ids */
  val channelIdsKey = "synthetic.unity.channelIds"

  /** Default value for `channelIdsKey` */
  val channelIdsDefault: Array[Long] = Array(22L, 33L, 44L)

  /** `Int` - The number of sessions each user has */
  val sessionCountKey = "synthetic.unity.sessionCount"

  /** Default value for `sessionCountKey`: 10 */
  val sessionCountDefault: Int = 10

  /** `Duration` - The interval between sessions */
  val sessionIntervalKey = "synthetic.unity.sessionInterval"

  /** Default value for `sessionIntervalKey`: 1 day */
  val sessionIntervalDefault: Duration = 1.day

  /** `Duration` - The length of a session */
  val sessionDurationKey = "synthetic.unity.sessionDuration"

  /** Default value for `sessionDurationKey`: 10 minutes */
  val sessionDurationDefault: Duration = 10.minutes

  /** `Array[String]` - The origin names */
  val providedOriginsKey = "synthetic.unity.providedOrigins"

  /** Default value for `providedOriginsKey`: ["origin1", "origin2", "origin3", "origin4", "origin5"] */
  val providedOriginsDefault: Array[String] = Array("origin1", "origin2", "origin3", "origin4", "origin5")

  /** `Array[Long]` - The origin ids */
  val mappedOriginsKey = "synthetic.unity.mappedOrigins"

  /** Default value for `mappedOriginsKey`: [9876L, 54321L, 54329L] */
  val mappedOriginsDefault: Array[Long] = Array(9876L, 54321L, 54329L)

  /** `Array[Long]` - The origin source ids */
  val originSourceIdsKey = "synthetic.unity.originSourceIds"

  /** Default value for `originSourceIdsKey`: [987L, 986L, 985L, 984L, 983L] */
  val originSourceIdsDefault: Array[Long] = Array(987L, 986L, 985L, 984L, 983L)

  /** `Array[Long]` - The origin method ids */
  val originMethodIdsKey = "synthetic.unity.originMethodIds"

  /** Default value for `originMethodIdsKey`: [12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L] */
  val originMethodIdsDefault: Array[Long] = Array(12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L)

  /** `Int` - The percentage of sessions that should be marked as crashed [0, 100] */
  val crashPercentKey = "synthetic.unity.crashPercent"

  /** Default value for `crashPercentKey`: 10 */
  val crashPercentDefault: Int = 10

  /** `Int` - The number of parameters in the session */
  val sessionParameterCountKey = "synthetic.unity.sessionParameterCount"

  /** Default value for `sessionParameterCountKey` */
  val sessionParameterCountDefault: Int =  7

  /** `Array[Int]` - The number of parameters to for each event */
  val sessionParametersPerSessionKey = "synthetic.unity.sessionParametersPerSession"

  /** Default value for `sessionParametersPerSessionKey`: [1L, 2L, 3L, 4L, 5L, 6L, 7L] */
  val sessionParametersPerSessionDefault: Array[Int] = Array(1 to sessionParameterCountDefault: _*)

  /** `Int` - The number of events in the session */
  val eventCountKey = "synthetic.unity.eventCount"

  /** Default value for `eventCountKey`: 10 */
  val eventCountDefault: Int = 10

  /** `Array[Long]` - The event ids */
  val eventIdsKey = "synthetic.unity.eventIds"

  /** Default value for `eventIdsKey`: [1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L] */
  val eventIdsDefault: Array[Long] = Array(1L to eventCountDefault: _*)

  /** `Duration` - The interval between events in a session */
  val eventIntervalKey = "synthetic.unity.eventInterval"

  /** Default value for `eventIntervalKey`: 3 minutes */
  val eventIntervalDefault: Duration = 3.minutes

  /** `Duration` - The duration of events in the session */
  val eventDurationKey = "synthetic.unity.eventDuration"

  /** Default value for `eventDurationKey`: 1 minute */
  val eventDurationDefault: Duration = 1.minute

  /** `Int` - The number of event parameters that exist */
  val eventParametersCountKey = "synthetic.unity.eventParametersCount"

  /** Default value for `eventParametersCountKey`: 5 */
  val eventParametersCountDefault: Int = 5

  /** `Array[Int]` - The number of parameters to for each event */
  val eventParametersPerEventKey = "synthetic.unity.eventParametersPerEvent"

  /** Default value for `eventParametersPerEventKey`: [0, 1, 2, 3, 4, 5] */
  val eventParametersPerEventDefault: Array[Int] = Array(0 to eventParametersCountDefault: _*)

}

/**
 * A simple synthetic data model for unity schema
 */
case class BurstUnitySyntheticDataProvider() extends BrioSyntheticDataProvider {

  override def data(itemCount: Int, properties: VitalsPropertyMap): Iterator[BrioPressInstance] = {
    val extended: VitalsRichExtendedPropertyMap = properties.extend
    val applicationId = extended.getValueOrDefault(applicationIdKey, applicationIdDefault)
    val installDateStart = Instant.from(ISO_DATE_TIME.parse(extended.getValueOrDefault(installDateStartKey, installDateStartDefault))).toEpochMilli
    val installDateInterval = extended.getValueOrDefault(installDateIntervalKey, installDateIntervalDefault).toMillis
    val deviceModelIds = propertyAsRepeatedValue(extended, deviceModelIdsKey, deviceModelIdsDefault)
    val appVersionIds = propertyAsRepeatedValue(extended, appVersionIdsKey, appVersionIdsDefault)
    val osVersionIds = propertyAsRepeatedValue(extended, osVersionIdsKey, osVersionIdsDefault)
    val localeCountryIds = propertyAsRepeatedValue(extended, localeCountryIdsKey, localeCountryIdsDefault)
    val languageIds = propertyAsRepeatedValue(extended, languageIdsKey, languageIdsDefault)
    val channelIds = propertyAsRepeatedValue(extended, channelIdsKey, channelIdsDefault)
    val interests = SyntheticRepeatedValue((1L to 10L).map(i => Array(i to 10L: _*)): _*)

    val sessionCount: Int = extended.getValueOrDefault(sessionCountKey, sessionCountDefault)
    val sessionInterval = extended.getValueOrDefault(sessionIntervalKey, sessionIntervalDefault).toMillis
    val sessionDuration = extended.getValueOrDefault(sessionDurationKey, sessionDurationDefault).toMillis
    val providedOrigins = propertyAsRepeatedValue(extended, providedOriginsKey, providedOriginsDefault)
    val mappedOrigins = propertyAsRepeatedValue(extended, mappedOriginsKey, mappedOriginsDefault)
    val originSourceIds = propertyAsRepeatedValue(extended, originSourceIdsKey, originSourceIdsDefault)
    val originMethodIds = propertyAsRepeatedValue(extended, originMethodIdsKey, Array(12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L))
    val crashPercent: Int = extended.getValueOrDefault(crashPercentKey, crashPercentDefault)
    val didCrash = SyntheticRepeatedValue((1 to (100 - crashPercent)).map(_ => false) ++ (1 to crashPercent).map(_ => true): _*)
    val sessionParameterCount: Int = extended.getValueOrDefault(sessionParameterCountKey, sessionParameterCountDefault)
    val sessionParameters = SyntheticRepeatedValue((1 to sessionParameterCount).map(i => Map(s"SK$i" -> s"SV$i")): _*)
    val sessionParametersPerSession = propertyAsRepeatedValue(extended, sessionParametersPerSessionKey, sessionParametersPerSessionDefault)

    val eventCount: Int = extended.getValueOrDefault(eventCountKey, eventCountDefault)
    val eventIds = propertyAsRepeatedValue(extended, eventIdsKey, eventIdsDefault)
    val eventInterval = extended.getValueOrDefault(eventIntervalKey, eventIntervalDefault).toMillis
    val eventDuration = extended.getValueOrDefault(eventDurationKey, eventDurationDefault).toMillis
    val eventParametersCount: Int = extended.getValueOrDefault(eventParametersCountKey, eventParametersCountDefault)
    val eventParameters = SyntheticRepeatedValue((1 to eventParametersCount).map(i => Map(s"EK$i" -> s"EV$i")): _*)
    val eventParametersPerEvent = propertyAsRepeatedValue(extended, eventParametersPerEventKey, eventParametersPerEventDefault)

    val unspecified = -1

    LazyList.from(0).take(itemCount)
      .map(userIdx => {
        val installTime: Long = installDateStart + userIdx * installDateInterval
        val appVersion = SyntheticAppVersionData(appVersionIds.value(userIdx))
        val sessionIds = new AtomicLong()

        SyntheticUserData(
          id = s"User#${userIdx + 1}",
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
          sessions = LazyList.from(0).take(sessionCount).map(sessionIdx =>
            SyntheticSessionData(
              id = sessionIds.incrementAndGet(),
              appVersion = SyntheticAppVersionData(appVersionIds.next),
              events = LazyList.from(0).take(eventCount).map(eventIdx =>
                SyntheticEventData(
                  id = eventIds.value(eventIdx),
                  eventType = unspecified.toByte,
                  startTime = installTime + sessionIdx * sessionInterval + eventIdx * eventInterval,
                  duration = eventDuration,
                  standardEventId = unspecified,
                  parameters = (0 to eventParametersPerEvent.value(eventIdx)).flatMap(eventParameters.value).toMap
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
