/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.runtime.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.ginsu.functions.datetime.GinsuOrdinalFunctions

/**
 * the runtime extension for [[GinsuOrdinalFunctions]]
 */
trait GinsuOrdinalRuntime extends GinsuOrdinalFunctions {

  implicit def threadRuntime: BrioThreadRuntime

  @inline final
  def secondOfMinuteOrdinal(epoch: Long): Long = super.secondOfMinuteOrdinal(epoch)(threadRuntime)

  @inline final
  def minuteOfHourOrdinal(epoch: Long): Long = super.minuteOfHourOrdinal(epoch)(threadRuntime)

  @inline final
  def hourOfDayOrdinal(epoch: Long): Long = super.hourOfDayOrdinal(epoch)(threadRuntime)

  @inline final
  def dayOfWeekOrdinal(epoch: Long): Long = super.dayOfWeekOrdinal(epoch)(threadRuntime)

  @inline final
  def dayOfMonthOrdinal(epoch: Long): Long = super.dayOfMonthOrdinal(epoch)(threadRuntime)

  @inline final
  def dayOfYearOrdinal(epoch: Long): Long = super.dayOfYearOrdinal(epoch)(threadRuntime)

  @inline final
  def weekOfYearOrdinal(epoch: Long): Long = super.weekOfYearOrdinal(epoch)(threadRuntime)

  @inline final
  def monthOfYearOrdinal(epoch: Long): Long = super.monthOfYearOrdinal(epoch)(threadRuntime)

  @inline final
  def yearOfEraOrdinal(epoch: Long): Long = super.yearOfEraOrdinal(epoch)(threadRuntime)

}
