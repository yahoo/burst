/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.time

import org.joda.time.DateTimeZone

import scala.language.implicitConversions

/**
  * helper types associated with time zones
  */
object VitalsTimeZones {

  type VitalsTimeZoneName = String
  type VitalsTimeZoneKey = Short

  private var index = 0

  /**
    * Keep a sorted list of what TZs and their keys as they are available in the current container.
    */
  val keyToTimeZoneMap: Map[VitalsTimeZoneKey, VitalsTimeZoneName] =
    (for (z <- timeZoneNameList.sorted.zipWithIndex) yield { z._2.toShort -> z._1}).toMap

  /**
    * map a string time zone name to our well defined set of TZ keys.
    */
  val timeZoneToKeyMap: Map[VitalsTimeZoneName, VitalsTimeZoneKey] = for (z <- keyToTimeZoneMap) yield z._2 -> z._1

  /**
    * get the TZ key for a given string name
    *
    * @param zone
    * @return
    */
  def keyForZone(zone: VitalsTimeZoneName): Option[VitalsTimeZoneKey] = timeZoneToKeyMap.get(zone)

  /**
    * return some string that represents the same zone as the given key or none
    *
    * @param key
    * @return
    */
  def zoneNameForKey(key: VitalsTimeZoneKey): Option[String] = keyToTimeZoneMap.get(key)

  /**
    * return the Joda time zone for a given time zone key
    *
    * @param key
    * @return
    */
  implicit def zoneForKey(key: VitalsTimeZoneKey): DateTimeZone = DateTimeZone.forID(zoneNameForKey(key).get)

  /**
    * for us SF BA provincial paisans we thing its all about NA west coast.
    */
  final val VitalsDefaultTimeZoneName = "America/Los_Angeles"

  /**
    * shared JODA time zone structure useful for all sorts of things JODA.
    */
  final val BurstDefaultTimeZone: DateTimeZone = DateTimeZone.forID(VitalsDefaultTimeZoneName)


  final val BurstDefaultTimeZoneKey: VitalsTimeZoneKey = keyForZone(VitalsDefaultTimeZoneName).get

}
