/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity.press

trait VariantData extends UnityPressInstanceV1 {

  def id: Long

  def versionId: Long

}

case class SyntheticVariantData(
                               id: Long,
                               versionId: Long,
                               ) extends VariantData
