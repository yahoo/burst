/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity

import org.burstsys.brio.flurry.provider.unity.press.AppVersionData
import org.burstsys.brio.flurry.provider.unity.press.ApplicationData
import org.burstsys.brio.flurry.provider.unity.press.ApplicationUseData
import org.burstsys.brio.flurry.provider.unity.press.ChannelData
import org.burstsys.brio.flurry.provider.unity.press.EventData
import org.burstsys.brio.flurry.provider.unity.press.FirstUseData
import org.burstsys.brio.flurry.provider.unity.press.LastUseData
import org.burstsys.brio.flurry.provider.unity.press.MostUseData
import org.burstsys.brio.flurry.provider.unity.press.SessionData
import org.burstsys.brio.flurry.provider.unity.press.TraitData
import org.burstsys.brio.flurry.provider.unity.press.UserData
import org.burstsys.brio.flurry.provider.unity.press.VariantData
import org.burstsys.brio.press.BrioPressCursor
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.press.BrioPressSourceBase
import org.burstsys.brio.press.BrioValueMapPressCapture
import org.burstsys.brio.press.BrioValueScalarPressCapture
import org.burstsys.brio.press.BrioValueVectorPressCapture

final case
class UnityPressSource(root: BrioPressInstance) extends BrioPressSourceBase {

  override
  def extractRootReferenceScalar(): BrioPressInstance = root

  override
  def extractReferenceScalar(cursor: BrioPressCursor, parentInstance: BrioPressInstance): BrioPressInstance = {
    parentInstance match {
      case user: UserData => cursor.relationName match {
        case "application" => user.application
        case name => user.unknownRelation(name)
      }
      case app: ApplicationData => cursor.relationName match {
        case "firstUse" => app.firstUse
        case "lastUse" => app.lastUse
        case "mostUse" => app.mostUse
        case name => app.unknownRelation(name)
      }
      case session: SessionData => cursor.relationName match {
        case "appVersion" => session.appVersion
        case name => session.unknownRelation(name)
      }
      case use: FirstUseData => cursor.relationName match {
        case "appVersion" => use.appVersion
        case name => use.unknownRelation(name)
      }
      case use: LastUseData => cursor.relationName match {
        case "appVersion" => use.appVersion
        case name => use.unknownRelation(name)
      }
      case use: MostUseData => cursor.relationName match {
        case "appVersion" => use.appVersion
        case name => use.unknownRelation(name)
      }
      case _ => unknownParentInstance(parentInstance, cursor.relationName)
    }
  }

  override
  def extractReferenceVector(cursor: BrioPressCursor, parentInstance: BrioPressInstance): Iterator[BrioPressInstance] = {
    parentInstance match {
      case user: UserData => cursor.relationName match {
        case "sessions" => user.sessions
        case "traits" => user.traits
        case name => user.unknownRelation(name)
      }
      case app: ApplicationData => cursor.relationName match {
        case "channels" => app.channels
        case name => app.unknownRelation(name)
      }
      case session: SessionData => cursor.relationName match {
        case "events" => session.events
        case "variants" => session.variants
        case name => session.unknownRelation(name)
      }
      case _ => unknownParentInstance(parentInstance, cursor.relationName)
    }
  }

  override
  def extractValueScalar(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueScalarPressCapture): Unit = {
    parentInstance match {
      case user: UserData => cursor.relationName match {
        case "id" => pressString(capture, user.id)
        case "deviceModelId" => capture.longValue(user.deviceModelId)
        case "deviceSubmodelId" => capture.longValue(user.deviceSubmodelId)
        case "deviceFormat" => capture.shortValue(user.deviceFormat)
        case "estimatedAgeBucket" => capture.byteValue(user.estimatedAgeBucket)
        case "estimatedGender" => capture.byteValue(user.estimatedGender)
        case name => user.unknownRelation(name)
      }
      case app: ApplicationData => cursor.relationName match {
        case "id" => capture.longValue(app.id)
        case name => app.unknownRelation(name)
      }
      case session: SessionData => cursor.relationName match {
        case "id" => capture.longValue(session.id)
        case "sessionType" => capture.byteValue(session.sessionType)
        case "applicationUserId" => pressString(capture, session.applicationUserId)
        case "pushTokenStatus" => capture.byteValue(session.pushTokenStatus)
        case "limitAdTracking" => capture.booleanValue(session.limitAdTracking)
        case "osVersionId" => capture.longValue(session.osVersionId)
        case "startTime" => capture.longValue(session.startTime)
        case "timeZone" => pressString(capture, session.timeZone)
        case "cityId" => capture.longValue(session.cityId)
        case "geoAreaId" => capture.longValue(session.geoAreaId)
        case "countryId" => capture.longValue(session.countryId)
        case "regionId" => capture.longValue(session.regionId)
        case "localeId" => capture.longValue(session.localeId)
        case "carrierId" => capture.longValue(session.carrierId)
        case "agentVersionId" => capture.longValue(session.agentVersionId)
        case "duration" => capture.longValue(session.duration)
        case "providedOrigin" => pressString(capture, session.providedOrigin)
        case "mappedOriginId" => capture.longValue(session.mappedOriginId)
        case "originSourceTypeId" => capture.longValue(session.originSourceTypeId)
        case "originMethodTypeId" => capture.longValue(session.originMethodTypeId)
        case "reportedBirthDate" => capture.longValue(session.reportedBirthDate)
        case "reportedAgeBucket" => capture.byteValue(session.reportedAgeBucket)
        case "reportedGender" => capture.byteValue(session.reportedGender)
        case "reportingDelay" => capture.integerValue(session.reportingDelay)
        case "crashed" => capture.booleanValue(session.crashed)
        case name => session.unknownRelation(name)
      }
      case event: EventData => cursor.relationName match {
        case "id" => capture.longValue(event.id)
        case "eventType" => capture.byteValue(event.eventType)
        case "startTime" => capture.longValue(event.startTime)
        case "duration" => capture.longValue(event.duration)
        case "standardEventId" => capture.longValue(event.standardEventId)
        case name => event.unknownRelation(name)
      }
      case use: ApplicationUseData => cursor.relationName match {
        case "sessionTime" => capture.longValue(use.sessionTime)
        case "osVersionId" => capture.longValue(use.osVersionId)
        case "agentVersionId" => capture.longValue(use.agentVersionId)
        case "pushTokenStatus" => capture.byteValue(use.pushTokenStatus)
        case "timeZone" => pressString(capture, use.timeZone)
        case "timeZoneOffsetSecs" => capture.integerValue(use.timeZoneOffsetSecs)
        case "limitAdTracking" => capture.booleanValue(use.limitAdTracking)
        case "cityId" => capture.longValue(use.cityId)
        case "geoAreaId" => capture.longValue(use.geoAreaId)
        case "countryId" => capture.longValue(use.countryId)
        case "regionId" => capture.longValue(use.regionId)
        case "localeId" => capture.longValue(use.localeId)
        case "localeCountryId" => capture.longValue(use.localeCountryId)
        case "languageId" => capture.longValue(use.languageId)
        case "reportedBirthDate" => capture.longValue(use.reportedBirthDate)
        case "reportedAgeBucket" => capture.byteValue(use.reportedAgeBucket)
        case "reportedGender" => capture.byteValue(use.reportedGender)
        case "crashTime" => capture.longValue(use.crashTime)
        case name => use.unknownRelation(name)
      }
      case channel: ChannelData => cursor.relationName match {
        case "sourceId" => capture.longValue(channel.sourceId)
        case "campaignId" => capture.longValue(channel.campaignId)
        case "channelId" => capture.longValue(channel.channelId)
        case name => channel.unknownRelation(name)
      }
      case traitData: TraitData => cursor.relationName match {
        case "id" => capture.longValue(traitData.id)
        case "typeId" => capture.longValue(traitData.typeId)
        case name => traitData.unknownRelation(name)
      }
      case version: AppVersionData => cursor.relationName match {
        case "id" => capture.longValue(version.id)
        case name => version.unknownRelation(name)
      }
      case variant: VariantData => cursor.relationName match {
        case "id" => capture.longValue(variant.id)
        case "versionId" => capture.longValue(variant.versionId)
        case name => variant.unknownRelation(name)
      }
      case _ => unknownParentInstance(parentInstance, cursor.relationName)
    }
  }

  override
  def extractValueMap(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueMapPressCapture): Unit = {
    parentInstance match {
      case user: UserData => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, user.parameters)
        case name => user.unknownRelation(name)
      }
      case app: ApplicationData => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, app.parameters)
        case name => app.unknownRelation(name)
      }
      case session: SessionData => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, session.parameters)
        case name => session.unknownRelation(name)
      }
      case event: EventData => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, event.parameters)
        case name => event.unknownRelation(name)
      }
      case channel: ChannelData => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, channel.parameters)
        case name => channel.unknownRelation(name)
      }
      case _ => unknownParentInstance(parentInstance, cursor.relationName)
    }
  }

  override
  def extractValueVector(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueVectorPressCapture): Unit = {
    parentInstance match {
      case user: UserData => cursor.relationName match {
        case "interests" => extractLongValueVector(capture, user.interests)
        case name => user.unknownRelation(name)
      }
      case _ => unknownParentInstance(parentInstance, cursor.relationName)
    }
  }

}
