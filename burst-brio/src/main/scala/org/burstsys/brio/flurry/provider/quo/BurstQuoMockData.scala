/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.quo

import org.burstsys.brio.model.BrioMockDataModel
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.press.BrioPressSource

import java.util.concurrent.atomic.AtomicLong

/**
  * create mock data for quo schema. Note to support unit testing (and perhaps other users) this must be
  * deterministic - if you provide the same input parameters, the result will be identical
  *
  * @param userCount
  */
final case
class BurstQuoMockData(
                        userCount: Int = 10,
                        maxSessions: Int = 50,
                        maxEvents: Int = 50,
                        eventSet: Array[Long] = Array(11111, 22222, 33333, 44444, 55555),
                        parameterSet: Array[(String, String)] = Array(
                          "key1" -> "value1", "key2" -> "value2",
                          "key3" -> "value3", "key4" -> "value4",
                          "key5" -> "value5"
                        )
                      ) extends BrioMockDataModel {


  override
  val schema: BrioSchema = BrioSchema("quo")

  override
  def pressSource(root: BrioPressInstance): BrioPressSource = BurstQuoMockPressSource(root)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  val longValue = new AtomicLong

  private
  def newLongValue: Long = longValue.getAndIncrement

  private
  var genderValue = -1

  private
  def newGenderValue: Byte = {
    genderValue += 1
    if (genderValue > 1) {
      genderValue = -1
    }
    genderValue.toByte
  }

  private
  var flurryIdValue = new AtomicLong()

  private
  def newFlurryId: String = s"FlurryId${flurryIdValue.getAndIncrement()}"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  lazy val items: Seq[QuoMockUser] =
    for (i <- 0 until userCount) yield {
      QuoMockUser(
        flurryId = newFlurryId,
        project = QuoMockProject(
          projectId = newLongValue,
          installTime = newLongValue,
          lastUsedTime = newLongValue,
          retainedTime = newLongValue,
          cityId = newLongValue,
          stateId = newLongValue,
          countryId = newLongValue,
          regionId = newLongValue,
          localeId = newLongValue,
          languageId = newLongValue,
          birthDate = newLongValue,
          gender = newGenderValue
        ),
        sessions = generateSessions,
        segments = generateSegments,
        channels = generateChannels,
        personas = generatePersona,
        deviceModelId = newLongValue,
        deviceSubModelId = newLongValue,
        parameters = generateMap
      )
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Implementation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  def generateMap: Map[String, String] = {
    Map("k1" -> "v1")
  }

  private
  def generatePersona: Array[QuoMockPersona] = {
    Array(QuoMockPersona(personaId = newLongValue))
  }

  private
  def generateChannels: Array[QuoMockChannel] = {
    Array(
      QuoMockChannel(channelId = newLongValue, networkId = newLongValue, isQuality = false)
    )
  }

  private
  def generateSegments: Array[QuoMockSegment] = {
    Array(QuoMockSegment(segmentId = newLongValue))
  }

  private
  def generateSessions: Array[QuoMockSession] = {
    Array(
      QuoMockSession(
        sessionId = newLongValue,
        osVersion = newLongValue,
        startTime = newLongValue,
        totalErrors = newLongValue,
        timeZoneId = newLongValue,
        localeId = newLongValue,
        regionId = newLongValue,
        countryId = newLongValue,
        stateId = newLongValue,
        cityId = newLongValue,
        carrierId = newLongValue,
        agentVersionId = newLongValue,
        appVersionId = newLongValue,
        duration = newLongValue,
        totalEvents = newLongValue,
        events = generateEvents,
        providedOrigin = "",
        mappedOrigin = newLongValue,
        originSourceType = newLongValue,
        originMethodType = newLongValue,
        parameters = generateMap,
        birthDateReported = newLongValue,
        genderReported = newGenderValue
      )
    )
  }

  private
  def generateEvents: Array[QuoMockEvent] = {
    Array(
      QuoMockEvent(
        eventId = newLongValue,
        startTime = newLongValue,
        duration = newLongValue,
        order = newLongValue.toInt,
        eventType = newLongValue.toByte,
        parameters = generateMap
      )
    )
  }

}
