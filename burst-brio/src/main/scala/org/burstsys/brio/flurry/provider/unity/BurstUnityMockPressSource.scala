/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity

import org.burstsys.brio.press._

final case
class BurstUnityMockPressSource(root: BrioPressInstance) extends BrioPressSourceBase with BrioPressSource {

  override
  def extractRootReferenceScalar(): BrioPressInstance = root

  override
  def extractReferenceScalar(cursor: BrioPressCursor, parentInstance: BrioPressInstance): BrioPressInstance = {
    parentInstance match {
      case o: UnityMockUser => cursor.relationName match {
        case "application" => o.application
        case _ => ???
      }
      case o: UnityMockApplication => cursor.relationName match {
        case "firstUse" => o.firstUse
        case "lastUse" => o.lastUse
        case "mostUse" => o.mostUse
        case _ => ???
      }
      case o: UnityMockUse => cursor.relationName match {
        case "appVersion" => o.appVersion
        case _ => ???
      }
      case o: UnityMockSession => cursor.relationName match {
        case "appVersion" => o.appVersion
        case _ => ???
      }
      case _ => ???
    }
  }

  override
  def extractReferenceVector(cursor: BrioPressCursor, parentInstance: BrioPressInstance): Iterator[BrioPressInstance] = {
    parentInstance match {
      case o: UnityMockUser => cursor.relationName match {
        case "sessions" => o.sessions.iterator
        case "traits" => o.traits.iterator
        case _ => ???
      }
      case o: UnityMockApplication => cursor.relationName match {
        case "channels" => o.channels.iterator
        case _ => ???
      }
      case o: UnityMockSession => cursor.relationName match {
        case "events" => o.events.iterator
        case "variants" => o.variants.iterator
        case _ => ???
      }
      case _ => ???
    }
  }

  override
  def extractValueScalar(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueScalarPressCapture): Unit = {
    parentInstance match {
      case o: UnityMockUser => cursor.relationName match {
        case "id" => capture.stringValue(capture.dictionaryEntry(o.id))
        case "deviceModelId" => capture.longValue(o.deviceModelId)
        case "deviceSubmodelId" => capture.longValue(o.deviceSubmodelId)
        case "deviceFormat" => capture.shortValue(o.deviceFormat)
        case "estimatedAgeBucket" => capture.byteValue(o.estimatedAgeBucket)
        case "estimatedGender" => capture.byteValue(o.estimatedGender)
        case _ => ???
      }
      case o: UnityMockApplication => cursor.relationName match {
        case "id" => capture.longValue(o.id)
        case _ => ???
      }
      case o: UnityMockUse => cursor.relationName match {
        case "sessionTime" => capture.longValue(o.sessionTime)
        case "osVersionId" => capture.longValue(o.osVersionId)
        case "agentVersionId" => capture.longValue(o.agentVersionId)
        case "pushTokenStatus" => capture.byteValue(o.pushTokenStatus)
        case "timeZone" =>extractStringValueWithPossibleNull(capture, o.timeZone)
        case "timeZoneOffsetSecs" => capture.integerValue(o.timeZoneOffsetSecs)
        case "limitAdTracking" => capture.booleanValue(o.limitAdTracking)
        case "cityId" => capture.longValue(o.cityId)
        case "geoAreaId" => capture.longValue(o.geoAreaId)
        case "countryId" => capture.longValue(o.countryId)
        case "regionId" => capture.longValue(o.regionId)
        case "localeId" => capture.longValue(o.localeId)
        case "localeCountryId" => capture.longValue(o.localeCountryId)
        case "languageId" => capture.longValue(o.languageId)
        case "reportedBirthDate" => capture.longValue(o.reportedBirthDate)
        case "reportedAgeBucket" => capture.byteValue(o.reportedAgeBucket)
        case "reportedGender" => capture.byteValue(o.reportedGender)
        case "crashTime" => capture.longValue(o.crashTime)
        case _ => ???
      }
      case o: UnityMockSession => cursor.relationName match {
        case "id" => capture.longValue(o.id)
        case "sessionType" => capture.byteValue(o.sessionType)
        case "applicationUserId" =>extractStringValueWithPossibleNull(capture, o.applicationUserId)
        case "pushTokenStatus" => capture.byteValue(o.pushTokenStatus)
        case "limitAdTracking" => capture.booleanValue(o.limitAdTracking)
        case "osVersionId" => capture.longValue(o.osVersionId)
        case "startTime" => capture.longValue(o.startTime)
        case "timeZone" => extractStringValueWithPossibleNull(capture, o.timeZone)
        case "cityId" => capture.longValue(o.cityId)
        case "geoAreaId" => capture.longValue(o.geoAreaId)
        case "countryId" => capture.longValue(o.countryId)
        case "regionId" => capture.longValue(o.regionId)
        case "localeId" => capture.longValue(o.localeId)
        case "carrierId" => capture.longValue(o.carrierId)
        case "agentVersionId" => capture.longValue(o.agentVersionId)
        case "duration" => capture.longValue(o.duration)
        case "providedOrigin" => extractStringValueWithPossibleNull(capture, o.providedOrigin)
        case "mappedOriginId" => capture.longValue(o.mappedOriginId)
        case "originSourceTypeId" => capture.longValue(o.originSourceTypeId)
        case "originMethodTypeId" => capture.longValue(o.originMethodTypeId)
        case "reportedBirthDate" => capture.longValue(o.reportedBirthDate)
        case "reportedAgeBucket" => capture.byteValue(o.reportedAgeBucket)
        case "reportedGender" => capture.byteValue(o.reportedGender)
        case "reportingDelay" => capture.integerValue(o.reportingDelay)
        case "crashed" => capture.booleanValue(o.crashed)
        case _ => ???
      }
      case o: UnityMockEvent => cursor.relationName match {
        case "id" => capture.longValue(o.id)
        case "eventType" => capture.byteValue(o.eventType)
        case "startTime" => capture.longValue(o.startTime)
        case "duration" => capture.longValue(o.duration)
        case "standardEventId" => capture.longValue(o.standardEventId)
        case _ => ???
      }
      case o: UnityMockChannel => cursor.relationName match {
        case "sourceId" => capture.longValue(o.sourceId)
        case "campaignId" => capture.longValue(o.campaignId)
        case "channelId" => capture.longValue(o.channelId)
        case "clickUrl" => extractStringValueWithPossibleNull(capture, o.clickUrl)
        case _ => ???
      }
      case o: UnityMockTrait => cursor.relationName match {
        case "id" => capture.longValue(o.id)
        case "typeId" => capture.longValue(o.typeId)
        case _ => ???
      }
      case o: UnityMockAppVersion => cursor.relationName match {
        case "id" => capture.longValue(o.id)
        case _ => ???
      }
      case o: UnityMockVariant => cursor.relationName match {
        case "id" => capture.longValue(o.id)
        case "versionId" => capture.longValue(o.versionId)
        case _ => ???
      }

      case _ => ???
    }
  }

  override
  def extractValueMap(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueMapPressCapture): Unit = {
    parentInstance match {
      case o: UnityMockUser => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, o.parameters)
        case _ => ???
      }
      case o: UnityMockApplication => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, o.parameters)
        case _ => ???
      }
      case o: UnityMockEvent => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, o.parameters)
        case _ => ???
      }
      case o: UnityMockChannel => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, o.parameters)
        case _ => ???
      }
      case o: UnityMockSession => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, o.parameters)
        case _ => ???
      }
      case _ => ???
    }
  }

  override
  def extractValueVector(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueVectorPressCapture): Unit = {
    parentInstance match {
      case o: UnityMockUser => cursor.relationName match {
        case "interests" => extractLongValueVector(capture, o.interests)
        case _ => ???
      }
      case o: UnityMockApplication => cursor.relationName match {
        case _ => ???
      }
      case _ => ???
    }
  }

}
