/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control

import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation

package object functions {

  /**
   * dispatch for ''control verb'' function calls
   *
   * @return
   */
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {

      case FeltCtrlAbortMemberFunc.functionName => new FeltCtrlAbortMemberFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }

      case FeltCtrlCommitMemberFunc.functionName => new FeltCtrlCommitMemberFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }

      case FeltCtrlAbortRelationFunc.functionName => new FeltCtrlAbortRelationFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }

      case FeltCtrlCommitRelationFunc.functionName => new FeltCtrlCommitRelationFunc {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
      }

    }

  }
}
