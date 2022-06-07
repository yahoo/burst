/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.poly

import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.reference.FeltReference
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

/**
 * generate the code associated with polymorphic dispatch functions
 */
trait FeltPolyRefFuncGen extends FeltReference {

  def generateContainsPolyFunc(func: FeltFuncExpr)(implicit cursor: FeltCodeCursor): FeltCode

}
