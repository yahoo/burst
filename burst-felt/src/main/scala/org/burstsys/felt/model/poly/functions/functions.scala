/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.poly

import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

package object functions {

  /**
   * function call dispatcher for ''star'' methods. These are methods
   * that work on more than one reference type and provide commonly
   * used functions across collectors, brio, mutables etc
   *
   * @return
   */
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {

      case FeltPolyContainsFunc.functionName => new FeltPolyContainsFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = FeltPolyContainsFunc.functionName
      }

    }
  }

}
