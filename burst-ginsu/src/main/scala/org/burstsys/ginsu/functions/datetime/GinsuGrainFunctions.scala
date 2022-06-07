/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.functions.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.vitals.errors._
import org.joda.time.{IllegalFieldValueException, IllegalInstantException}

/**
 * given an epoch time, return a new epoch time truncated to a specific calendar granularity.
 * This is used for ''bucketing'' incoming epoch times to calendar groupings
 *
 */
trait GinsuGrainFunctions extends Any {

  /**
   * given an epoch time, return a new epoch time truncated to a calendar second
   *
   * @param epoch
   * @return
   */
  @inline final def secondGrain(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)

      time.setMillisOfSecond(0)
      time.getMillis
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
   * given an epoch time, return a new epoch time truncated to a calendar minute
   *
   * @param epoch
   * @return
   */
  @inline final def minuteGrain(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)

      time.setSecondOfMinute(0)
      time.setMillisOfSecond(0)
      time.getMillis
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
   * given an epoch time, return a new epoch time truncated to a calendar hour
   *
   * @param epoch
   * @return
   */
  @inline final def hourGrain(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)

      time.setMinuteOfHour(0)
      time.setSecondOfMinute(0)
      time.setMillisOfSecond(0)
      time.getMillis
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
   * given an epoch time, return a new epoch time truncated to a calendar day
   *
   * @param epoch
   * @return
   */
  @inline final def dayGrain(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)

      time.setHourOfDay(0)
      time.setMinuteOfDay(0)
      time.setSecondOfDay(0)
      time.setMillisOfDay(0)
      time.setSecondOfMinute(0)
      time.setMillisOfSecond(0)
      time.getMillis
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
   * given an epoch time, return a new epoch time truncated to a calendar month
   *
   * @param epoch
   * @return
   */
  @inline final def monthGrain(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)

      time.setDayOfMonth(1)
      time.setHourOfDay(0)
      time.setMinuteOfDay(0)
      time.setSecondOfMinute(0)
      time.setMillisOfSecond(0)
      time.getMillis
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
   * given an epoch time, return a new epoch time truncated to a calendar week
   *
   * @param epoch
   * @return
   */
  @inline final def weekGrain(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)

      time.setDayOfWeek(1)
      time.setHourOfDay(0)
      time.setMinuteOfDay(0)
      time.setSecondOfDay(0)
      time.setMillisOfDay(0)
      time.setSecondOfMinute(0)
      time.setMillisOfSecond(0)
      time.getMillis
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
   * given an epoch time, return a new epoch time truncated to a calendar quarter
   *
   * @param epoch
   * @return
   */
  @inline final def quarterGrain(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)

      val moy = time.getMonthOfYear
      if (Q1.contains(moy)) {
        // TODO rewrite
        time.setMonthOfYear(1)
      } else {
        if (Q2.contains(moy)) {
          // TODO rewrite
          time.setMonthOfYear(4)
        } else {
          if (Q3.contains(moy)) {
            // TODO rewrite
            time.setMonthOfYear(7)
          } else {
            if (Q4.contains(moy)) {
              // TODO rewrite
              time.setMonthOfYear(10)
            }
          }
        }
      }
      time.setDayOfMonth(1)
      time.setHourOfDay(0)
      time.setMinuteOfDay(0)
      time.setSecondOfMinute(0)
      time.setMillisOfSecond(0)
      time.getMillis
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
   * given an epoch time, return a new epoch time truncated to a calendar half
   *
   * @param epoch
   * @return
   */
  @inline final def halfGrain(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)

      val moy = time.getMonthOfYear
      if (H1.contains(moy)) {
        // TODO rewrite
        time.setMonthOfYear(1)
      } else {
        if (H2.contains(moy)) {
          // TODO rewrite
          time.setMonthOfYear(7)
        }
      }
      time.setDayOfMonth(1)
      time.setHourOfDay(0)
      time.setMinuteOfDay(0)
      time.setSecondOfMinute(0)
      time.setMillisOfSecond(0)
      time.getMillis
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
   * given an epoch time, return a new epoch time truncated to a calendar year
   *
   * @param epoch
   * @return
   */
  @inline final def yearGrain(epoch: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    try {
      val time = threadRuntime.time
      time.setMillis(epoch)

      time.setDayOfYear(1)
      time.setHourOfDay(0)
      time.setMinuteOfDay(0)
      time.setSecondOfMinute(0)
      time.setMillisOfSecond(0)
      time.getMillis
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
