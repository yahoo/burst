/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.model.expressions.function.FeltFuncExpr

/**
 * =ginsu=
 * The Ginsu package is a set of Felt builtin dicing and slicing functions with runtime support.
 * @see [[FeltFuncExpr]]
 */
package object ginsu {

  trait FeltGinsuFunction extends FeltFuncExpr

}
