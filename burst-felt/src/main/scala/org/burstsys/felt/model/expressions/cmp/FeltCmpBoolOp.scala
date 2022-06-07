/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.cmp

import org.burstsys.felt.model.expressions.op.FeltBinaryOp
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.literals.primitive.{FeltBoolPrimitive, FeltFixPrimitive, FeltFltPrimitive, FeltStrPrimitive}
import org.burstsys.felt.model.tree.FeltLocation

/**
 * operators that do a value comparison between a left and right hand expression (that return
 * a boolean/logical true/false)
 */
trait FeltCmpBoolOp extends FeltBinaryOp {

  final override val nodeName = "felt-cmp-bool-op"

  /**
   * two OPs (`==`, `!=`) can deliver a determinative comparison to a null...
   *
   * @return If this OP does not support determinative literal null comparison return '''NONE'''
   *         else return the boolean sense of that comparison '''Some'''(`true`/`false`)
   */
  def determinativeNullComparison: Option[Boolean] = None

  //////////////////////////////////////////////////////////////
  // boolean
  //////////////////////////////////////////////////////////////
  def reduce(l: Boolean, r: Boolean): Option[Boolean] = None

  def reduce(l: Boolean, r: Long): Option[Boolean] = None

  def reduce(l: Boolean, r: Double): Option[Boolean] = None

  def reduce(l: Boolean, r: String): Option[Boolean] = None

  //////////////////////////////////////////////////////////////
  //fixed
  //////////////////////////////////////////////////////////////
  def reduce(l: Long, r: Boolean): Option[Boolean] = None

  def reduce(l: Long, r: Long): Option[Boolean] = None

  def reduce(l: Long, r: Double): Option[Boolean] = None

  def reduce(l: Long, r: String): Option[Boolean] = None

  //////////////////////////////////////////////////////////////
  // float
  //////////////////////////////////////////////////////////////
  def reduce(l: Double, r: Boolean): Option[Boolean] = None

  def reduce(l: Double, r: Long): Option[Boolean] = None

  def reduce(l: Double, r: Double): Option[Boolean] = None

  def reduce(l: Double, r: String): Option[Boolean] = None

  //////////////////////////////////////////////////////////////
  // string
  //////////////////////////////////////////////////////////////
  def reduce(l: String, r: Boolean): Option[Boolean] = None

  def reduce(l: String, r: Long): Option[Boolean] = None

  def reduce(l: String, r: Double): Option[Boolean] = None

  def reduce(l: String, r: String): Option[Boolean] = None

  /**
   * attempt to do a static comparison
   *
   * @param lhs
   * @param rhs
   * @return
   */
  def reduceLiterals(lhs: FeltLiteral, rhs: FeltLiteral): Option[FeltLiteral] = {
    val result: Option[Boolean] = lhs match {
      case l: FeltBoolPrimitive => rhs match {
        case r: FeltBoolPrimitive => reduce(l.value, r.value)
        case r: FeltFixPrimitive => reduce(l.value, r.value)
        case r: FeltFltPrimitive => reduce(l.value, r.value)
        case r: FeltStrPrimitive => reduce(l.value, r.value)
        case _ => None
      }
      case l: FeltFixPrimitive => rhs match {
        case r: FeltBoolPrimitive => reduce(l.value, r.value)
        case r: FeltFixPrimitive => reduce(l.value, r.value)
        case r: FeltFltPrimitive => reduce(l.value, r.value)
        case r: FeltStrPrimitive => reduce(l.value, r.value)
        case _ => None
      }
      case l: FeltFltPrimitive => rhs match {
        case r: FeltBoolPrimitive => reduce(l.value, r.value)
        case r: FeltFixPrimitive => reduce(l.value, r.value)
        case r: FeltFltPrimitive => reduce(l.value, r.value)
        case r: FeltStrPrimitive => reduce(l.value, r.value)
        case _ => None
      }
      case l: FeltStrPrimitive => rhs match {
        case r: FeltBoolPrimitive => reduce(l.value, r.value)
        case r: FeltFixPrimitive => reduce(l.value, r.value)
        case r: FeltFltPrimitive => reduce(l.value, r.value)
        case r: FeltStrPrimitive => reduce(l.value, r.value)
        case _ => None
      }
    }
    result match {
      case None => invalidType(lhs, rhs)
      case Some(v) => Some(
        new FeltBoolPrimitive {
          sync(FeltCmpBoolOp.this)
          final override val value: Boolean = v
          final override val location: FeltLocation = lhs.location
        }

      )
    }
  }

}

/**
 * true if left hand side is less than right hand side
 */
trait LESS_THAN extends FeltCmpBoolOp {

  final val symbol: String = "<"

  override def reduce(l: Boolean, r: Boolean): Option[Boolean] = Some(l < r)

  override def reduce(l: Boolean, r: Long): Option[Boolean] = None

  override def reduce(l: Boolean, r: Double): Option[Boolean] = None

  override def reduce(l: Boolean, r: String): Option[Boolean] = None

  override def reduce(l: Long, r: Boolean): Option[Boolean] = None

  override def reduce(l: Long, r: Long): Option[Boolean] = Some(l < r)

  override def reduce(l: Long, r: Double): Option[Boolean] = Some(l < r)

  override def reduce(l: Long, r: String): Option[Boolean] = None

  override def reduce(l: Double, r: Boolean): Option[Boolean] = None

  override def reduce(l: Double, r: Long): Option[Boolean] = Some(l < r)

  override def reduce(l: Double, r: Double): Option[Boolean] = Some(l < r)

  override def reduce(l: Double, r: String): Option[Boolean] = None

  override def reduce(l: String, r: Boolean): Option[Boolean] = None

  override def reduce(l: String, r: Long): Option[Boolean] = None

  override def reduce(l: String, r: Double): Option[Boolean] = None

  override def reduce(l: String, r: String): Option[Boolean] = Some(l < r)

}

/**
 * true if left hand side is equal to right hand side
 */
trait EQUAL extends FeltCmpBoolOp {

  final val symbol: String = "=="

  override def determinativeNullComparison: Option[Boolean] = Some(true)

  override def reduce(l: Boolean, r: Boolean): Option[Boolean] = Some(l == r)

  override def reduce(l: Boolean, r: Long): Option[Boolean] = None

  override def reduce(l: Boolean, r: Double): Option[Boolean] = None

  override def reduce(l: Boolean, r: String): Option[Boolean] = None

  override def reduce(l: Long, r: Boolean): Option[Boolean] = None

  override def reduce(l: Long, r: Long): Option[Boolean] = Some(l == r)

  override def reduce(l: Long, r: Double): Option[Boolean] = Some(l == r)

  override def reduce(l: Long, r: String): Option[Boolean] = None

  override def reduce(l: Double, r: Boolean): Option[Boolean] = None

  override def reduce(l: Double, r: Long): Option[Boolean] = Some(l == r)

  override def reduce(l: Double, r: Double): Option[Boolean] = Some(l == r)

  override def reduce(l: Double, r: String): Option[Boolean] = None

  override def reduce(l: String, r: Boolean): Option[Boolean] = None

  override def reduce(l: String, r: Long): Option[Boolean] = None

  override def reduce(l: String, r: Double): Option[Boolean] = None

  override def reduce(l: String, r: String): Option[Boolean] = Some(l == r)

}

/**
 * true if left hand side is greater than or equal to the right hand side
 */
trait GREATER_THAN_OR_EQUAL extends FeltCmpBoolOp {

  final val symbol: String = ">="

  override def reduce(l: Boolean, r: Boolean): Option[Boolean] = Some(l >= r)

  override def reduce(l: Boolean, r: Long): Option[Boolean] = None

  override def reduce(l: Boolean, r: Double): Option[Boolean] = None

  override def reduce(l: Boolean, r: String): Option[Boolean] = None

  override def reduce(l: Long, r: Boolean): Option[Boolean] = None

  override def reduce(l: Long, r: Long): Option[Boolean] = Some(l >= r)

  override def reduce(l: Long, r: Double): Option[Boolean] = Some(l >= r)

  override def reduce(l: Long, r: String): Option[Boolean] = None

  override def reduce(l: Double, r: Boolean): Option[Boolean] = None

  override def reduce(l: Double, r: Long): Option[Boolean] = Some(l >= r)

  override def reduce(l: Double, r: Double): Option[Boolean] = Some(l >= r)

  override def reduce(l: Double, r: String): Option[Boolean] = None

  override def reduce(l: String, r: Boolean): Option[Boolean] = None

  override def reduce(l: String, r: Long): Option[Boolean] = None

  override def reduce(l: String, r: Double): Option[Boolean] = None

  override def reduce(l: String, r: String): Option[Boolean] = Some(l >= r)

}

/**
 * true if left hand side is less than or equal to the right hand side
 */
trait LESS_THAN_OR_EQUAL extends FeltCmpBoolOp {

  final val symbol: String = "<="

  override def reduce(l: Boolean, r: Boolean): Option[Boolean] = Some(l <= r)

  override def reduce(l: Boolean, r: Long): Option[Boolean] = None

  override def reduce(l: Boolean, r: Double): Option[Boolean] = None

  override def reduce(l: Boolean, r: String): Option[Boolean] = None

  override def reduce(l: Long, r: Boolean): Option[Boolean] = None

  override def reduce(l: Long, r: Long): Option[Boolean] = Some(l <= r)

  override def reduce(l: Long, r: Double): Option[Boolean] = Some(l <= r)

  override def reduce(l: Long, r: String): Option[Boolean] = None

  override def reduce(l: Double, r: Boolean): Option[Boolean] = None

  override def reduce(l: Double, r: Long): Option[Boolean] = Some(l <= r)

  override def reduce(l: Double, r: Double): Option[Boolean] = Some(l <= r)

  override def reduce(l: Double, r: String): Option[Boolean] = None

  override def reduce(l: String, r: Boolean): Option[Boolean] = None

  override def reduce(l: String, r: Long): Option[Boolean] = None

  override def reduce(l: String, r: Double): Option[Boolean] = None

  override def reduce(l: String, r: String): Option[Boolean] = Some(l <= r)

}

/**
 * true if left hand side is not equal to the right hand side
 */
trait NOT_EQUAL extends FeltCmpBoolOp {

  final val symbol: String = "!="

  override def determinativeNullComparison: Option[Boolean] = Some(false)

  override def reduce(l: Boolean, r: Boolean): Option[Boolean] = Some(l != r)

  override def reduce(l: Boolean, r: Long): Option[Boolean] = None

  override def reduce(l: Boolean, r: Double): Option[Boolean] = None

  override def reduce(l: Boolean, r: String): Option[Boolean] = None

  override def reduce(l: Long, r: Boolean): Option[Boolean] = None

  override def reduce(l: Long, r: Long): Option[Boolean] = Some(l != r)

  override def reduce(l: Long, r: Double): Option[Boolean] = Some(l != r)

  override def reduce(l: Long, r: String): Option[Boolean] = None

  override def reduce(l: Double, r: Boolean): Option[Boolean] = None

  override def reduce(l: Double, r: Long): Option[Boolean] = Some(l != r)

  override def reduce(l: Double, r: Double): Option[Boolean] = Some(l != r)

  override def reduce(l: Double, r: String): Option[Boolean] = None

  override def reduce(l: String, r: Boolean): Option[Boolean] = None

  override def reduce(l: String, r: Long): Option[Boolean] = None

  override def reduce(l: String, r: Double): Option[Boolean] = None

  override def reduce(l: String, r: String): Option[Boolean] = Some(l != r)

}

/**
 * true if left hand side is greater than the right hand side
 */
trait GREATER_THAN extends FeltCmpBoolOp {

  final val symbol: String = ">"

  override def reduce(l: Boolean, r: Boolean): Option[Boolean] = Some(l > r)

  override def reduce(l: Boolean, r: Long): Option[Boolean] = None

  override def reduce(l: Boolean, r: Double): Option[Boolean] = None

  override def reduce(l: Boolean, r: String): Option[Boolean] = None

  override def reduce(l: Long, r: Boolean): Option[Boolean] = None

  override def reduce(l: Long, r: Long): Option[Boolean] = Some(l > r)

  override def reduce(l: Long, r: Double): Option[Boolean] = Some(l > r)

  override def reduce(l: Long, r: String): Option[Boolean] = None

  override def reduce(l: Double, r: Boolean): Option[Boolean] = None

  override def reduce(l: Double, r: Long): Option[Boolean] = Some(l > r)

  override def reduce(l: Double, r: Double): Option[Boolean] = Some(l > r)

  override def reduce(l: Double, r: String): Option[Boolean] = None

  override def reduce(l: String, r: Boolean): Option[Boolean] = None

  override def reduce(l: String, r: Long): Option[Boolean] = None

  override def reduce(l: String, r: Double): Option[Boolean] = None

  override def reduce(l: String, r: String): Option[Boolean] = Some(l > r)

}
