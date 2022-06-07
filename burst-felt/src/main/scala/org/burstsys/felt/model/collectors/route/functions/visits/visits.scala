/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions

import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

package object visits {

  trait FeltRouteVisitFunction extends FeltRouteFunction

  private[functions]
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    final override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case FeltRouteVisitPathIsCompleteFunc.functionName => new FeltRouteVisitPathIsCompleteFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteVisitPathIsFirstFunc.functionName => new FeltRouteVisitPathIsFirstFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteVisitPathIsLastFunc.functionName => new FeltRouteVisitPathIsLastFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteVisitPathOrdinalFunc.functionName => new FeltRouteVisitPathOrdinalFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteVisitStepIsFirstFunc.functionName => new FeltRouteVisitStepIsFirstFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteVisitStepIsLastFunc.functionName => new FeltRouteVisitStepIsLastFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteVisitStepIsLastInPathFunc.functionName => new FeltRouteVisitStepIsLastInPathFunc  {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteVisitStepOrdinalFunc.functionName => new FeltRouteVisitStepOrdinalFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteVisitStepKeyFunc.functionName => new FeltRouteVisitStepKeyFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteVisitStepTagFunc.functionName => new FeltRouteVisitStepTagFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteVisitStepTimeFunc.functionName => new FeltRouteVisitStepTimeFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
    }

  }

}
