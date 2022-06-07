/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.math

import org.burstsys.felt.model.expressions.op.FeltUnaryOp
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.literals.primitive.{FeltFixPrimitive, FeltFltPrimitive}
import org.burstsys.felt.model.tree.FeltLocation

/**
 * operators that act on a single right hand side expression
 */
trait FeltUnaryMathOp extends FeltUnaryOp {

  final override val nodeName = "felt-unary-math-op"


}

/**
 * the identity operator on the the right hand side expression - no change - included for expression symmetry
 */
trait POSIT extends FeltUnaryMathOp {

  final val symbol = "+"

  final override
  def reducePrimitive(literal: FeltLiteral): Option[FeltLiteral] = Some(literal)

}

/**
 * invert or negate the right hand side expression
 */
trait NEGATE extends FeltUnaryMathOp {

  final val symbol = "-"

  final override
  def reducePrimitive(literal: FeltLiteral): Option[FeltLiteral] = {
    literal match {
      case l: FeltFixPrimitive =>
        Some(new FeltFixPrimitive {
          sync(NEGATE.this)
          final override val value: Long = -l.value
          final override val location: FeltLocation = l.location

        })
      case l: FeltFltPrimitive =>
        Some(new FeltFltPrimitive {
          sync(NEGATE.this)
          final override val value: Double = -l.value
          final override val location: FeltLocation = l.location

        })
      case _ => None
    }
  }

}

