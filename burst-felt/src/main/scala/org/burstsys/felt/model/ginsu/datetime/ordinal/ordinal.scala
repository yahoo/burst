/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu.datetime

import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.ordinal._
import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

import scala.language.postfixOps

package object ordinal {

  trait FeltGinsuDatetimeOrdinalFuncExpr extends FeltGinsuDatetimeFuncExpr

  /**
   * dispatch datetime ordinal FELT function calls
   *
   * @return
   */
  final def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case DAY_OF_MONTH_ORDINAL_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeOrdinalFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-day-of-month-ordinal-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = DAY_OF_MONTH_ORDINAL_DIMENSION_SEMANTIC.name
      }
      case DAY_OF_WEEK_ORDINAL_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeOrdinalFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-day-of-week-ordinal-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = DAY_OF_WEEK_ORDINAL_DIMENSION_SEMANTIC.name
      }
      case DAY_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeOrdinalFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-day-of-year-ordinal-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = DAY_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC.name
      }
      case HOUR_OF_DAY_ORDINAL_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeOrdinalFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-hour-of-day-ordinal-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = HOUR_OF_DAY_ORDINAL_DIMENSION_SEMANTIC.name
      }
      case SECOND_OF_MINUTE_ORDINAL_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeOrdinalFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-second-of-minute-ordinal-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = SECOND_OF_MINUTE_ORDINAL_DIMENSION_SEMANTIC.name
      }
      case MINUTE_OF_HOUR_ORDINAL_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeOrdinalFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-minute-of-hour-ordinal-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = MINUTE_OF_HOUR_ORDINAL_DIMENSION_SEMANTIC.name
      }
      case MONTH_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeOrdinalFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-month-of-year-ordinal-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = MONTH_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC.name
      }
      case WEEK_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeOrdinalFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-week-of-year-ordinal-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = WEEK_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC.name
      }
      case YEAR_OF_ERA_ORDINAL_DIMENSION_SEMANTIC.id => new FeltGinsuDatetimeOrdinalFuncExpr {
        global = functionLocation.global
        final override val nodeName = "ginsu-year-of-era-ordinal-call"
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = YEAR_OF_ERA_ORDINAL_DIMENSION_SEMANTIC.name
      }
    }
  }

}
