/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.time

import org.burstsys.vitals.time.VitalsTimeZones.{VitalsTimeZoneKey, VitalsTimeZoneName, VitalsDefaultTimeZoneName}

import scala.language.implicitConversions

/**
  *
  */
trait VitalsLocale extends Any {
  def timezone: VitalsTimeZoneName

  def timeZoneKey: Option[VitalsTimeZoneKey] = VitalsTimeZones.keyForZone(timezone)
}

object VitalsLocale {

  implicit def timezoneNameToString(timezone: VitalsTimeZoneName): VitalsLocale = locale(timezone)


  implicit def apply(tz: Option[String]): VitalsLocale = {
    tz match {
      case None => defaultLocale
      case Some(tzn) => locale(tzn)
    }

  }
}

/**
  *
  * @param timezone
  */
case class locale(timezone: VitalsTimeZoneName = VitalsDefaultTimeZoneName) extends VitalsLocale {
  override def toString: VitalsTimeZoneName = s"'${timezone}''"
}

/**
  *
  */
object defaultLocale extends locale(VitalsDefaultTimeZoneName)
