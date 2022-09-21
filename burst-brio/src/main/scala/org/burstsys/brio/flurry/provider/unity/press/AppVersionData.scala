/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity.press

trait AppVersionData extends UnityPressInstanceV1 {

  def id: Long

}

case class SyntheticAppVersionData(id: Long) extends AppVersionData
