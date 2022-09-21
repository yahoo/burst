/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity.press

trait ApplicationData extends UnityPressInstanceV1 {

  def firstUse: FirstUseData

  def lastUse: LastUseData

  def mostUse: MostUseData

  def channels: Iterator[ChannelData]

  def id: Long

  def parameters: Map[String, String]

}

case class SyntheticApplicationData(
                                   id: Long,
                                   firstUse: FirstUseData,
                                   lastUse: LastUseData,
                                   mostUse: MostUseData,
                                   channels: Iterator[ChannelData],
                                   parameters: Map[String, String],
                                   ) extends ApplicationData
