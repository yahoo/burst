/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu.datetime

import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.duration._
import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

import scala.language.postfixOps


package object duration {

  trait FeltGinsuDurationFuncExpr extends FeltGinsuDatetimeFuncExpr

  /**
   * dispatch datetime duration FELT function calls
   *
   * @return
   */
  final def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {

      case SECOND_DURATION_DIMENSION_SEMANTIC.id => new FeltGinsuDurationFuncExpr {

        final override val nodeName = "ginsu-second-duration-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = SECOND_DURATION_DIMENSION_SEMANTIC.name
      }
      case MINUTE_DURATION_DIMENSION_SEMANTIC.id => new FeltGinsuDurationFuncExpr {
        final override val nodeName = "ginsu-minute-duration-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = MINUTE_DURATION_DIMENSION_SEMANTIC.name
      }
      case HOUR_DURATION_DIMENSION_SEMANTIC.id => new FeltGinsuDurationFuncExpr {
        final override val nodeName = "ginsu-hour-duration-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = HOUR_DURATION_DIMENSION_SEMANTIC.name
      }
      case DAY_DURATION_DIMENSION_SEMANTIC.id => new FeltGinsuDurationFuncExpr {
        final override val nodeName = "ginsu-day-duration-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = DAY_DURATION_DIMENSION_SEMANTIC.name
      }
      case WEEK_DURATION_DIMENSION_SEMANTIC.id => new FeltGinsuDurationFuncExpr {
        final override val nodeName = "ginsu-week-duration-call"
        final override val location: FeltLocation = functionLocation
        final val functionName: String = WEEK_DURATION_DIMENSION_SEMANTIC.name
      }

    }

  }


}
