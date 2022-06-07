/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu

import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.ginsu
import org.burstsys.felt.model.tree.FeltLocation

package object functions {

  /**
   * function call dispatcher for ''ginsu'' methods
   *
   * @return
   */
  final def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      ginsu.group.dispatch(functionLocation) orElse
        ginsu.datetime.duration.dispatch(functionLocation) orElse
        ginsu.datetime.grain.dispatch(functionLocation) orElse
        ginsu.datetime.ordinal.dispatch(functionLocation) orElse
        ginsu.datetime.ticks.dispatch(functionLocation) orElse
        ginsu.datetime.now.dispatch(functionLocation)
    }

  }
}
