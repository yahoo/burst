/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity

import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider._
import org.burstsys.brio.flurry.provider.unity.press._
import org.burstsys.brio.press.{BrioPressInstance, BrioPressSource}
import org.burstsys.brio.provider.BrioSyntheticDataProvider
import org.burstsys.brio.provider.SyntheticDataProvider.SyntheticRepeatedValue
import org.burstsys.vitals.properties._

import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import scala.annotation.unused
import scala.concurrent.duration.{Duration, DurationInt}

object BurstUnitySyntheticDataProvider {
  /** `string` - The prefix for the user id */
  val userIdPrefixKey = "synthetic.unity.userId.prefix"

  /** Default prefix for user id */
  val userIdPrefixDefault = "User#"

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
  // val originMethodIdsDefault: Array[Long] = Array(12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L)

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
@unused  // found by reflection
case class BurstUnitySyntheticDataProvider() extends BrioSyntheticDataProvider {

  class Parameters(properties: VitalsPropertyMap) {
    val extended: VitalsRichExtendedPropertyMap = properties.extend
    val userIdPrefix: VitalsPropertyKey = extended.getValueOrDefault(userIdPrefixKey, userIdPrefixDefault)
    val applicationId: Long = extended.getValueOrDefault(applicationIdKey, applicationIdDefault)
    val installDateStart: Long = Instant.from(ISO_DATE_TIME.parse(extended.getValueOrDefault(installDateStartKey, installDateStartDefault))).toEpochMilli
    val installDateInterval: Long = extended.getValueOrDefault(installDateIntervalKey, installDateIntervalDefault).toMillis
    val deviceModelIds: SyntheticRepeatedValue[Long] = propertyAsRepeatedValue(extended, deviceModelIdsKey, deviceModelIdsDefault)
    val appVersionIds: SyntheticRepeatedValue[Long] = propertyAsRepeatedValue(extended, appVersionIdsKey, appVersionIdsDefault)
    val osVersionIds: SyntheticRepeatedValue[Long] = propertyAsRepeatedValue(extended, osVersionIdsKey, osVersionIdsDefault)
    val localeCountryIds: SyntheticRepeatedValue[Long] = propertyAsRepeatedValue(extended, localeCountryIdsKey, localeCountryIdsDefault)
    val languageIds: SyntheticRepeatedValue[Long] = propertyAsRepeatedValue(extended, languageIdsKey, languageIdsDefault)
    val channelIds: SyntheticRepeatedValue[Long] = propertyAsRepeatedValue(extended, channelIdsKey, channelIdsDefault)
    val interests: SyntheticRepeatedValue[Array[Long]] = SyntheticRepeatedValue((1L to 10L).map(i => Array(i to 10L: _*)): _*)

    val sessionCount: Int = extended.getValueOrDefault(sessionCountKey, sessionCountDefault)
    val sessionInterval: Long = extended.getValueOrDefault(sessionIntervalKey, sessionIntervalDefault).toMillis
    val sessionDuration: Long = extended.getValueOrDefault(sessionDurationKey, sessionDurationDefault).toMillis
    val providedOrigins: SyntheticRepeatedValue[VitalsPropertyKey] = propertyAsRepeatedValue(extended, providedOriginsKey, providedOriginsDefault)
    val mappedOrigins: SyntheticRepeatedValue[Long] = propertyAsRepeatedValue(extended, mappedOriginsKey, mappedOriginsDefault)
    val originSourceIds: SyntheticRepeatedValue[Long] = propertyAsRepeatedValue(extended, originSourceIdsKey, originSourceIdsDefault)
    val originMethodIds: SyntheticRepeatedValue[Long] = propertyAsRepeatedValue(extended, originMethodIdsKey, Array(12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L))
    val crashPercent: Int = extended.getValueOrDefault(crashPercentKey, crashPercentDefault)
    val didCrash: SyntheticRepeatedValue[Boolean] = SyntheticRepeatedValue((1 to (100 - crashPercent)).map(_ => false) ++ (1 to crashPercent).map(_ => true): _*)
    val sessionParameterCount: Int = extended.getValueOrDefault(sessionParameterCountKey, sessionParameterCountDefault)
    val sessionParameters: SyntheticRepeatedValue[Map[String, String]] = SyntheticRepeatedValue((1 to sessionParameterCount).map(i => Map(s"SK$i" -> s"SV$i")): _*)
    val sessionParametersPerSession: SyntheticRepeatedValue[Int] = propertyAsRepeatedValue(extended, sessionParametersPerSessionKey, sessionParametersPerSessionDefault)

    val eventCount: Int = extended.getValueOrDefault(eventCountKey, eventCountDefault)
    val eventIds: SyntheticRepeatedValue[Long] = propertyAsRepeatedValue(extended, eventIdsKey, eventIdsDefault)
    val eventInterval: Long = extended.getValueOrDefault(eventIntervalKey, eventIntervalDefault).toMillis
    val eventDuration: Long = extended.getValueOrDefault(eventDurationKey, eventDurationDefault).toMillis
    val eventParametersCount: Int = extended.getValueOrDefault(eventParametersCountKey, eventParametersCountDefault)
    val eventParameters: SyntheticRepeatedValue[Map[String, String]] = SyntheticRepeatedValue((1 to eventParametersCount).map(i => Map(s"EK$i" -> s"EV$i")): _*)
    val eventParametersPerEvent: SyntheticRepeatedValue[Int] = propertyAsRepeatedValue(extended, eventParametersPerEventKey, eventParametersPerEventDefault)

    val unspecified: Int = -1
  }

  override def data(itemCount: Int, properties: VitalsPropertyMap): Iterator[BrioPressInstance] = {
    val params = new Parameters(properties)

    LazyList.from(0).take(itemCount)
      .map(userIdx => makeItem(userIdx, params)).iterator
  }

  def makeItem(userIdx: Int, params: Parameters): SyntheticUserData = {
    val installTime: Long = params.installDateStart + userIdx * params.installDateInterval
    val appVersion = SyntheticAppVersionData(params.appVersionIds.value(userIdx))
    val sessionIds = new AtomicLong()
    val idVal = s"${params.userIdPrefix}${userIdx + 1}"
    if (log.isTraceEnabled)
      log trace s"id=$idVal"

    SyntheticUserData(
      id = idVal,
      deviceModelId = params.deviceModelIds.value(userIdx),
      deviceSubmodelId = Option.empty,
      deviceFormat = params.unspecified.toByte,
      estimatedAgeBucket = Option.empty,
      estimatedGender = params.unspecified.toByte,
      application = SyntheticApplicationData(
        id = params.applicationId,
        firstUse = SyntheticApplicationUseData(
          appVersion, Option(installTime), Option(params.osVersionIds.value(userIdx)),
          timeZone = null,
          localeCountryId = Option(params.localeCountryIds.value(userIdx)),
          languageId = Option(params.languageIds.value(userIdx)),
        ),
        lastUse = SyntheticApplicationUseData(
          appVersion, Option(installTime + TimeUnit.DAYS.toMillis(30)), Option(params.osVersionIds.value(userIdx)),
          timeZone = null,
          localeCountryId = Option(params.localeCountryIds.value(userIdx)),
          languageId = Option(params.languageIds.value(userIdx)),
        ),
        mostUse = SyntheticApplicationUseData(
          appVersion,
          osVersionId = Option(params.osVersionIds.value(userIdx)),
          timeZone = null,
          localeCountryId = Option(params.localeCountryIds.value(userIdx)),
          languageId = Option(params.languageIds.value(userIdx)),
        ),
        channels = Array(SyntheticChannelData(
          sourceId = params.unspecified,
          campaignId = params.unspecified,
          channelId = Option(params.channelIds.value(userIdx)),
          parameters = Map.empty)).iterator,
        parameters = Map.empty
      ),
      sessions = LazyList.from(0).take(params.sessionCount).map(sessionIdx =>
        SyntheticSessionData(
          id = sessionIds.incrementAndGet(),
          appVersion = SyntheticAppVersionData(params.appVersionIds.next),
          events = LazyList.from(0).take(params.eventCount).map(eventIdx =>
            SyntheticEventData(
              id = params.eventIds.value(eventIdx),
              eventType = params.unspecified.toByte,
              startTime = installTime + sessionIdx * params.sessionInterval + eventIdx * params.eventInterval,
              duration = params.eventDuration,
              standardEventId = params.unspecified,
              parameters = (0 to params.eventParametersPerEvent.value(eventIdx)).flatMap(params.eventParameters.value).toMap
            )).iterator,
          variants = Array.empty.iterator,
          sessionType = params.unspecified.toByte,
          applicationUserId = null,
          pushTokenStatus = params.unspecified.toByte,
          limitAdTracking = false,
          osVersionId = params.osVersionIds.value(sessionIdx),
          startTime = installTime + sessionIdx * params.sessionInterval,
          timeZone = null,
          cityId = params.unspecified,
          geoAreaId = params.unspecified,
          countryId = params.unspecified,
          regionId = params.unspecified,
          localeId = params.unspecified,
          carrierId = params.unspecified,
          agentVersionId = params.unspecified,
          duration = params.sessionDuration,
          providedOrigin = params.providedOrigins.value(sessionIdx),
          mappedOriginId = params.mappedOrigins.value(sessionIdx),
          originSourceTypeId = params.originSourceIds.value(sessionIdx),
          originMethodTypeId = params.originMethodIds.value(sessionIdx),
          reportedBirthDate = Option.empty,
          reportedAgeBucket = Option.empty,
          reportedGender = params.unspecified.byteValue,
          reportingDelay = params.unspecified,
          crashed = params.didCrash.next,
          parameters = (1 to params.sessionParametersPerSession.value(sessionIdx)).flatMap(params.sessionParameters.value).toMap
        )).iterator,
      traits = Array.empty.iterator,
      parameters = Map.empty,
      interests = params.interests.value(userIdx)
    )
  }

  override def datasetName: String = "simple-unity"

  override def schemaName: String = "unity"

  override def pressSource(root: BrioPressInstance): BrioPressSource = UnityPressSource(root)

}
