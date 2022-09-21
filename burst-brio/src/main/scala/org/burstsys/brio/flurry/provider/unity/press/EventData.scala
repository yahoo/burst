/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity.press

trait EventData extends UnityPressInstanceV1 {

  def id: Long

  def eventType: Byte

  def startTime: Long

  def duration: Long

  def standardEventId: Long

  def parameters: Map[String, String]

}

case class SyntheticEventData(
                             id: Long,
                             eventType: Byte,
                             startTime: Long,
                             duration: Long,
                             standardEventId: Long,
                             parameters: Map[String, String],
                             ) extends EventData
