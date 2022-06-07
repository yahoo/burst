/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.functions.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime

/**
 * given an quantity of a given time or calendar quantity, return the number of
 * equivalent ms '''ticks'''
 *
 */
trait GinsuTickFunctions extends Any {

  /**
   * return the '''ms''' ticks for a given quantity of ''seconds''
   *
   * @param seconds
   * @return
   */
  @inline final def secondTicks(seconds: Long)(implicit threadRuntime: BrioThreadRuntime): Long = seconds * 1E3.toLong

  /**
   * return the '''ms''' ticks for a given quantity of ''minutes''
   *
   * @param minutes
   * @return
   */
  @inline final def minuteTicks(minutes: Long)(implicit threadRuntime: BrioThreadRuntime): Long = minutes * 1E3.toLong * 60

  /**
   * return the '''ms''' ticks for a given quantity of ''hours''
   *
   * @param hours
   * @return
   */
  @inline final def hourTicks(hours: Long)(implicit threadRuntime: BrioThreadRuntime): Long = hours * 1E3.toLong * 60 * 60

  /**
   * return the '''ms''' ticks for a given quantity of ''days''
   *
   * @param days
   * @return
   */
  @inline final def dayTicks(days: Long)(implicit threadRuntime: BrioThreadRuntime): Long = days * 1E3.toLong * 60 * 60 * 24

  /**
   * return the '''ms''' ticks for a given quantity of ''weeks''
   *
   * @param weeks
   * @return
   */
  @inline final def weekTicks(weeks: Long)(implicit threadRuntime: BrioThreadRuntime): Long = weeks * 1E3.toLong * 60 * 60 * 24 * 7
}
