/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu

import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.`enum`.ENUM_DIMENSION_SEMANTIC
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.split.SPLIT_DIMENSION_SEMANTIC
import org.burstsys.felt.model.expressions.function.{FeltFunctionDispatcher, FeltFunctionSelector}
import org.burstsys.felt.model.tree.FeltLocation


package object group {

  trait FeltGinsuGrpFuncExpr extends FeltGinsuFunction

  /**
   * function call dispatcher for ''ginsu'' grouping methods
   *
   * @return
   */
  def dispatch: FeltFunctionDispatcher = new FeltFunctionDispatcher {
    override
    def apply(functionLocation: FeltLocation): FeltFunctionSelector = {
      case ENUM_DIMENSION_SEMANTIC.id => new FeltGinsuGrpEnumFuncExpr {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = ENUM_DIMENSION_SEMANTIC.name
      }
      case SPLIT_DIMENSION_SEMANTIC.id => new FeltGinsuGrpSplitFuncExpr {
        global = functionLocation.global
        final override val location: FeltLocation = functionLocation
        final override val functionName: String = SPLIT_DIMENSION_SEMANTIC.name
      }
    }
  }

}
