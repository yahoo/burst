/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider

import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.provider.BrioSchemaProvider
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.vitals.logging._

package object unity extends VitalsLogger {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Mock Data
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @return
   */
  def mockBlobs: Array[TeslaMutableBuffer] = BurstUnityMockData().pressToBuffers

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // SCHEMA
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * provider class for the brio 'unity' schema
   */
  final case class UnitySchemaProvider() extends BrioSchemaProvider[BurstUnityMockPressSource] {

    val names: Array[String] = Array("org.burstsys.schema.unity", "unity", "Unity")

    val schemaResourcePath: String = "org/burstsys/brio/flurry/schema/unity"

    val presserClass: Class[BurstUnityMockPressSource] = classOf[BurstUnityMockPressSource]

  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // MOCK DATA MODEL
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  sealed trait UnityMockInstance extends BrioPressInstance {
    final override val schemaVersion: BrioVersionKey = 1
  }

  case
  class UnityMockUser(
                       id: String = "SomeUserId",
                       application: UnityMockApplication = null,
                       parameters: Map[String, String] = Map.empty,
                       deviceModelId: Long = -1,
                       deviceSubmodelId: Long = -1,
                       deviceFormat: Short = -1,
                       estimatedAgeBucket: Byte = -1,
                       estimatedGender: Byte = -1,
                       interests: Array[Long] = Array.empty,
                       traits: Array[UnityMockTrait] = Array.empty,
                       sessions: Array[UnityMockSession] = Array.empty
                     ) extends UnityMockInstance

  final case
  class UnityMockApplication(
                              id: Long = -1,
                              firstUse: UnityMockUse = null,
                              lastUse: UnityMockUse = null,
                              mostUse: UnityMockUse = null,
                              parameters: Map[String, String] = Map.empty,
                              channels: Array[UnityMockChannel] = Array.empty
                            ) extends UnityMockInstance

  final case
  class UnityMockUse(
                      sessionTime: Long = -1,
                      appVersion: UnityMockAppVersion = null,
                      osVersionId: Long = -1,
                      agentVersionId: Long = -1,
                      pushTokenStatus: Byte = -1,
                      timeZone: String = null,
                      timeZoneOffsetSecs: Int = -1,
                      limitAdTracking: Boolean = false,
                      cityId: Long = -1,
                      geoAreaId: Long = -1,
                      countryId: Long = -1,
                      regionId: Long = -1,
                      localeId: Long = -1,
                      localeCountryId: Long = -1,
                      languageId: Long = -1,
                      reportedBirthDate: Long = -1,
                      reportedAgeBucket: Byte = -1,
                      reportedGender: Byte = -1,
                      crashTime: Long = 0
                    ) extends UnityMockInstance

  final case
  class UnityMockSession(
                          id: Long = -1,
                          sessionType: Byte = -1,
                          applicationUserId: String = "",
                          pushTokenStatus: Byte = -1,
                          limitAdTracking: Boolean = false,
                          osVersionId: Long = -1,
                          startTime: Long = -1,
                          timeZone: String = "",
                          cityId: Long = -1,
                          geoAreaId: Long = -1,
                          countryId: Long = -1,
                          regionId: Long = -1,
                          localeId: Long = -1,
                          carrierId: Long = -1,
                          agentVersionId: Long = -1,
                          appVersion: UnityMockAppVersion = null,
                          duration: Long = -1,
                          providedOrigin: String = "",
                          mappedOriginId: Long = -1,
                          originSourceTypeId: Long = -1,
                          originMethodTypeId: Long = -1,
                          reportedBirthDate: Long = -1,
                          reportedAgeBucket: Byte = -1,
                          reportedGender: Byte = -1,
                          reportingDelay: Int = -1,
                          crashed: Boolean = false,
                          parameters: Map[String, String] = Map.empty,
                          variants: Array[UnityMockVariant] = Array.empty,
                          events: Array[UnityMockEvent] = Array.empty
                        ) extends UnityMockInstance

  final case
  class UnityMockEvent(id: Long = -1L,
                       eventType: Byte = -1,
                       startTime: Long = -1,
                       duration: Long = -1,
                       standardEventId: Long = -1,
                       parameters: Map[String, String] = Map.empty
                      ) extends UnityMockInstance

  final case
  class UnityMockChannel(sourceId: Long = -1,
                         campaignId: Long = -1,
                         channelId: Long = -1,
                         clickUrl: String = "",
                         parameters: Map[String, String] = Map.empty
                        ) extends UnityMockInstance

  final case
  class UnityMockTrait(id: Long = -1,
                       typeId: Long = -1
                      ) extends UnityMockInstance

  final case
  class UnityMockVariant(id: Long, versionId: Long) extends UnityMockInstance

  final case
  class UnityMockAppVersion(id: Long = -1L) extends UnityMockInstance

}
