/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube

import org.burstsys.felt.model.expressions.function.{FeltFuncExpr, FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

package object functions {

  trait FeltCubeFunction extends FeltFuncExpr

  /**
   * function call dispatcher for ''felt cube'' specific methods
   *
   * @return
   */
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    final override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case FeltCubeInsertFunc.functionName => new FeltCubeInsertFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }
    }

  }


}
