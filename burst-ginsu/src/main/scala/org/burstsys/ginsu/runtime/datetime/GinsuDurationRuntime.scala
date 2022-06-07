/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.runtime.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.ginsu.functions.datetime.GinsuDurationFunctions

/**
 * the runtime extension for [[GinsuDurationFunctions]]
 */
trait GinsuDurationRuntime extends GinsuDurationFunctions {

  implicit def threadRuntime: BrioThreadRuntime

  @inline final def secondDuration(ms: Long): Long = super.secondDuration(ms)(threadRuntime)

  @inline final def minuteDuration(ms: Long): Long = super.minuteDuration(ms)(threadRuntime)

  @inline final def hourDuration(ms: Long): Long = super.hourDuration(ms)(threadRuntime)

  @inline final def dayDuration(ms: Long): Long = super.dayDuration(ms)(threadRuntime)

  @inline final def weekDuration(ms: Long): Long = super.weekDuration(ms)(threadRuntime)

}
