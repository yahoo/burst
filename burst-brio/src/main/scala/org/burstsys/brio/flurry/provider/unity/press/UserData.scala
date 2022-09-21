/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity.press

trait UserData extends UnityPressInstanceV1 {

  def application: ApplicationData

  def sessions: Iterator[SessionData]

  def traits: Iterator[TraitData]

  def id: String

  def deviceModelId: Long

  def deviceSubmodelId: Option[Long]

  def deviceFormat: Short

  def estimatedAgeBucket: Option[Byte]

  def estimatedGender: Byte

  def parameters: Map[String, String]

  def interests: Array[Long]

}

case class SyntheticUserData(
                              id: String,
                              deviceModelId: Long,
                              deviceSubmodelId: Option[Long],
                              deviceFormat: Short,
                              estimatedAgeBucket: Option[Byte],
                              estimatedGender: Byte,
                              application: ApplicationData,
                              sessions: Iterator[SessionData],
                              traits: Iterator[TraitData],
                              parameters: Map[String, String],
                              interests: Array[Long],
                            ) extends UserData
