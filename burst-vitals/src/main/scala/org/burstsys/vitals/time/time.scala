/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import org.joda.time.format.{DateTimeFormat, PeriodFormatterBuilder}
import org.joda.time.{DateTime, DateTimeZone, Period}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
 * helper types associated with date/time
 */
package object time {

  def nsToSec(ns: Long): Double = ns.toDouble / 1E9.toDouble

  type VitalsMs = Long

  val timeZoneNameList: Array[String] = DateTimeZone.getAvailableIDs.asScala.toList.toArray

  def Now = new DateTime(System.currentTimeMillis())

  implicit def longToJodaTime(ticks: Long): DateTime = new DateTime(ticks)

  def dateTimeToString(dateTime: DateTime): String = {
    DateTimeFormat.forPattern("yyyy/MM/dd hh:mm:ss a z").print(dateTime)
  }

  def dateTimeToString(ms: Long): String = {
    DateTimeFormat.forPattern("yyyy/MM/dd hh:mm:ss a z").print(new DateTime(ms))
  }

  def dateTimeToString: String = {
    DateTimeFormat.forPattern("yyyy/MM/dd hh:mm:ss a z").print(new DateTime(Now))
  }

  def dateTimeToStringNoYear(ms: Long): String = {
    DateTimeFormat.forPattern("hh:mm:ss a z").print(new DateTime(ms))
  }

  def printTimeInFuture(dateTime: DateTime): String = {
    s"${
      new PeriodFormatterBuilder()
        .appendDays().appendSuffix(" day(s) ")
        .appendHours().appendSuffix(" hour(s) ")
        .appendMinutes().appendSuffix(" min(s) ")
        .appendSeconds().appendSuffix(" sec(s) ")
        .toFormatter.print(new Period(new DateTime, dateTime)).trim
    }"
  }

  def printTimeInFuture(ms: Long): String = {
    printTimeInFuture(new DateTime(ms))
  }


  def printTimeInPast(dateTime: DateTime): String = {
    s"${
      new PeriodFormatterBuilder()
        .appendDays().appendSuffix(" day(s) ")
        .appendHours().appendSuffix(" hour(s) ")
        .appendMinutes().appendSuffix(" min(s) ")
        .appendSeconds().appendSuffix(" sec(s) ")
        .toFormatter.print(new Period(dateTime, new DateTime)).trim
    }"
  }

  def printTimeInPast(ms: Long): String = {
    printTimeInPast(new DateTime(ms))
  }


}
