/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet

import org.burstsys.felt.model.collectors.route.functions.FeltRouteFunction
import org.burstsys.felt.model.expressions.function.{FeltFuncExpr, FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

package object functions {

  trait FeltTabletFunction extends FeltFuncExpr

  /**
   * function call dispatcher for ''felt'' tablet methods
   *
   * @return
   */
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {
    final override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case FeltTabletMemberAddFunc.functionName => new FeltTabletMemberAddFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltTabletMemberIsFirstFunc.functionName => new FeltTabletMemberIsFirstFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltTabletMemberIsLastFunc.functionName => new FeltTabletMemberIsLastFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
      case FeltTabletMemberValueFunc.functionName => new FeltTabletMemberValueFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
    }
  }

}
