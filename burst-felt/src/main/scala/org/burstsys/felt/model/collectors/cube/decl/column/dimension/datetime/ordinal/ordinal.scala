/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime

import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltDimSemType

package object ordinal {
  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // ordinal functions
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private[felt] final val SecondOfMinuteOrdinalName = "secondOfMinuteOrdinal"
  private[felt] final val MinuteOfHourOrdinalName = "minuteOfHourOrdinal"
  private[felt] final val HourOfDayOrdinalName = "hourOfDayOrdinal"
  private[felt] final val MinuteOfDayOrdinalName = "minuteOfDayOrdinal"
  private[felt] final val DayOfWeekOrdinalName = "dayOfWeekOrdinal"
  private[felt] final val HourOfMonthOrdinalName = "hourOfMonthOrdinal"
  private[felt] final val DayOfMonthOrdinalName = "dayOfMonthOrdinal"
  private[felt] final val DayOfYearOrdinalName = "dayOfYearOrdinal"
  private[felt] final val MonthOfYearOrdinalName = "monthOfYearOrdinal"
  private[felt] final val WeekOfYearOrdinalName = "weekOfYearOrdinal"
  private[felt] final val YearOfEraOrdinalName = "yearOfEraOrdinal"

  /////////////////////////////////////////////////////////////////////////////////////
  // ordinals
  /////////////////////////////////////////////////////////////////////////////////////

  trait FeltCubeDimTimeOrdinalSemRt extends AnyRef with FeltCubeDimDatetimeSemRt

  abstract
  class FeltCubeDimTimeOrdinalSem extends FeltCubeDimDateTimeSem

  abstract class FeltCubeDimSecOfMinSem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimSecondOfMinuteSemRt = FeltCubeDimSecondOfMinuteSemRt()
  }

  abstract class FeltCubeDimMinOfHrSem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimMinuteOfHourSemRt = FeltCubeDimMinuteOfHourSemRt()
  }

  abstract class FeltCubeDimHrOfDaySem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimHourOfDaySemRt = FeltCubeDimHourOfDaySemRt()
  }

  abstract class FeltCubeDimWkOfYrSem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimWeekOfYearSemRt = FeltCubeDimWeekOfYearSemRt()
  }

  abstract class FeltCubeDimYrOfEraSem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimYearOfEraSemRt = FeltCubeDimYearOfEraSemRt()
  }

  abstract class FeltCubeDimMonthOfYrSem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimMonthOfYearSemRt = FeltCubeDimMonthOfYearSemRt()
  }

  abstract class FeltCubeDimDayOfMonthSem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimDayOfMonthSemRt = FeltCubeDimDayOfMonthSemRt()
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Time Ordinal Semantic
  //////////////////////////////////////////////////////////////////////////////////////////

  abstract class FeltCubeDimDayOfWkSem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimDayOfWeekSemRt = FeltCubeDimDayOfWeekSemRt()
  }

  abstract class FeltCubeDimDayOfYrSem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimDayOfYearSemRt = FeltCubeDimDayOfYearSemRt()
  }

  object DAY_OF_WEEK_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(DayOfWeekOrdinalName)

  object DAY_OF_MONTH_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(DayOfMonthOrdinalName)

  object MONTH_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(MonthOfYearOrdinalName)

  object DAY_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(DayOfYearOrdinalName)

  object WEEK_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(WeekOfYearOrdinalName)

  object HOUR_OF_DAY_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(HourOfDayOrdinalName)

  object MINUTE_OF_HOUR_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(MinuteOfHourOrdinalName)

  object SECOND_OF_MINUTE_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(SecondOfMinuteOrdinalName)

  object YEAR_OF_ERA_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(YearOfEraOrdinalName)

}
