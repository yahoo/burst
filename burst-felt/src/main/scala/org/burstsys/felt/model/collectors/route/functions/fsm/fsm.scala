/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions

import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

package object fsm {

  trait FeltRouteFsmFunction extends FeltRouteFunction

  /**
   * function call dispatcher for ''felt route'' state machine specific methods
   *
   * @return
   */
  private[functions]
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    final override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case FeltRouteFsmStepAssertFunc.functionName => new FeltRouteFsmStepAssertFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteFsmAssertTimeFunc.functionName => new FeltRouteFsmAssertTimeFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteFsmBackFillFunc.functionName => new FeltRouteFsmBackFillFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteFsmEndPathFunc.functionName => new FeltRouteFsmEndPathFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteFsmInPathFunc.functionName => new FeltRouteFsmInPathFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteFsmInStepFunc.functionName => new FeltRouteFsmInStepFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteFsmIsEmptyFunc.functionName => new FeltRouteFsmIsEmptyFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
    }

  }

}
