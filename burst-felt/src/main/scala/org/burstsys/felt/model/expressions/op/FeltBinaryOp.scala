/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.op

import org.burstsys.brio.types.BrioTypes.brioDataTypeNameFromKey
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.literals.FeltLiteral

/**
 * an operator that takes two arguments - left and right hand sides (lhs, rhs)
 */
trait FeltBinaryOp extends FeltOperator {

  protected final
  def invalidType[T <: Any](lhs: FeltLiteral, rhs: FeltLiteral): Option[T] = {
    val ldt = brioDataTypeNameFromKey(lhs.feltType.valueType).toLowerCase
    val rdt = brioDataTypeNameFromKey(rhs.feltType.valueType).toLowerCase
    throw FeltException(lhs, s"operation [ $ldt '$symbol' $rdt ] not supported")
  }

  /**
   * attempt to reduce both sides of a binary expression to a static null. If either side does not statically
   * reduce no matter how hard we try, return NONE. Otherwise return a statically reduced literal
   *
   * @param lhs
   * @param rhs
   * @return
   */
  def reduceToNull(lhs: Option[FeltExpression], rhs: Option[FeltExpression]): Option[FeltLiteral] = {
    lhs match {
      case None => return None
      case Some(e) => e.reduceToNull match {
        case None => None
        case Some(n) => return Some(n)
      }
    }
    rhs match {
      case None => None
      case Some(e) => e.reduceToNull match {
        case None => None
        case Some(n) => Some(n)
      }
    }
  }

  final
  def reduceToLiteral(lhs: Option[FeltLiteral], rhs: Option[FeltLiteral]): Option[FeltLiteral] = {
    reduceToNull(lhs, rhs) match {
      case None =>
      case Some(n) => return Some(n)
    }

    if (lhs.isEmpty || rhs.isEmpty) return None

    val ll = lhs.get.reduceToLiteral
    val rl = rhs.get.reduceToLiteral

    if (ll.isEmpty || rl.isEmpty) return None
    reduceLiterals(ll.get, rl.get)
  }

  /**
   * you have a valid rhs and lhs reduced literal  so you must try to reduce using the op
   *
   * @param lhs
   * @param rhs
   * @return
   */
  def reduceLiterals(lhs: FeltLiteral, rhs: FeltLiteral): Option[FeltLiteral]

}
