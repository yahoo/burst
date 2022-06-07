/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions

import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

/**
 * common methods are those that can be asked of any route at any time.
 * They will always answer wrt the last 'committed' state.
 */
package object common {


  trait FeltRouteCommonFunction extends FeltRouteFunction

  /**
   * function call dispatcher for ''felt route'' state ''common'' methods
   *
   * @return
   */
  private[functions]
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    final override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case FeltRouteCompletePathsFunc.functionName => new FeltRouteCompletePathsFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteLastPathIsCompleteFunc.functionName => new FeltRouteLastPathIsCompleteFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteLastPathOrdinalFunc.functionName => new FeltRouteLastPathOrdinalFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteLastStepKeyFunc.functionName => new FeltRouteLastStepKeyFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteLastStepTagFunc.functionName => new FeltRouteLastStepTagFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteLastStepTimeFunc.functionName => new FeltRouteLastStepTimeFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
    }

  }
}
