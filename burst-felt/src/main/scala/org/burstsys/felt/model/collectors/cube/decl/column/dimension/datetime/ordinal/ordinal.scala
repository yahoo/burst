/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
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

  object DAY_OF_WEEK_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(DayOfWeekOrdinalName)

  object DAY_OF_MONTH_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(DayOfMonthOrdinalName)

  object MONTH_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(MonthOfYearOrdinalName)

  object DAY_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(DayOfYearOrdinalName)

  object WEEK_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(WeekOfYearOrdinalName)

  object HOUR_OF_DAY_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(HourOfDayOrdinalName)

  object MINUTE_OF_HOUR_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(MinuteOfHourOrdinalName)

  object SECOND_OF_MINUTE_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(SecondOfMinuteOrdinalName)

  object YEAR_OF_ERA_ORDINAL_DIMENSION_SEMANTIC extends FeltDimSemType(YearOfEraOrdinalName)

  //////////////////////////////////////////////////////////////////////////////////////////
  // Time Ordinal Semantic
  //////////////////////////////////////////////////////////////////////////////////////////

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

  abstract class FeltCubeDimDayOfWkSem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimDayOfWeekSemRt = FeltCubeDimDayOfWeekSemRt()
  }

  abstract class FeltCubeDimDayOfYrSem extends FeltCubeDimTimeOrdinalSem {
    final override val semanticRt: FeltCubeDimDayOfYearSemRt = FeltCubeDimDayOfYearSemRt()
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Time Ordinal Semantic Runtime
  //////////////////////////////////////////////////////////////////////////////////////////

  trait FeltCubeDimTimeOrdinalSemRt extends AnyRef with FeltCubeDimDatetimeSemRt

  final case
  class FeltCubeDimWeekOfYearSemRt() extends FeltCubeDimTimeOrdinalSemRt {
    semanticType = WEEK_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = weekOfYearOrdinal(time)
  }

  final case
  class FeltCubeDimSecondOfMinuteSemRt() extends FeltCubeDimTimeOrdinalSemRt {
    semanticType = SECOND_OF_MINUTE_ORDINAL_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = secondOfMinuteOrdinal(time)
  }

  final case
  class FeltCubeDimMinuteOfHourSemRt() extends FeltCubeDimTimeOrdinalSemRt {
    semanticType = MINUTE_OF_HOUR_ORDINAL_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = minuteOfHourOrdinal(time)
  }

  final case
  class FeltCubeDimYearOfEraSemRt() extends FeltCubeDimTimeOrdinalSemRt {
    semanticType = YEAR_OF_ERA_ORDINAL_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = yearOfEraOrdinal(time)
  }

  final case
  class FeltCubeDimDayOfWeekSemRt() extends FeltCubeDimTimeOrdinalSemRt {
    semanticType = DAY_OF_WEEK_ORDINAL_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = dayOfWeekOrdinal(time)
  }

  final case
  class FeltCubeDimHourOfDaySemRt() extends FeltCubeDimTimeOrdinalSemRt {
    semanticType = HOUR_OF_DAY_ORDINAL_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = hourOfDayOrdinal(time)
  }

  final case
  class FeltCubeDimDayOfMonthSemRt() extends FeltCubeDimTimeOrdinalSemRt {
    semanticType = DAY_OF_MONTH_ORDINAL_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = dayOfMonthOrdinal(time)
  }

  final case
  class FeltCubeDimMonthOfYearSemRt() extends FeltCubeDimTimeOrdinalSemRt {
    semanticType = MONTH_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = monthOfYearOrdinal(time)
  }

  final case
  class FeltCubeDimDayOfYearSemRt() extends FeltCubeDimTimeOrdinalSemRt {
    semanticType = DAY_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = dayOfYearOrdinal(time)
  }

}
