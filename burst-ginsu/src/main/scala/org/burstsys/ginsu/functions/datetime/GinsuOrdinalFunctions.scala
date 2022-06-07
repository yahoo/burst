/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.functions.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.vitals.errors._
import org.joda.time.{IllegalFieldValueException, IllegalInstantException}

/**
 * given an epoch time, return the ordinal of one calendar unit in the context of another calendar unit
 *
 */
trait GinsuOrdinalFunctions extends Any {

  /**
   * given an epoch time, return the second of the associated minute
   *
   * @param epoch
   * @return
   */
  @inline final
  def secondOfMinuteOrdinal(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)
      time.getSecondOfMinute
    } catch safely {
      case iffe: IllegalFieldValueException =>
        threadRuntime error iffe
        0L
      case iie: IllegalInstantException =>
        threadRuntime error iie
        0L
    }
  }

  /**
   * given an epoch time, return the minute of the associated hour
   *
   * @param epoch
   * @return
   */
  @inline final
  def minuteOfHourOrdinal(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)
      time.getMinuteOfHour
    } catch safely {
      case iffe: IllegalFieldValueException =>
        threadRuntime error iffe
        0L
      case iie: IllegalInstantException =>
        threadRuntime error iie
        0L
    }
  }

  /**
   * given an epoch time, return the hour of the associated day (0-23)
   *
   * @param epoch
   * @return
   */
  @inline final
  def hourOfDayOrdinal(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)
      time.getHourOfDay
    } catch safely {
      case iffe: IllegalFieldValueException =>
        threadRuntime error iffe
        0L
      case iie: IllegalInstantException =>
        threadRuntime error iie
        0L
    }
  }

  /**
   * given an epoch time, return the associated day of week 1-7 for monday-sunday
   *
   * @param epoch
   * @return
   */
  @inline final
  def dayOfWeekOrdinal(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)
      time.getDayOfWeek
    } catch safely {
      case iffe: IllegalFieldValueException =>
        threadRuntime error iffe
        0L
      case iie: IllegalInstantException =>
        threadRuntime error iie
        0L
    }
  }

  /**
   * given an epoch time, return the associated day of month
   *
   * @param epoch
   * @return
   */
  @inline final
  def dayOfMonthOrdinal(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)
      time.getDayOfMonth
    } catch safely {
      case iffe: IllegalFieldValueException =>
        threadRuntime error iffe
        0L
      case iie: IllegalInstantException =>
        threadRuntime error iie
        0L
    }
  }

  /**
   * given an epoch time, return the associated data of the year e.g. 1-365
   *
   * @param epoch
   * @return
   */
  @inline final
  def dayOfYearOrdinal(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)
      time.getDayOfYear
    } catch safely {
      case iffe: IllegalFieldValueException =>
        threadRuntime error iffe
        0L
      case iie: IllegalInstantException =>
        threadRuntime error iie
        0L
    }
  }

  /**
   * given an epoch time, return the ordinal of the week of the associated year
   * <p/>
   * '''Note:''' n the standard ISO8601 week algorithm, the first week of the year is that in which at least 4 days
   * are in the year. As a result of this definition, day 1 of the first week may be in the previous year.
   * The weekyear allows you to query the effective year for that day.
   *
   * @param epoch
   * @return
   */
  @inline final
  def weekOfYearOrdinal(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)
      time.getWeekOfWeekyear
    } catch safely {
      case iffe: IllegalFieldValueException =>
        threadRuntime error iffe
        0L
      case iie: IllegalInstantException =>
        threadRuntime error iie
        0L
    }
  }

  /**
   * given an epoch time, return the associated month of the year e.g. 1-12
   *
   * @param epoch
   * @return
   */
  @inline final
  def monthOfYearOrdinal(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)
      time.getMonthOfYear
    } catch safely {
      case iffe: IllegalFieldValueException =>
        threadRuntime error iffe
        0L
      case iie: IllegalInstantException =>
        threadRuntime error iie
        0L
    }
  }

  /**
   * given an epoch time, return the ordinal year A.D. e.g. 2018, 2020
   *
   * @param epoch
   * @return
   */
  @inline final
  def yearOfEraOrdinal(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)
      time.getYear
    } catch safely {
      case iffe: IllegalFieldValueException =>
        threadRuntime error iffe
        0L
      case iie: IllegalInstantException =>
        threadRuntime error iie
        0L
    }
  }

}
