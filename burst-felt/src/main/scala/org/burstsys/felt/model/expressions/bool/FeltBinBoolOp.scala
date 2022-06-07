/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.bool

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.op.FeltBinaryOp
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.literals.primitive.{FeltBoolPrimitive, FeltNullPrimitive}
import org.burstsys.felt.model.tree.FeltLocation

/**
 * operators that do a boolean operation between a left and right hand expression
 *
 */
trait FeltBinBoolOp extends FeltBinaryOp {

  final override val nodeName = "felt-bin-bool-op"

  def booleanOp(lhs: Boolean, rhs: Boolean): Boolean

  final override
  def reduceLiterals(lhs: FeltLiteral, rhs: FeltLiteral): Option[FeltLiteral] = {
    lhs match {
      case lbl: FeltBoolPrimitive =>
        rhs match {
          case rbl: FeltBoolPrimitive => Some(
            new FeltBoolPrimitive {
              final override val location: FeltLocation = lhs.location

              override def value: Boolean = booleanOp(lbl.value, rbl.value)
            }
          )
          case _ => None
        }
      case _ => None
    }
  }

}

/**
 * A binary boolean conjunction
 */
trait AND extends FeltBinBoolOp {

  final val symbol = "&&"

  final override def booleanOp(lhs: Boolean, rhs: Boolean): Boolean = lhs && rhs

  /**
   * special case for BOOLEAN AND. If either side reduces to null then this is null
   *
   * @param lhs
   * @param rhs
   * @return
   */
  final override def reduceToNull(lhs: Option[FeltExpression], rhs: Option[FeltExpression]): Option[FeltLiteral] = {
    if (lhs.isEmpty || rhs.isEmpty) return None
    // special case AND with either side null
    if (lhs.get.reduceToNull.nonEmpty || rhs.get.reduceToNull.nonEmpty) {
      return Some(
        new FeltNullPrimitive {
          sync(AND.this)

          override def location: FeltLocation = AND.this.location
        }
      )
    }
    super.reduceToNull(lhs, rhs)
  }

}

/**
 * A binary boolean disjunction
 */
trait OR extends FeltBinBoolOp {

  final val symbol = "||"

  final override def booleanOp(lhs: Boolean, rhs: Boolean): Boolean = lhs || rhs

  /**
   * special case for BOOLEAN OR. If either side reduces to boolean false literal then this is false
   *
   * @param lhs
   * @param rhs
   * @return
   */
  final override def reduceToNull(lhs: Option[FeltExpression], rhs: Option[FeltExpression]): Option[FeltLiteral] = {
    if (lhs.nonEmpty) {
      lhs.get.reduceToBoolAtom match {
        case None =>
        case Some(b) => if (b.value) return Some(b)
      }
    }
    if (rhs.nonEmpty) {
      rhs.get.reduceToBoolAtom match {
        case None =>
        case Some(b) => if (b.value) return Some(b)
      }
    }
    super.reduceToNull(lhs, rhs)
  }

}
