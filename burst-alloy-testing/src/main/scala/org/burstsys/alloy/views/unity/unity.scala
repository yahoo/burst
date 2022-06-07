/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.views

import org.burstsys.brio.flurry.provider.unity._
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.fabric.metadata.model.FabricDomainKey
import org.burstsys.fabric.metadata.model.FabricViewKey
import org.burstsys.vitals.logging._

package object unity extends VitalsLogger {

  def UnityGeneratedDataset(domainKey: FabricDomainKey = 1L,
                            viewKey: FabricViewKey = 10L,
                            numberOfItems: Int = 100, numberOfSessions: Int = 10,
                            timeKey: Long = System.currentTimeMillis()): UnitMiniView = {

    val sessionStartDelta = 100

    val items: Array[BrioPressInstance] =
      (for (i <- 0 until numberOfItems) yield {
        UnityMockUser(
          id = i.toString,
          // code generation 1 to 3 applications depending on the modulo of 3
          application = UnityMockApplication(
            id = 1,
            // code generation 1 or 2 channels depending on the modulo of the item
            channels = (for (k <- 1 until i % 2 + 1) yield {
              UnityMockChannel(
                sourceId = 1L,
                campaignId = 1L,
                channelId = k,
                parameters = Map("" -> "")
              )
            }).toArray,
            parameters = Map("" -> ""),
            firstUse = UnityMockUse(
              sessionTime = timeKey + 1 * 1000,
              osVersionId = 1L,
              agentVersionId = 1L,
              pushTokenStatus = 1,
              timeZone = "",
              timeZoneOffsetSecs = 1,
              limitAdTracking = true,
              cityId = 1L,
              geoAreaId = i % 50 + 1,
              countryId = i % 100 + 1,
              regionId = i % 10 + 1,
              localeId = i % 10 + 1,
              localeCountryId = i % 10 + 1,
              languageId = i % 10 + 1,
              reportedBirthDate = 1L,
              reportedAgeBucket = (i % 2 + 1).toByte,
              reportedGender = (i % 2 + 1).toByte,
              appVersion = UnityMockAppVersion(
                id = 1L
              )
            ),
            lastUse = UnityMockUse(
              sessionTime = timeKey + numberOfSessions * sessionStartDelta,
              osVersionId = 1L,
              agentVersionId = 1L,
              pushTokenStatus = 1,
              timeZone = "",
              timeZoneOffsetSecs = 1,
              limitAdTracking = true,
              cityId = 1L,
              geoAreaId = i % 50 + 1,
              countryId = i % 100 + 1,
              regionId = i % 10 + 1,
              localeId = i % 10 + 1,
              localeCountryId = i % 10 + 1,
              languageId = i % 10 + 1,
              reportedBirthDate = 1L,
              reportedAgeBucket = (i % 2 + 1).toByte,
              reportedGender = (i % 2 + 1).toByte,
              appVersion = UnityMockAppVersion(
                id = 1L
              ),
              crashTime = if (i % 2 == 0) timeKey + 100L else 0L
            ),
            mostUse = UnityMockUse(
              sessionTime = timeKey + 1 * 1000,
              osVersionId = 1L,
              agentVersionId = 1L,
              pushTokenStatus = 1,
              timeZone = "",
              timeZoneOffsetSecs = 1,
              limitAdTracking = true,
              cityId = 1L,
              geoAreaId = i % 50 + 1,
              countryId = i % 100 + 1,
              regionId = i % 10 + 1,
              localeId = i % 10 + 1,
              localeCountryId = i % 10 + 1,
              languageId = i % 10 + 1,
              reportedBirthDate = 1L,
              reportedAgeBucket = (i % 2 + 1).toByte,
              reportedGender = (i % 2 + 1).toByte,
              appVersion = UnityMockAppVersion(
                id = 1L
              )
            )
          ),
          sessions = (for (j <- 1 to numberOfSessions) yield {
            val sessionTimeKey = timeKey + i * sessionStartDelta
            UnityMockSession(
              id = j,
              events = (for (k <- 1 until j * 5) yield {
                UnityMockEvent(
                  id = k, startTime = sessionTimeKey + k, duration = 1L,
                  eventType = if (i == 1) 5 else 1,
                  standardEventId = if (i == 1) 15L else 0,
                  parameters = Map("" -> "")
                )
              }).toArray,
              parameters = Map("" -> ""),
              sessionType = 1,
              pushTokenStatus = 1,
              limitAdTracking = true,
              osVersionId = 1L,
              startTime = sessionTimeKey,
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
              mappedOriginId = 1L,
              originSourceTypeId = 1L,
              originMethodTypeId = 1L,
              reportedBirthDate = 1,
              reportedAgeBucket = 1,
              reportedGender = 1,
              reportingDelay = 1,
              crashed = (j % 2 == 0)
            )
          }).toArray,
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
      }).toArray

    UnitMiniView(schema = BrioSchema("unity"), domainKey = domainKey, viewKey = viewKey, items = items)
  }

}
