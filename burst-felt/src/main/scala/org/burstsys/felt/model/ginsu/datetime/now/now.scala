/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu.datetime

import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

package object now {

  final val NOW = "now"

  final def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {

      case NOW => new FeltGinsuNowFuncExpr {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }

    }

  }

}
