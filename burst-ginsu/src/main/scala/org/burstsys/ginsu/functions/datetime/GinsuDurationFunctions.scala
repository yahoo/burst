/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.functions.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime

/**
 * given an epoch time, return a new epoch time truncated to a specific time unit granularity.
 * This is used for ''bucketing'' incoming epoch times to time unit groupings e.g. by hour, by day
 */
trait GinsuDurationFunctions extends Any {

  /**
   * given an epoch time, return a new epoch time truncated to a ''second'' granularity
   *
   * @param ms
   * @return
   */
  @inline final def secondDuration(ms: Long)(implicit threadRuntime: BrioThreadRuntime): Long = ms / 1E3.toLong

  /**
   * given an epoch time, return a new epoch time truncated to a ''minute'' granularity
   *
   * @param ms
   * @return
   */
  @inline final def minuteDuration(ms: Long)(implicit threadRuntime: BrioThreadRuntime): Long = ms / 1E3.toLong / 60

  /**
   * given an epoch time, return a new epoch time truncated to a ''hour'' granularity
   *
   * @param ms
   * @return
   */
  @inline final def hourDuration(ms: Long)(implicit threadRuntime: BrioThreadRuntime): Long = ms / 1E3.toLong / 60 / 60

  /**
   * given an epoch time, return a new epoch time truncated to a ''day'' granularity
   *
   * @param ms
   * @return
   */
  @inline final def dayDuration(ms: Long)(implicit threadRuntime: BrioThreadRuntime): Long = ms / 1E3.toLong / 60 / 60 / 24

  /**
   * given an epoch time, return a new epoch time truncated to a ''week'' granularity
   *
   * @param ms
   * @return
   */
  @inline final def weekDuration(ms: Long)(implicit threadRuntime: BrioThreadRuntime): Long = ms / 1E3.toLong / 60 / 60 / 24 / 7

}
