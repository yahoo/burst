/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.runtime.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.ginsu.functions.datetime.GinsuGrainFunctions

/**
 * the runtime extension for [[GinsuGrainFunctions]]
 */
trait GinsuGrainRuntime extends GinsuGrainFunctions {

  implicit def threadRuntime: BrioThreadRuntime

  @inline final def secondGrain(epoch: Long): Long = super.secondGrain(epoch)(threadRuntime)

  @inline final def minuteGrain(epoch: Long): Long = super.minuteGrain(epoch)(threadRuntime)

  @inline final def hourGrain(epoch: Long): Long = super.hourGrain(epoch)(threadRuntime)

  @inline final def monthGrain(epoch: Long): Long = super.monthGrain(epoch)(threadRuntime)

  @inline final def dayGrain(epoch: Long): Long = super.dayGrain(epoch)(threadRuntime)

  @inline final def weekGrain(epoch: Long): Long = super.weekGrain(epoch)(threadRuntime)

  @inline final def quarterGrain(epoch: Long): Long = super.quarterGrain(epoch)(threadRuntime)

  @inline final def halfGrain(epoch: Long): Long = super.halfGrain(epoch)(threadRuntime)

  @inline final def yearGrain(epoch: Long): Long = super.yearGrain(epoch)(threadRuntime)

}
