/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route

import org.burstsys.felt.model.collectors.route
import org.burstsys.felt.model.expressions.function.{FeltFuncExpr, FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

package object functions {

  trait FeltRouteFunction extends FeltFuncExpr

  /**
   * function call dispatcher for ''felt route'' specific methods
   *
   * @return
   */
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    final override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      route.functions.scopes.dispatch(functionLocation) orElse
        route.functions.common.dispatch(functionLocation) orElse
        route.functions.fsm.dispatch(functionLocation) orElse
        route.functions.visits.dispatch(functionLocation)
    }

  }

}
