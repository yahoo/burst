/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu.datetime

import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.grain._
import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation


package object grain {

  trait FeltGinsuDatetimeGrainFuncExpr extends FeltGinsuDatetimeFuncExpr

  /**
   * dispatch datetime grain FELT function calls
   *
   * @return
   */
  final def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case YEAR_GRAIN_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeGrainFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-year-grain-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = YEAR_GRAIN_DIMENSION_SEMANTIC.name
      }
      case HALF_GRAIN_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeGrainFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-half-grain-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = HALF_GRAIN_DIMENSION_SEMANTIC.name
      }
      case QUARTER_GRAIN_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeGrainFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-quarter-grain-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = QUARTER_GRAIN_DIMENSION_SEMANTIC.name
      }
      case MONTH_GRAIN_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeGrainFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-month-grain-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = MONTH_GRAIN_DIMENSION_SEMANTIC.name
      }
      case WEEK_GRAIN_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeGrainFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-week-grain-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = WEEK_GRAIN_DIMENSION_SEMANTIC.name
      }
      case DAY_GRAIN_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeGrainFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-day-grain-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = DAY_GRAIN_DIMENSION_SEMANTIC.name
      }
      case HOUR_GRAIN_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeGrainFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-hour-grain-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = HOUR_GRAIN_DIMENSION_SEMANTIC.name
      }
      case MINUTE_GRAIN_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeGrainFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-minute-grain-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = MINUTE_GRAIN_DIMENSION_SEMANTIC.name
      }
      case SECOND_GRAIN_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeGrainFuncExpr {
        final override val nodeName = "ginsu-second-grain-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = SECOND_GRAIN_DIMENSION_SEMANTIC.name
      }
    }
  }


}
