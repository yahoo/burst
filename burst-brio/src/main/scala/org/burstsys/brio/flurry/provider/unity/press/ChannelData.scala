/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity.press

trait ChannelData extends UnityPressInstanceV1 {

  def sourceId: Long

  def campaignId: Long

  def channelId: Option[Long]

  def parameters: Map[String, String]

}

case class SyntheticChannelData(
                               sourceId: Long,
                               campaignId: Long,
                               channelId: Option[Long],
                               parameters: Map[String, String],
                               ) extends ChannelData
