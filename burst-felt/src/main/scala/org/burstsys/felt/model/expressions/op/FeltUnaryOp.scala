/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.op

import org.burstsys.felt.model.literals.FeltLiteral

/**
 */
trait FeltUnaryOp extends FeltOperator {

  def reducePrimitive(get: FeltLiteral): Option[FeltLiteral]

  final
  def reduceToPrimLit(rhs: Option[FeltLiteral]): Option[FeltLiteral] = {
    if (rhs.isEmpty) return None
    val nullity = reduceToNull(rhs)
    if (nullity.nonEmpty) return nullity
    val rl = rhs.get.reduceToLiteral
    if (rl.isEmpty) return None
    reducePrimitive(rl.get)
  }

  final
  def reduceToNull(rhs: Option[FeltLiteral]): Option[FeltLiteral] = {
    if (rhs.isEmpty) return None
    // if either side is null - this expression is null
    if (rhs.get.reduceToNull.nonEmpty) return rhs.get.reduceToNull
    None
  }

}
