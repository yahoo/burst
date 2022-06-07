/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.bool

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.op.FeltUnaryOp
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.literals.primitive.FeltBoolPrimitive
import org.burstsys.felt.model.tree.FeltLocation

/**
 * unary boolean/logical math operation
 */
trait FeltUnaryBoolOp extends FeltUnaryOp {

  final override val nodeName = "felt-unary-bool-op"
}


/**
 * logical inversion operator
 */
trait NOT extends FeltUnaryBoolOp {
  final val symbol = "!"

  def reducePrimitive(rhs: FeltLiteral): Option[FeltLiteral] = {
    rhs match {
      case b: FeltBoolPrimitive =>
        Some(
          new FeltBoolPrimitive {
            sync(NOT.this)
            final override val location: FeltLocation = rhs.location
            final override val value: Boolean = !b.value
          }
        )
      case _ => throw FeltException(rhs.location, s"$this must be boolean")
    }
  }

}
