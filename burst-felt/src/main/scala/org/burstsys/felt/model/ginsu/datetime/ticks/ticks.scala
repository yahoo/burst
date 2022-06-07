/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu.datetime

import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

import scala.language.postfixOps

package object ticks {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // tick functions
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  final val SecondTicksName = "secondTicks"
  final val MinuteTicksName = "minuteTicks"
  final val HourTicksName = "hourTicks"
  final val DayTicksName = "dayTicks"
  final val WeekTicksName = "weekTicks"

  final def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case SecondTicksName => new FeltGinsuTicksFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-second-ticks-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = SecondTicksName
      }
      case MinuteTicksName => new FeltGinsuTicksFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-minute-ticks-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = MinuteTicksName
      }
      case HourTicksName => new FeltGinsuTicksFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-hour-ticks-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = HourTicksName
      }
      case DayTicksName => new FeltGinsuTicksFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-day-ticks-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = DayTicksName
      }
      case WeekTicksName => new FeltGinsuTicksFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-week-ticks-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = WeekTicksName
      }
    }
  }


}
