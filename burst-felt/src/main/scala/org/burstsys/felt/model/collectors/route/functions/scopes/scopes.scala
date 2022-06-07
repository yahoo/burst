/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.functions

import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

package object scopes {

  trait FeltRouteScopeFunction extends FeltRouteFunction

  /**
   * function call dispatcher for ''felt route'' scope specific methods
   *
   * @return
   */
  private[functions]
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    final override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case FeltRouteScopeAbortFunc.functionName => new FeltRouteScopeAbortFunc {
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteScopeCommitFunc.functionName => new FeltRouteScopeCommitFunc {
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteScopeCurrentPathFunc.functionName => new FeltRouteScopeCurrentPathFunc {
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteScopeCurrentStepFunc.functionName => new FeltRouteScopeCurrentStepFunc {
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteScopePathChangedFunc.functionName => new FeltRouteScopePathChangedFunc {
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteScopePriorPathFunc.functionName => new FeltRouteScopePriorPathFunc {
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteScopePriorStepFunc.functionName => new FeltRouteScopePriorStepFunc {
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteScopeStartFunc.functionName => new FeltRouteScopeStartFunc {
        final override val location: FeltLocation = functionLocation
      }
      case FeltRouteScopeStepChangedFunc.functionName => new FeltRouteScopeStepChangedFunc {
        final override val location: FeltLocation = functionLocation
      }
    }

  }

}
