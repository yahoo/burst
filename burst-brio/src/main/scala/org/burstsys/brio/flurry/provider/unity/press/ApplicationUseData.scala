/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity.press

trait ApplicationUseData extends UnityPressInstanceV1 {

  def appVersion: AppVersionData

  def sessionTime: Option[Long]

  def osVersionId: Option[Long]

  def agentVersionId: Option[Long]

  def pushTokenStatus: Option[Byte]

  def timeZone: String

  def timeZoneOffsetSecs: Option[Int]

  def limitAdTracking: Option[Boolean]

  def cityId: Option[Long]

  def geoAreaId: Option[Long]

  def countryId: Option[Long]

  def regionId: Option[Long]

  def localeId: Option[Long]

  def localeCountryId: Option[Long]

  def languageId: Option[Long]

  def reportedBirthDate: Option[Long]

  def reportedAgeBucket: Option[Byte]

  def reportedGender: Option[Byte]

  def crashTime: Option[Long]

}

trait FirstUseData extends ApplicationUseData

trait LastUseData extends ApplicationUseData

trait MostUseData extends ApplicationUseData

case class SyntheticApplicationUseData(
                                        appVersion: AppVersionData,
                                        sessionTime: Option[Long] = Option.empty,
                                        osVersionId: Option[Long] = Option.empty,
                                        agentVersionId: Option[Long] = Option.empty,
                                        pushTokenStatus: Option[Byte] = Option.empty,
                                        timeZone: String,
                                        timeZoneOffsetSecs: Option[Int] = Option.empty,
                                        limitAdTracking: Option[Boolean] = Option.empty,
                                        cityId: Option[Long] = Option.empty,
                                        geoAreaId: Option[Long] = Option.empty,
                                        countryId: Option[Long] = Option.empty,
                                        regionId: Option[Long] = Option.empty,
                                        localeId: Option[Long] = Option.empty,
                                        localeCountryId: Option[Long] = Option.empty,
                                        languageId: Option[Long] = Option.empty,
                                        reportedBirthDate: Option[Long] = Option.empty,
                                        reportedAgeBucket: Option[Byte] = Option.empty,
                                        reportedGender: Option[Byte] = Option.empty,
                                        crashTime: Option[Long] = Option.empty,
                                      ) extends ApplicationUseData
  with FirstUseData with LastUseData with MostUseData


