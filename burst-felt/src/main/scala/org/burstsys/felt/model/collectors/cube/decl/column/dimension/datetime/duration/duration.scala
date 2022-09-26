/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime

import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltDimSemType

package object duration {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // duration functions
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private[felt] final val SecondDurationName = "secondDuration"
  private[felt] final val MinuteDurationName = "minuteDuration"
  private[felt] final val HourDurationName = "hourDuration"
  private[felt] final val DayDurationName = "dayDuration"
  private[felt] final val WeekDurationName = "weekDuration"

  /////////////////////////////////////////////////////////////////////////////////////
  // durations
  /////////////////////////////////////////////////////////////////////////////////////

  sealed abstract
  class FeltCubeDimDurationSem extends FeltCubeDimDateTimeSem

  abstract class FeltCubeDimSecondDurSem extends FeltCubeDimDurationSem {
    final override val semanticRt: FeltCubeDimSecondDurSemRt = FeltCubeDimSecondDurSemRt()
  }

  abstract class FeltCubeDimMinuteDurSem extends FeltCubeDimDurationSem {
    final override val semanticRt: FeltCubeDimMinuteDurSemRt = FeltCubeDimMinuteDurSemRt()
  }

  abstract class FeltCubeDimHourDurSem extends FeltCubeDimDurationSem {
    final override val semanticRt: FeltCubeDimHourDurSemRt = FeltCubeDimHourDurSemRt()
  }

  abstract class FeltCubeDimDayDurSem extends FeltCubeDimDurationSem {
    final override val semanticRt: FeltCubeDimDayDurSemRt = FeltCubeDimDayDurSemRt()
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Time Grain Semantic Runtime
  //////////////////////////////////////////////////////////////////////////////////////////

  abstract class FeltCubeDimWeekDurSem extends FeltCubeDimDurationSem {
    final override val semanticRt: FeltCubeDimWeekDurSemRt = FeltCubeDimWeekDurSemRt()
  }

  object WEEK_DURATION_DIMENSION_SEMANTIC extends FeltDimSemType(WeekDurationName)

  object DAY_DURATION_DIMENSION_SEMANTIC extends FeltDimSemType(DayDurationName)

  object HOUR_DURATION_DIMENSION_SEMANTIC extends FeltDimSemType(HourDurationName)

  object MINUTE_DURATION_DIMENSION_SEMANTIC extends FeltDimSemType(MinuteDurationName)

  object SECOND_DURATION_DIMENSION_SEMANTIC extends FeltDimSemType(SecondDurationName)
}
