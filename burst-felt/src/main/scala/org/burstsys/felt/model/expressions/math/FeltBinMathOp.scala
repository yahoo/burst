/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.math

import org.burstsys.felt.model.expressions.op.FeltBinaryOp
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.literals.primitive.{FeltBoolPrimitive, FeltFixPrimitive, FeltFltPrimitive, FeltStrPrimitive}
import org.burstsys.felt.model.tree.FeltLocation

/**
 * operators that do a math operation between a left and right hand expression that both resolve to a
 * value
 */
trait FeltBinMathOp extends FeltBinaryOp {

  final override val nodeName = "felt-bin-math-op"

  //////////////////////////////////////////////////////////////
  //fixed
  //////////////////////////////////////////////////////////////

  def reduce(l: Long, r: Long): Option[Any] = None

  def reduce(l: Long, r: Double): Option[Any] = None

  def reduce(l: Long, r: String): Option[Any] = None

  //////////////////////////////////////////////////////////////
  // float
  //////////////////////////////////////////////////////////////

  def reduce(l: Double, r: Long): Option[Any] = None

  def reduce(l: Double, r: Double): Option[Any] = None

  def reduce(l: Double, r: String): Option[Any] = None

  //////////////////////////////////////////////////////////////
  // string
  //////////////////////////////////////////////////////////////
  def reduce(l: String, r: Boolean): Option[Any] = None

  def reduce(l: String, r: Long): Option[Any] = None

  def reduce(l: String, r: Double): Option[Any] = None

  def reduce(l: String, r: String): Option[Any] = None

  /**
   * attempt to do a static comparison
   *
   * @param lhs
   * @param rhs
   * @return
   */
  def reduceLiterals(lhs: FeltLiteral, rhs: FeltLiteral): Option[FeltLiteral] = {
    val result: Option[Any] = lhs match {
      case l: FeltBoolPrimitive => rhs match {
        case _ => None
      }
      case l: FeltFixPrimitive => rhs match {
        case r: FeltFixPrimitive => reduce(l.value, r.value)
        case r: FeltFltPrimitive => reduce(l.value.toDouble, r.value)
        case r: FeltStrPrimitive => reduce(l.value, r.value)
        case _ => None
      }
      case l: FeltFltPrimitive => rhs match {
        case r: FeltFixPrimitive => reduce(l.value, r.value.toDouble)
        case r: FeltFltPrimitive => reduce(l.value, r.value)
        case r: FeltStrPrimitive => reduce(l.value, r.value.toDouble)
        case _ => None
      }
      case l: FeltStrPrimitive => rhs match {
        case r: FeltFixPrimitive => reduce(l.value, r.value)
        case r: FeltFltPrimitive => reduce(l.value, r.value)
        case r: FeltStrPrimitive => reduce(l.value, r.value)
        case _ => None
      }
    }
    result match {
      case None => invalidType(lhs, rhs)
      case Some(v) => v match {

        case l: Long =>
          Some(
            new FeltFixPrimitive {
              sync(FeltBinMathOp.this)
              final override val value: Long = l
              final override val location: FeltLocation = lhs.location
            }
          )

        case f: Double =>
          Some(
            new FeltFltPrimitive {
              sync(FeltBinMathOp.this)
              final override val value: Double = f
              final override val location: FeltLocation = lhs.location
            }
          )

        case s: String =>
          Some(
            new FeltStrPrimitive {
              sync(FeltBinMathOp.this)
              final override val value: String = s
              final override val location: FeltLocation = lhs.location
            }
          )

        case _ => invalidType(lhs, rhs)
      }
    }
  }

}

/**
 * mathematical add
 */
trait ADD extends FeltBinMathOp {

  final val symbol = "+"


  override def reduce(l: Long, r: Long): Option[Any] = Some(l + r)

  override def reduce(l: Long, r: Double): Option[Any] = Some(l + r)

  override def reduce(l: Long, r: String): Option[Any] = Some(l + r)


  override def reduce(l: Double, r: Long): Option[Any] = Some(l + r)

  override def reduce(l: Double, r: Double): Option[Any] = Some(l + r)

  override def reduce(l: Double, r: String): Option[Any] = Some(l + r)


  override def reduce(l: String, r: Long): Option[Any] = Some(l + r)

  override def reduce(l: String, r: Double): Option[Any] = Some(l + r)

  override def reduce(l: String, r: String): Option[Any] = Some(l + r)

}

/**
 * mathematical subtraction
 */
trait SUBTRACT extends FeltBinMathOp {

  final val symbol = "-"


  override def reduce(l: Long, r: Long): Option[Any] = Some(l - r)

  override def reduce(l: Long, r: Double): Option[Any] = Some(l - r)

  override def reduce(l: Long, r: String): Option[Any] = None


  override def reduce(l: Double, r: Long): Option[Any] = Some(l - r)

  override def reduce(l: Double, r: Double): Option[Any] = Some(l - r)

  override def reduce(l: Double, r: String): Option[Any] = None


  override def reduce(l: String, r: Long): Option[Any] = None

  override def reduce(l: String, r: Double): Option[Any] = None

  override def reduce(l: String, r: String): Option[Any] = None

}

/**
 * mathematical multiplication
 */
trait MULTIPLY extends FeltBinMathOp {

  final val symbol = "*"


  override def reduce(l: Long, r: Long): Option[Any] = Some(l * r)

  override def reduce(l: Long, r: Double): Option[Any] = Some(l * r)

  override def reduce(l: Long, r: String): Option[Any] = None


  override def reduce(l: Double, r: Long): Option[Any] = Some(l * r)

  override def reduce(l: Double, r: Double): Option[Any] = Some(l * r)

  override def reduce(l: Double, r: String): Option[Any] = None


  override def reduce(l: String, r: Long): Option[Any] = None

  override def reduce(l: String, r: Double): Option[Any] = None

  override def reduce(l: String, r: String): Option[Any] = None

}

/**
 * mathematical modulo
 */
trait MODULO extends FeltBinMathOp {

  final val symbol = "%"


  override def reduce(l: Long, r: Long): Option[Any] = Some(l % r)

  override def reduce(l: Long, r: Double): Option[Any] = Some(l % r)

  override def reduce(l: Long, r: String): Option[Any] = None


  override def reduce(l: Double, r: Long): Option[Any] = Some(l % r)

  override def reduce(l: Double, r: Double): Option[Any] = Some(l % r)

  override def reduce(l: Double, r: String): Option[Any] = None


  override def reduce(l: String, r: Long): Option[Any] = None

  override def reduce(l: String, r: Double): Option[Any] = None

  override def reduce(l: String, r: String): Option[Any] = None

}


/**
 * mathematical divide
 */
trait DIVIDE extends FeltBinMathOp {

  final val symbol = "/"


  override def reduce(l: Long, r: Long): Option[Any] = Some(l / r)

  override def reduce(l: Long, r: Double): Option[Any] = Some(l / r)

  override def reduce(l: Long, r: String): Option[Any] = None


  override def reduce(l: Double, r: Long): Option[Any] = Some(l / r)

  override def reduce(l: Double, r: Double): Option[Any] = Some(l / r)

  override def reduce(l: Double, r: String): Option[Any] = None


  override def reduce(l: String, r: Long): Option[Any] = None

  override def reduce(l: String, r: Double): Option[Any] = None

  override def reduce(l: String, r: String): Option[Any] = None

}


