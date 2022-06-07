/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio

import org.burstsys.felt.model.expressions.function.{FeltFuncExpr, FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

package object functions {

  trait FeltBrioFunction extends FeltFuncExpr

  /**
   * function call dispatcher for ''brio'' methods
   *
   * @return
   */
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case FeltBrioSizeFunc.functionName => new FeltBrioSizeFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = FeltBrioSizeFunc.functionName
      }
      case FeltBrioKeyFunc.functionName => new FeltBrioKeyFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = FeltBrioKeyFunc.functionName
      }
      case FeltBrioValueFunc.functionName => new FeltBrioValueFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = FeltBrioValueFunc.functionName
      }
      case FeltBrioKeysFunc.functionName => new FeltBrioKeysFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = FeltBrioKeysFunc.functionName
      }
      case FeltBrioValuesFunc.functionName => new FeltBrioValuesFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = FeltBrioValuesFunc.functionName
      }
      case FeltBrioIsFirstFunc.functionName => new FeltBrioIsFirstFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = FeltBrioIsFirstFunc.functionName
      }
      case FeltBrioIsLastFunc.functionName => new FeltBrioIsLastFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = FeltBrioIsLastFunc.functionName
      }
    }
  }

}
