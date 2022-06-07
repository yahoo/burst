/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.runtime.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.ginsu.functions.datetime.GinsuTickFunctions

/**
 * the runtime extension for [[GinsuTickFunctions]]
 */
trait GinsuTickRuntime extends GinsuTickFunctions {

  implicit def threadRuntime: BrioThreadRuntime

  @inline final def secondTicks(seconds: Long): Long = super.secondTicks(seconds)(threadRuntime)

  @inline final def minuteTicks(minutes: Long): Long = super.minuteTicks(minutes)(threadRuntime)

  @inline final def hourTicks(hours: Long): Long = super.hourTicks(hours)(threadRuntime)

  @inline final def dayTicks(days: Long): Long = super.dayTicks(days)(threadRuntime)

  @inline final def weekTicks(weeks: Long): Long = super.weekTicks(weeks)(threadRuntime)

}
