/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.tree.FeltLocation

/**
 * =functions=
 */
package object function {

  /**
   * partial function chaining of dispatch
   */
  type FeltFunctionSelector = PartialFunction[String, FeltFuncExpr]


  /**
   * a type that can dispatch a FELT function call
   */
  trait FeltFunctionDispatcher {

    /**
     * attempt to satisfy a dispatch - return partial function
     *
     * @param location
     * @return
     */
    def apply(location: FeltLocation): FeltFunctionSelector

  }

  /**
   * When you run out of options...
   *
   * @return
   */
  def unknownFunction: FeltFunctionDispatcher = new FeltFunctionDispatcher {

    final override
    def apply(location: FeltLocation): FeltFunctionSelector = {
      case unknownFunction => throw FeltException(location, s"'$unknownFunction' is an unknown function")
    }

  }


}
