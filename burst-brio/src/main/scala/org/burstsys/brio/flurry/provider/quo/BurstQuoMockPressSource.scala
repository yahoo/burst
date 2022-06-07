/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.quo

import org.burstsys.brio.press._

final case
class BurstQuoMockPressSource(root: BrioPressInstance) extends BrioPressSourceBase with BrioPressSource {

  override
  def extractRootReferenceScalar(): BrioPressInstance = root

  override
  def extractReferenceScalar(cursor: BrioPressCursor, parentInstance: BrioPressInstance): BrioPressInstance = {
    parentInstance match {
      case o: QuoMockUser => cursor.relationName match {
        case "project" => o.project
        case _ => ???
      }
      case _ => ???
    }
  }

  override
  def extractReferenceVector(cursor: BrioPressCursor, parentInstance: BrioPressInstance): Iterator[BrioPressInstance] = {
    parentInstance match {
      case o: QuoMockUser => cursor.relationName match {
        case "sessions" => o.sessions.sortBy(_.startTime).iterator // make sure its sorted
        case "segments" => o.segments.iterator // not sorted
        case "channels" => o.channels.iterator // not sorted
        case "personas" => o.personas.iterator // not sorted
        case _ => ???
      }
      case o: QuoMockSession => cursor.relationName match {
        case "events" => o.events.sortBy(_.startTime).iterator // make sure its sorted
        case _ => ???
      }
      case _ => ???
    }
  }

  override
  def extractValueScalar(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueScalarPressCapture): Unit = {
    parentInstance match {
      case o: QuoMockUser => cursor.relationName match {
        case "flurryId" =>
          if (o.flurryId == null) {
            capture.markRelationNull()
          } else capture.stringValue(capture.dictionaryEntry(o.flurryId))
        case "deviceModelId" => capture.longValue(o.deviceModelId)
        case "deviceSubModelId" => capture.longValue(o.deviceSubModelId)
        case _ => ???
      }
      case o: QuoMockProject => cursor.relationName match {
        case "projectId" => capture.longValue(o.projectId)
        case "birthDate" => capture.longValue(o.birthDate)
        case "cityId" => capture.longValue(o.cityId)
        case "countryId" => capture.longValue(o.countryId)
        case "languageId" => capture.longValue(o.languageId)
        case "localeId" => capture.longValue(o.localeId)
        case "installTime" => capture.longValue(o.installTime)
        case "regionId" => capture.longValue(o.regionId)
        case "lastUsedTime" => capture.longValue(o.lastUsedTime)
        case "retainedTime" => capture.longValue(o.retainedTime)
        case "stateId" => capture.longValue(o.stateId)
        case "gender" => capture.byteValue(o.gender)
        case _ => ???
      }
      case o: QuoMockSession => cursor.relationName match {
        case "sessionId" => capture.longValue(o.sessionId)
        case "osVersion" => capture.longValue(o.osVersion)
        case "totalErrors" => capture.longValue(o.totalErrors)
        case "totalEvents" => capture.longValue(o.totalEvents)
        case "carrierId" => capture.longValue(o.carrierId)
        case "stateId" => capture.longValue(o.stateId)
        case "cityId" => capture.longValue(o.cityId)
        case "localeId" => capture.longValue(o.localeId)
        case "regionId" => capture.longValue(o.regionId)
        case "birthDateReported" => capture.longValue(o.birthDateReported)
        case "genderReported" => capture.byteValue(o.genderReported)
        case "timeZoneId" => capture.longValue(o.timeZoneId)
        case "startTime" => capture.longValue(o.startTime)
        case "duration" => capture.longValue(o.duration)
        case "appVersionId" => capture.longValue(o.appVersionId)
        case "agentVersionId" => capture.longValue(o.agentVersionId)
        case "countryId" => capture.longValue(o.countryId)
        case "originMethodType" => capture.longValue(o.originMethodType)
        case "originSourceType" => capture.longValue(o.originSourceType)
        case "mappedOrigin" => capture.longValue(o.mappedOrigin)
        case "providedOrigin" =>
          if (o.providedOrigin == null) capture.markRelationNull()
          else capture.stringValue(capture.dictionaryEntry(o.providedOrigin))
        case _ => ???
      }
      case o: QuoMockEvent => cursor.relationName match {
        case "eventId" => capture.longValue(o.eventId)
        case "duration" => capture.longValue(o.duration)
        case "startTime" => capture.longValue(o.startTime)
        case "eventType" => capture.byteValue(o.eventType)
        case "order" => capture.integerValue(o.order)
        case _ => ???
      }
      case o: QuoMockSegment => cursor.relationName match {
        case "segmentId" => capture.longValue(o.segmentId)
        case _ => ???
      }
      case o: QuoMockPersona => cursor.relationName match {
        case "personaId" => capture.longValue(o.personaId)
        case _ => ???
      }
      case o: QuoMockChannel => cursor.relationName match {
        case "channelId" => capture.longValue(o.channelId)
        case "networkId" => capture.longValue(o.networkId)
        case "isQuality" => capture.booleanValue(o.isQuality)
        case _ => ???
      }
      case _ => ???
    }
  }

  override
  def extractValueMap(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueMapPressCapture): Unit = {
    parentInstance match {
      case o: QuoMockUser => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, o.parameters)
        case _ => ???
      }
      case o: QuoMockSession => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, o.parameters)
      }
      case o: QuoMockEvent => cursor.relationName match {
        case "parameters" => extractStringStringMap(capture, o.parameters)
      }
      case _ => ???
    }
  }

  override
  def extractValueVector(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueVectorPressCapture): Unit = {
    ??? // no value vectors in quo at this point
  }

}
