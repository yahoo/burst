/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.canned

import org.burstsys.catalog.api.BurstCatalogApiQueryLanguageType.Eql
import org.burstsys.catalog.canned.CatalogCan
import org.burstsys.catalog.model.query.CatalogCannedQuery

object EqlQueriesCan {
  val uiUnityMetadataSourceV1: String =
    s"""
       |select count(user) as userCount
       |      beside select count(user.sessions) as sessionCount
       |      beside select count(user.sessions.events) as eventCount
       |      beside select min(user.application.firstUse.sessionTime) as minInstallTime
       |      beside select max(user.application.firstUse.sessionTime) as maxInstallTime
       |      beside select min(user.sessions.startTime) as mixInstallTime
       |      beside select max(user.sessions.startTime) as maxInstallTime
       |      beside select count(user) as users, user.deviceModelId as deviceModel
       |      beside select count(user.sessions) as sessions, user.sessions.osVersionId as osVersionId
       |      beside select count(user.application) as projects, user.application.firstUse.languageId as languageId
       |      beside select count(user) as users, user.sessions.appVersion.id as appVersion
       |      beside select count(user.sessions) as sessions, user.sessions.providedOrigin as providedOrigin
       |      beside select count(user.sessions) as sessions, user.sessions.mappedOriginId as mappedOrigin
       |      beside select count(user.sessions) as sessions, user.sessions.originSourceTypeId as originSourceType
       |      beside select count(user.sessions) as sessions, user.sessions.originMethodTypeId as originMethodType
       |      beside select count(user.sessions.events) as eventFrequency, user.sessions.events.id as eventId
       |      beside select count(user.sessions) as localeIdFrequency, user.sessions.localeId as localeId
       |      beside select count(user) as campaignIdFrequency, user.application.channels.campaignId as campaignId
       |      beside select count(user) as channelIdFrequency, user.application.channels.channelId as channelId
       |      beside select count(user.sessions) as sessionParameterFrequency, user.sessions.parameters.key as sessionParameterKey
       |      beside select count(user.sessions.events) as eventParameterFrequency, user.sessions.events.id as eventId, user.sessions.events.parameters.key as eventParameterKey
       |from schema unity
       """.stripMargin

  val uiUnityMetadataSourceV2: String =
    s"""
       |select count(user) as userCount limit 1
       |     beside select count(user.sessions) as sessionCount limit 1
       |     beside select count(user.sessions.events) as eventCount limit 1
       |     beside select min(user.sessions.startTime) as minSessionTime limit 1
       |     beside select max(user.sessions.startTime) as maxSessionTime limit 1
       |     beside select count(user) as users, user.deviceModelId as deviceModelIds limit 1000
       |     beside select count(user.sessions) as sessions, user.sessions.osVersionId as osVersionIds limit 400
       |     beside select count(user.application) as projects, user.application.firstUse.languageId as languageId limit 400
       |     beside select count(user) as users, user.sessions.appVersion.id as appVersionIds  limit 1000
       |     beside select count(user.sessions) as sessions, user.sessions.providedOrigin as providedOrigins  limit 100
       |     beside select count(user.sessions) as sessions, user.sessions.mappedOriginId as mappedOrigin  limit 50
       |     beside select count(user.sessions) as sessions, user.sessions.originSourceTypeId as originSourceType  limit 10
       |     beside select count(user.sessions) as sessions, user.sessions.originMethodTypeId as originMethodType  limit 10
       |     beside select count(user.sessions.events) as eventFrequency, user.sessions.events.id as events  limit 1400
       |     beside select count(user) as campaignIdFrequency, user.application.channels.campaignId as campaignId limit 2000
       |     beside select count(user) as channelIdFrequency, user.application.channels.channelId as channelId limit 200
       |     beside select count(user.sessions) as sessionParameterFrequency, user.sessions.parameters.key as sessionParameterKeys limit 200
       |     beside select count(user.sessions.events) as eventParameterFrequency, user.sessions.events.id as eventParameters, user.sessions.events.parameters.key as eventParameterKey limit  14000
       |from schema unity
       """.stripMargin

  val uiUnityMetadataSourceV3: String =
    s"""
       |select count(user) as userCount limit 1
       |     beside select count(user.sessions) as sessionCount limit 1
       |     beside select count(user.sessions.events) as eventCount limit 1
       |     beside select min(user.sessions.startTime) as minSessionTime limit 1
       |     beside select max(user.sessions.startTime) as maxSessionTime limit 1
       |     beside select top[1000](user) as users, user.deviceModelId as deviceModelIds limit 1000
       |     beside select top[400](user.sessions) as sessions, user.sessions.osVersionId as osVersionIds limit 400
       |     beside select top[400](user.application) as projects, user.application.firstUse.languageId as languageId limit 400
       |     beside select top[1000](user) as users, user.sessions.appVersion.id as appVersionIds  limit 1000
       |     beside select top[100](user.sessions) as sessions, user.sessions.providedOrigin as providedOrigins  limit 100
       |     beside select top[50](user.sessions) as sessions, user.sessions.mappedOriginId as mappedOrigin  limit 50
       |     beside select top[10](user.sessions) as sessions, user.sessions.originSourceTypeId as originSourceType  limit 10
       |     beside select top[10](user.sessions) as sessions, user.sessions.originMethodTypeId as originMethodType  limit 10
       |     beside select top[1400](user.sessions.events) as eventFrequency, user.sessions.events.id as events  limit 1400
       |     beside select top[2000](user) as campaignIdFrequency, user.application.channels.campaignId as campaignId limit 2000
       |     beside select top[200](user) as channelIdFrequency, user.application.channels.channelId as channelId limit 200
       |     beside select top[200](user.sessions) as sessionParameterFrequency, user.sessions.parameters.key as sessionParameterKeys limit 200
       |     beside select top[14000](user.sessions.events) as eventParameterFrequency, user.sessions.events.id as eventParameters, user.sessions.events.parameters.key as eventParameterKey limit  14000
       |from schema unity
       """.stripMargin

  val uiUnityMetadataSourceV4: String =
    s"""
       |select count(user) as userCount limit 1
       |     beside select count(user.sessions) as sessionCount
       |     beside select count(user.sessions.events) as eventCount
       |     beside select min(user.sessions.startTime) as minSessionTime
       |     beside select max(user.sessions.startTime) as maxSessionTime
       |     beside select top[1000](user) as users, user.deviceModelId as deviceModelIds
       |     beside select top[400](user.sessions) as sessions, user.sessions.osVersionId as osVersionIds
       |     beside select top[400](user.application) as projects, user.application.firstUse.languageId as languageId
       |     beside select top[1000](user) as users, user.sessions.appVersion.id as appVersionIds
       |     beside select top[100](user.sessions) as sessions, user.sessions.providedOrigin as providedOrigins
       |     beside select top[50](user.sessions) as sessions, user.sessions.mappedOriginId as mappedOrigin
       |     beside select top[10](user.sessions) as sessions, user.sessions.originSourceTypeId as originSourceType
       |     beside select top[10](user.sessions) as sessions, user.sessions.originMethodTypeId as originMethodType
       |     beside select top[1400](user.sessions.events) as eventFrequency, user.sessions.events.id as events
       |     beside select top[2000](user) as campaignIdFrequency, user.application.channels.campaignId as campaignId
       |     beside select top[200](user) as channelIdFrequency, user.application.channels.channelId as channelId
       |     beside select top[200](user.sessions) as sessionParameterFrequency, user.sessions.parameters.key as sessionParameterKeys
       |     beside select top[14000](user.sessions.events) as eventParameterFrequency, user.sessions.events.id as eventParameters, user.sessions.events.parameters.key as eventParameterKey
       |from schema unity
     """.stripMargin

  val uiQuoMetadataSourceV1: String =
    s"""
       |select count(user) as userCount limit 1
       |     beside select count(user.sessions) as sessionCount limit 1
       |     beside select count(user.sessions.events) as eventCount limit 1
       |     beside select max(user.sessions.startTime) as maxSessionTime limit 1
       |     beside select count(user) as users, user.deviceModelId as deviceModelIds limit 1000
       |     beside select count(user.sessions) as sessions, user.sessions.osVersion as osVersionIds limit 400
       |     beside select count(user.project) as projects, user.project.languageId as languageIds limit 400
       |     beside select count(user) as users, user.sessions.appVersionId as appVersionIds limit 1000
       |     beside select count(user.sessions) as sessions, user.sessions.providedOrigin as providedOrigins limit 100
       |     beside select count(user.sessions) as sessions, user.sessions.mappedOrigin as mappedOrigin limit 50
       |     beside select count(user.sessions) as sessions, user.sessions.originSourceType as originSourceType limit 10
       |     beside select count(user.sessions) as sessions, user.sessions.originMethodType as originMethodType limit 10
       |     beside select count(user.sessions.events) as eventFrequency, user.sessions.events.eventId as events limit 1400
       |     beside select count(user.sessions.events) as eventParameterFrequency, user.sessions.events.eventId as eventParameters, user.sessions.events.parameters.key as eventParameterKey limit 14000
       |     beside select count(user) as campaignIdFrequency, user.channels.channelId as campaignId limit 2000
       |     beside select count(user) as channelIdFrequency, user.channels.networkId as channelId limit 200
       |     beside select count(user.sessions) as sessionParameterFrequency, user.sessions.parameters.key as sessionParameterKeys limit 200
       |from schema quo
     """.stripMargin

  val uiQuoMetadataSourceV2: String =
    s"""
       |select count(user) as userCount, count(user.sessions) as sessionCount, count(user.sessions.events) as eventCount,
       |       max(user.sessions.startTime) as maxSessionTime limit 1
       |     beside select count(user) as users, user.deviceModelId as deviceModelIds limit 1000
       |     beside select count(user.sessions) as sessions, user.sessions.osVersion as osVersionIds limit 400
       |     beside select count(user.project) as projects, user.project.languageId as languageIds limit 400
       |     beside select count(user) as users, user.sessions.appVersionId as appVersionIds limit 1000
       |     beside select count(user.sessions) as sessions, user.sessions.providedOrigin as providedOrigins limit 100
       |     beside select count(user.sessions) as sessions, user.sessions.mappedOrigin as mappedOrigin limit 50
       |     beside select count(user.sessions) as sessions, user.sessions.originSourceType as originSourceType limit 10
       |     beside select count(user.sessions) as sessions, user.sessions.originMethodType as originMethodType limit 10
       |     beside select count(user.sessions.events) as eventFrequency, user.sessions.events.eventId as events limit 1400
       |     beside select count(user.sessions.events) as eventParameterFrequency, user.sessions.events.eventId as eventParameters, user.sessions.events.parameters.key as eventParameterKey limit 14000
       |     beside select count(user) as campaignIdFrequency, user.channels.channelId as campaignId limit 2000
       |     beside select count(user) as channelIdFrequency, user.channels.networkId as channelId limit 200
       |     beside select count(user.sessions) as sessionParameterFrequency, user.sessions.parameters.key as sessionParameterKeys limit 200
       |from schema quo
     """.stripMargin

  val uiQuoMetadataSourceV3: String =
    s"""
       |select count(user) as userCount, count(user.sessions) as sessionCount, count(user.sessions.events) as eventCount,
       |       max(user.sessions.startTime) as maxSessionTime limit 1
       |     beside select top[1000](user) as users, user.deviceModelId as deviceModelIds limit 1000
       |     beside select top[400](user.sessions) as sessions, user.sessions.osVersion as osVersionIds limit 400
       |     beside select top[400](user.project) as projects, user.project.languageId as languageIds limit 400
       |     beside select top[1000](user) as users, user.sessions.appVersionId as appVersionIds limit 1000
       |     beside select top[100](user.sessions) as sessions, user.sessions.providedOrigin as providedOrigins limit 100
       |     beside select top[50](user.sessions) as sessions, user.sessions.mappedOrigin as mappedOrigin limit 50
       |     beside select top[10](user.sessions) as sessions, user.sessions.originSourceType as originSourceType limit 10
       |     beside select top[10](user.sessions) as sessions, user.sessions.originMethodType as originMethodType limit 10
       |     beside select top[1400](user.sessions.events) as eventFrequency, user.sessions.events.eventId as events limit 1400
       |     beside select top[14000](user.sessions.events) as eventParameterFrequency, user.sessions.events.eventId as eventParameters, user.sessions.events.parameters.key as eventParameterKey limit 14000
       |     beside select top[2000](user) as campaignIdFrequency, user.channels.channelId as campaignId limit 2000
       |     beside select top[200](user) as channelIdFrequency, user.channels.networkId as channelId limit 200
       |     beside select top[200](user.sessions) as sessionParameterFrequency, user.sessions.parameters.key as sessionParameterKeys limit 200
       |from schema quo
       """.stripMargin

  val uiUnityDimensionUsageSourceV1: String =
    s"""
       | select count(user) as frequency, user.deviceModelId as deviceIds limit 1000
       |      beside select count(user.sessions) as frequency, user.sessions.osVersionId as firmwareIds limit 200
       |      beside select count(user.application) as projects, user.application.firstUse.languageId as languageId limit 200
       |      beside select count(user) as frequency, user.sessions.appVersion.id as versionIds limit 1000
       |      beside select count(user.sessions.events) as frequency, user.sessions.events.id as eventIds limit 1000
       |      from schema unity
       """.stripMargin

  val uiUnityDimensionUsageSourceV2: String =
    s"""
       | select top[1000](user) as frequency, user.deviceModelId as deviceIds limit 1000
       |      beside select top[200](user.sessions) as frequency, user.sessions.osVersionId as firmwareIds limit 200
       |      beside select top[200](user.application) as projects, user.application.firstUse.languageId as languageId limit 200
       |      beside select top[1000](user) as frequency, user.sessions.appVersion.id as versionIds limit 1000
       |      beside select top[1000](user.sessions.events) as frequency, user.sessions.events.id as eventIds limit 1000
       |      from schema unity
       """.stripMargin

  val uiUnityDimensionUsageSourceV3: String =
    s"""
       | select top[1000](user) as frequency, user.deviceModelId as deviceIds
       |      beside select top[200](user.sessions) as frequency, user.sessions.osVersionId as firmwareIds
       |      beside select top[200](user.application) as projects, user.application.firstUse.languageId as languageId
       |      beside select top[1000](user) as frequency, user.sessions.appVersion.id as versionIds
       |      beside select top[1000](user.sessions.events) as frequency, user.sessions.events.id as eventIds
       |      from schema unity
     """.stripMargin

  def apply(): EqlQueriesCan = new EqlQueriesCan()
}

final class EqlQueriesCan extends CatalogCan {

  override def queries: Array[CatalogCannedQuery] = {
    Array(
      CatalogCannedQuery("UI Metadata UNITY", Eql, EqlQueriesCan.uiUnityMetadataSourceV2),
      CatalogCannedQuery("UI Metadata TOP UNITY", Eql, EqlQueriesCan.uiUnityMetadataSourceV3),
      CatalogCannedQuery("UI Dimension Usage UNITY", Eql, EqlQueriesCan.uiUnityDimensionUsageSourceV2),
      CatalogCannedQuery("UI Metadata QUO", Eql, EqlQueriesCan.uiQuoMetadataSourceV2),
      CatalogCannedQuery("UI Metadata TOP QUO", Eql, EqlQueriesCan.uiQuoMetadataSourceV3)
    )
  }
}
