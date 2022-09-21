/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity.press

trait SessionData extends UnityPressInstanceV1 {

  def appVersion: AppVersionData

  def events: Iterator[EventData]

  def variants: Iterator[VariantData]

  def id: Long

  def sessionType: Byte

  def applicationUserId: String

  def pushTokenStatus: Byte

  def limitAdTracking: Boolean

  def osVersionId: Long

  def startTime: Long

  def timeZone: String

  def cityId: Long

  def geoAreaId: Long

  def countryId: Long

  def regionId: Long

  def localeId: Long

  def carrierId: Long

  def agentVersionId: Long

  def duration: Long

  def providedOrigin: String

  def mappedOriginId: Long

  def originSourceTypeId: Long

  def originMethodTypeId: Long

  def reportedBirthDate: Option[Long]

  def reportedAgeBucket: Option[Byte]

  def reportedGender: Byte

  def reportingDelay: Int

  def crashed: Boolean

  def parameters: Map[String, String]

}

case class SyntheticSessionData(
                                 id: Long,
                                 appVersion: AppVersionData,
                                 events: Iterator[EventData],
                                 variants: Iterator[VariantData],
                                 sessionType: Byte,
                                 applicationUserId: String,
                                 pushTokenStatus: Byte,
                                 limitAdTracking: Boolean,
                                 osVersionId: Long,
                                 startTime: Long,
                                 timeZone: String,
                                 cityId: Long,
                                 geoAreaId: Long,
                                 countryId: Long,
                                 regionId: Long,
                                 localeId: Long,
                                 carrierId: Long,
                                 agentVersionId: Long,
                                 duration: Long,
                                 providedOrigin: String,
                                 mappedOriginId: Long,
                                 originSourceTypeId: Long,
                                 originMethodTypeId: Long,
                                 reportedBirthDate: Option[Long],
                                 reportedAgeBucket: Option[Byte],
                                 reportedGender: Byte,
                                 reportingDelay: Int,
                                 crashed: Boolean,
                                 parameters: Map[String, String],
                               ) extends SessionData
