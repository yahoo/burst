/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltDimSemType

package object grain {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // grain functions
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private[felt] final val SecondGrainName = "secondGrain"
  private[felt] final val MinuteGrainName = "minuteGrain"
  private[felt] final val HourGrainName = "hourGrain"
  private[felt] final val DayGrainName = "dayGrain"
  private[felt] final val WeekGrainName = "weekGrain"
  private[felt] final val MonthGrainName = "monthGrain"
  private[felt] final val QuarterGrainName = "quarterGrain"
  private[felt] final val HalfGrainName = "halfGrain"
  private[felt] final val YearGrainName = "yearGrain"

  /////////////////////////////////////////////////////////////////////////////////////
  // grains
  /////////////////////////////////////////////////////////////////////////////////////

  object QUARTER_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(QuarterGrainName)

  object HALF_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(HalfGrainName)

  object MONTH_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(MonthGrainName)

  object DAY_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(DayGrainName)

  object WEEK_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(WeekGrainName)

  object HOUR_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(HourGrainName)

  object MINUTE_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(MinuteGrainName)

  object SECOND_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(SecondGrainName)

  object YEAR_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(YearGrainName)


  //////////////////////////////////////////////////////////////////////////////////////////
  // Time Grain Semantic
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
   * all date time related dimensions
   */
  sealed abstract
  class FeltCubeDimTimeGrainSem extends FeltCubeDimDateTimeSem

  abstract class FeltCubeDimSecondGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimSecondGrainSemRt = FeltCubeDimSecondGrainSemRt()
  }

  abstract class FeltCubeDimMinuteGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimMinuteGrainSemRt = FeltCubeDimMinuteGrainSemRt()
  }

  abstract class FeltCubeDimHourGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimHourGrainSemRt = FeltCubeDimHourGrainSemRt()
  }

  abstract class FeltCubeDimDayGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimDayGrainSemRt = FeltCubeDimDayGrainSemRt()
  }

  abstract class FeltCubeDimWeekGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimWeekGrainSemRt = FeltCubeDimWeekGrainSemRt()
  }

  abstract class FeltCubeDimMonthGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimMonthGrainSemRt = FeltCubeDimMonthGrainSemRt()
  }

  abstract class FeltCubeDimQuarterGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimQuarterGrainSemRt = FeltCubeDimQuarterGrainSemRt()
  }

  abstract class FeltCubeDimHalfGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimHalfGrainSemRt = FeltCubeDimHalfGrainSemRt()
  }

  abstract class FeltCubeDimYearGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimYearGrainSemRt = FeltCubeDimYearGrainSemRt()
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Time Grain Semantic Runtime
  //////////////////////////////////////////////////////////////////////////////////////////

  sealed trait FeltCubeDimTimeGrainSemRt extends FeltCubeDimDatetimeSemRt

  final case class FeltCubeDimSecondGrainSemRt() extends FeltCubeDimTimeGrainSemRt {
    semanticType = SECOND_GRAIN_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      secondGrain(time)
  }

  final case class FeltCubeDimMinuteGrainSemRt() extends FeltCubeDimTimeGrainSemRt {
    semanticType = MINUTE_GRAIN_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      minuteGrain(time)
  }

  final case class FeltCubeDimHourGrainSemRt() extends FeltCubeDimTimeGrainSemRt {
    semanticType = HOUR_GRAIN_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      hourGrain(time)
  }

  final case class FeltCubeDimDayGrainSemRt() extends FeltCubeDimTimeGrainSemRt {
    semanticType = DAY_GRAIN_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      dayGrain(time)
  }

  final case class FeltCubeDimWeekGrainSemRt() extends FeltCubeDimTimeGrainSemRt {
    semanticType = WEEK_GRAIN_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      weekGrain(time)
  }

  final case class FeltCubeDimMonthGrainSemRt() extends FeltCubeDimTimeGrainSemRt {
    semanticType = MONTH_GRAIN_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      monthGrain(time)
  }

  final case class FeltCubeDimQuarterGrainSemRt() extends FeltCubeDimTimeGrainSemRt {
    semanticType = QUARTER_GRAIN_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      quarterGrain(time)
  }

  final case class FeltCubeDimHalfGrainSemRt() extends FeltCubeDimTimeGrainSemRt {
    semanticType = HALF_GRAIN_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      halfGrain(time)
  }

  final case class FeltCubeDimYearGrainSemRt() extends FeltCubeDimTimeGrainSemRt {
    semanticType = YEAR_GRAIN_DIMENSION_SEMANTIC

    @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      yearGrain(time)
  }

}
