/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity

import org.burstsys.brio.model.BrioMockDataModel
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.press.BrioPressSource

/**
  * create mock data for unity schema. Note to support unit testing (and perhaps other users) this must be
  * deterministic - if you provide the same input parameters, the result will be identical
  *
  * @param itemCount
  */
final case
class BurstUnityMockData(itemCount: Int = 1000) extends BrioMockDataModel {


  override
  val schema: BrioSchema =  BrioSchema("unity")

  override
  def pressSource(root: BrioPressInstance): BrioPressSource = BurstUnityMockPressSource(root)

  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  lazy val items: Seq[UnityMockInstance] =
    for (_ <- 0 until itemCount) yield {
      UnityMockUser(
        id = "",
        application = UnityMockApplication(
          id = 1L,
          channels = Array(
            UnityMockChannel(
              sourceId = 1L,
              campaignId = 1L,
              channelId = 1L,
              clickUrl = "",
              parameters = Map("" -> "")
            )
          ),
          parameters = Map("" -> ""),
          firstUse = UnityMockUse(
              sessionTime = 1L,
              osVersionId = 1L,
              agentVersionId = 1L,
              cityId = 1L,
              geoAreaId = 1L,
              countryId = 1L,
              regionId = 1L,
              localeId = 1L,
              localeCountryId = 1L,
              languageId = 1L,
              reportedBirthDate = 1L,
              reportedAgeBucket = 1,
              reportedGender = 1,
              pushTokenStatus = 1,
              timeZone = "",
              timeZoneOffsetSecs = 1,
              limitAdTracking = true,
              appVersion = UnityMockAppVersion(
                  id = 1L
              )
          ),
          lastUse = UnityMockUse(
              sessionTime = 1L,
              osVersionId = 1L,
              agentVersionId = 1L,
              cityId = 1L,
              geoAreaId = 1L,
              countryId = 1L,
              regionId = 1L,
              localeId = 1L,
              localeCountryId = 1L,
              languageId = 1L,
              reportedBirthDate = 1L,
              reportedAgeBucket = 1,
              reportedGender = 1,
              pushTokenStatus = 1,
              timeZone = "",
              timeZoneOffsetSecs = 1,
              limitAdTracking = true,
              appVersion = UnityMockAppVersion(
                  id = 1L
              )
          ),
          mostUse = UnityMockUse(
              sessionTime = 1L,
              osVersionId = 1L,
              agentVersionId = 1L,
              cityId = 1L,
              geoAreaId = 1L,
              countryId = 1L,
              regionId = 1L,
              localeId = 1L,
              localeCountryId = 1L,
              languageId = 1L,
              reportedBirthDate = 1L,
              reportedAgeBucket = 1,
              reportedGender = 1,
              pushTokenStatus = 1,
              timeZone = "",
              timeZoneOffsetSecs = 1,
              limitAdTracking = true,
              appVersion = UnityMockAppVersion(
                  id = 1L
              )
          )
        ),
        sessions = Array(
          UnityMockSession(
            id = 1L,
            events = Array(
              UnityMockEvent(
                id = 1L, startTime = 1L, duration = 1L, eventType = 1, standardEventId = 1L,
                parameters = Map("" -> "")
              )
            ),
            variants = Array(
              UnityMockVariant(id = 1L, versionId = 1L)
            ),
            parameters = Map("" -> ""),
            sessionType = 1,
            applicationUserId = "",
            pushTokenStatus = 1,
            limitAdTracking = true,
            osVersionId = 1L,
            startTime = 1L,
            timeZone = "",
            cityId = 1L,
            geoAreaId = 1L,
            countryId = 1L,
            regionId = 1L,
            localeId = 1L,
            carrierId = 1L,
            agentVersionId = 1L,
            appVersion = UnityMockAppVersion(
              id = 1L
            ),
            duration = 1,
            providedOrigin = "",
            mappedOriginId = 1L,
            originSourceTypeId = 1L,
            originMethodTypeId = 1L,
            reportedBirthDate = 1,
            reportedAgeBucket = 1,
            reportedGender = 1,
            reportingDelay = 1
          )
        ),
        interests = Array(
          x = 1L
        ),
        traits = Array(
          UnityMockTrait(
            id = 1L, typeId = 2L
          )
        ),
        parameters = Map("" -> ""),
        deviceModelId = 1L,
        deviceSubmodelId = 1L,
        deviceFormat = 1,
        estimatedAgeBucket = 1,
        estimatedGender = 1
      )
    }


}
