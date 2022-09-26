/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime

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

  trait FeltCubeDimTimeGrainSemRt extends FeltCubeDimDatetimeSemRt

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


  //////////////////////////////////////////////////////////////////////////////////////////
  // Time Grain Semantic
  //////////////////////////////////////////////////////////////////////////////////////////

  abstract class FeltCubeDimHalfGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimHalfGrainSemRt = FeltCubeDimHalfGrainSemRt()
  }

  abstract class FeltCubeDimYearGrainSem extends FeltCubeDimTimeGrainSem {
    final override val semanticRt: FeltCubeDimYearGrainSemRt = FeltCubeDimYearGrainSemRt()
  }

  object QUARTER_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(QuarterGrainName)

  object HALF_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(HalfGrainName)

  object MONTH_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(MonthGrainName)

  object DAY_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(DayGrainName)

  object WEEK_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(WeekGrainName)

  object HOUR_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(HourGrainName)

  object MINUTE_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(MinuteGrainName)

  object SECOND_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(SecondGrainName)

  object YEAR_GRAIN_DIMENSION_SEMANTIC extends FeltDimSemType(YearGrainName)
}
