/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.literals.primitive._
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

/**
 * the generic ''expression'' node type
 */
trait FeltExpression extends FeltNode {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * if this is reducible, then return the constant result of that expression
   *
   * @return
   */
  def reduceToLiteral: Option[FeltLiteral] = None

  /**
   * if this is reducible to a null, then return the null atom result of that expression
   *
   * @return
   */
  def reduceToNull: Option[FeltNullPrimitive] = None

  /**
   * if this is reducible to a boolean, then return the boolean constant result of that expression
   *
   * @return
   */
  final
  def reduceToBoolAtom: Option[FeltBoolPrimitive] = reduceToLiteral match {
    case None => None
    case Some(pl) => pl match {
      case bl: FeltBoolPrimitive => Some(bl)
      case _ => None
    }
  }

  final
  def reduceToBoolAtomOrThrow: FeltBoolPrimitive = reduceToBoolAtom.getOrElse(
    throw FeltException(location, s"could not reduce to boolean atom")
  )

  /**
   * if this is reducible to a fixed, then return the fixed constant result of that expression
   *
   * @return
   */
  final
  def reduceToFixAtom: Option[FeltFixPrimitive] = reduceToLiteral match {
    case None =>
      None
    case Some(pl) => pl match {
      case bl: FeltFixPrimitive => Some(bl)
      case _ => None
    }
  }

  final
  def reduceToFixAtomOrThrow: FeltFixPrimitive = reduceToFixAtom.getOrElse(
    throw FeltException(location, s"could not reduce to fixed atom")
  )

  /**
   * if this is reducible to a float, then return the float constant result of that expression
   *
   * @return
   */
  final
  def reduceToFltAtom: Option[FeltFltPrimitive] = reduceToLiteral match {
    case None => None
    case Some(pl) => pl match {
      case bl: FeltFltPrimitive => Some(bl)
      case _ => None
    }
  }

  final
  def reduceToFltAtomOrThrow: FeltFltPrimitive = reduceToFltAtom.getOrElse(
    throw FeltException(location, s"could not reduce to float atom")
  )

  /**
   * if this is reducible to a string, then return the string constant result of that expression
   *
   * @return
   */
  final
  def reduceToStrAtom: Option[FeltStrPrimitive] = reduceToLiteral match {
    case None => None
    case Some(pl) => pl match {
      case bl: FeltStrPrimitive => Some(bl)
      case _ => None
    }
  }

  final
  def reduceToStrAtomOrThrow: FeltStrPrimitive = reduceToStrAtom.getOrElse(
    throw FeltException(location, s"could not reduce to string atom")
  )

  /**
   * turn this expression into an alternative optimized expressions
   *
   * @return
   */
  def reduceStatics: FeltExpression = this

  /**
   * validate that the references in this expression are valid at a particular
   * traversal point
   *
   * @param traversePt
   */
  def validateBrioReferences(traversePt: BrioNode): Unit = {}

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Generate executable code to be used in a Felt sweep.
   *
   * @param cursor
   * @return
   */
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode

}




