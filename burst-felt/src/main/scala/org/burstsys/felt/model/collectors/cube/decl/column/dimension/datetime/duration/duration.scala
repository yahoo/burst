/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.{FeltCubeDimSemRt, FeltDimSemType}
import org.burstsys.ginsu.functions.datetime.GinsuDurationFunctions
import org.burstsys.vitals.kryo.VitalsKryoStatelessSerializable

package object duration {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // duration functions
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private[felt] final val SecondDurationName = "secondDuration"
  private[felt] final val MinuteDurationName = "minuteDuration"
  private[felt] final val HourDurationName = "hourDuration"
  private[felt] final val DayDurationName = "dayDuration"
  private[felt] final val weekDurationName = "weekDuration"

  /////////////////////////////////////////////////////////////////////////////////////
  // durations
  /////////////////////////////////////////////////////////////////////////////////////

  object WEEK_DURATION_DIMENSION_SEMANTIC extends FeltDimSemType(weekDurationName)

  object DAY_DURATION_DIMENSION_SEMANTIC extends FeltDimSemType(DayDurationName)

  object HOUR_DURATION_DIMENSION_SEMANTIC extends FeltDimSemType(HourDurationName)

  object MINUTE_DURATION_DIMENSION_SEMANTIC extends FeltDimSemType(MinuteDurationName)

  object SECOND_DURATION_DIMENSION_SEMANTIC extends FeltDimSemType(SecondDurationName)

  //////////////////////////////////////////////////////////////////////////////////////////
  // Time Grain Semantic Runtime
  //////////////////////////////////////////////////////////////////////////////////////////

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

  abstract class FeltCubeDimWeekDurSem extends FeltCubeDimDurationSem {
    final override val semanticRt: FeltCubeDimWeekDurSemRt = FeltCubeDimWeekDurSemRt()
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Time Grain Semantic Runtime
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Slice time into partitions - NOTE this does not return new numbers but new UTC dates (longs/ticks)
   * that are of a smaller sampling
   */
  sealed
  trait FeltCubeDimDurationSemRt extends AnyRef
    with FeltCubeDimSemRt with VitalsKryoStatelessSerializable with GinsuDurationFunctions {
    override protected val dimensionHandlesStrings: Boolean = false

    @inline def duration(time: Long)(implicit threadRuntime: BrioThreadRuntime): Long

    @inline final override
    def doLong(value: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = duration(value)
  }

  final case class FeltCubeDimWeekDurSemRt() extends FeltCubeDimDurationSemRt {
    semanticType = WEEK_DURATION_DIMENSION_SEMANTIC

    @inline def duration(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      weekDuration(time)
  }

  final case class FeltCubeDimDayDurSemRt() extends FeltCubeDimDurationSemRt {
    semanticType = DAY_DURATION_DIMENSION_SEMANTIC

    @inline override def duration(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      dayDuration(time)
  }

  final case class FeltCubeDimHourDurSemRt() extends FeltCubeDimDurationSemRt {
    semanticType = HOUR_DURATION_DIMENSION_SEMANTIC

    @inline override def duration(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      hourDuration(time)
  }

  final case class FeltCubeDimMinuteDurSemRt() extends FeltCubeDimDurationSemRt {
    semanticType = MINUTE_DURATION_DIMENSION_SEMANTIC

    @inline override def duration(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      minuteDuration(time)
  }

  final case class FeltCubeDimSecondDurSemRt() extends FeltCubeDimDurationSemRt {
    semanticType = SECOND_DURATION_DIMENSION_SEMANTIC

    @inline override def duration(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
      secondDuration(time)
  }

}
