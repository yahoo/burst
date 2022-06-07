/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.cast

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.literals._
import org.burstsys.felt.model.literals.primitive.{FeltBoolPrimitive, FeltFixPrimitive, FeltFltPrimitive, FeltStrPrimitive}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.{FeltType, FeltTypeDecl}
import org.burstsys.vitals.strings._

import scala.reflect.ClassTag

/**
 * an expression that performs a ''coerce'' from the type of a given expression to
 * another type (if compatible)
 */
trait FeltCastNumberExpr extends FeltCastExpr with FeltCastStringGen with FeltCastNumberGen {

  final override val nodeName = "felt-cast-expr"

  /**
   * This is a static determinable semantic
   *
   * @return
   */
  def typeDeclaration: FeltTypeDecl

  /**
   * the expression to be ''coerce''
   *
   * @return
   */
  def expression: FeltExpression

  final val usage: String =
    s"""|USAGE: cast( e1:FeltExpression as t1:FeltTypeDeclaration ) -> e2:FeltExpression
        | return an expression 'e2' created by ''casting'' the resulting type of the given expression 'e1'
        | to a different type 't1' (if possible)
        |""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ typeDeclaration.treeApply(rule) ++ expression.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = Array(typeDeclaration, expression)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = expression.canInferTypes && typeDeclaration.canInferTypes

  final override
  def resolveTypes: this.type = {
    expression.resolveTypes
    typeDeclaration.resolveTypes
    feltType = typeDeclaration.feltType
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceToLiteral: Option[FeltLiteral] = {
    resolveTypes
    expression.reduceToLiteral match {
      // expression can't be statically reduced - we will need a dynamic cast
      case None => None
      // expression statically reduced - lets cast statically
      case Some(lit) =>
        val sourceType = expression.feltType
        val destinationType = typeDeclaration.feltType
        // no cast necessary
        if (sourceType == destinationType)
          return Some(lit)
        // perform the cast
        Some(staticCast(lit, sourceType, destinationType))
    }
  }

  private
  def staticCast(lit: FeltLiteral, sourceType: FeltType, destinationType: FeltType): FeltLiteral = {
    // conform to the destination type
    (lit, destinationType.valueType) match {
      // convert string to boolean
      case (s: FeltStrPrimitive, BrioBooleanKey) =>
        new FeltBoolPrimitive {
          sync(FeltCastNumberExpr.this)
          override val value: Boolean = s.value.toBoolean
          override val location: FeltLocation = s.location
        }
      // convert string to byte, short, integer, or long
      case (s: FeltStrPrimitive, dt) if dt == BrioByteKey || dt == BrioShortKey || dt == BrioIntegerKey || dt == BrioLongKey =>
        new FeltFixPrimitive {
          sync(FeltCastNumberExpr.this)
          override val value: Long = s.value.toLong
          override val location: FeltLocation = s.location
          coerce(destinationType)
        }
      case (s: FeltStrPrimitive, BrioDoubleKey) =>
        new FeltFltPrimitive {
          sync(FeltCastNumberExpr.this)
          override val value: Double = s.value.toDouble
          override val location: FeltLocation = s.location
        }
      case (s: FeltFixPrimitive, dt) if dt == BrioByteKey || dt == BrioShortKey || dt == BrioIntegerKey || dt == BrioLongKey =>
        s.coerce(destinationType)
        s
      case (s: FeltFixPrimitive, BrioDoubleKey) =>
        new FeltFltPrimitive {
          sync(FeltCastNumberExpr.this)
          override val value: Double = s.value.toDouble
          override val location: FeltLocation = s.location
        }
      case (s: FeltFltPrimitive, dt) if dt == BrioByteKey || dt == BrioShortKey || dt == BrioIntegerKey || dt == BrioLongKey =>
        new FeltFixPrimitive {
          sync(FeltCastNumberExpr.this)
          override val value: Long = s.value.toLong
          override val location: FeltLocation = s.location
          coerce(destinationType)
        }
      case (s: FeltFltPrimitive, BrioStringKey) =>
        new FeltStrPrimitive {
          sync(FeltCastNumberExpr.this)
          override val value: String = s.value.toString
          override val location: FeltLocation = s.location
        }
      case (s: FeltFixPrimitive, BrioStringKey) =>
        new FeltStrPrimitive {
          sync(FeltCastNumberExpr.this)
          override val value: String = s.value.toString
          override val location: FeltLocation = s.location
        }
      case _ =>
        assertIncompatible
        lit
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExpression = {
    reduceToLiteral match {
      case Some(l) => l
      case None => new FeltCastNumberExpr {
        sync(FeltCastNumberExpr.this)
        final override val location: FeltLocation = FeltCastNumberExpr.this.location
        final override val typeDeclaration: FeltTypeDecl = FeltCastNumberExpr.this.typeDeclaration.reduceStatics.resolveTypes
        final override val expression: FeltExpression = FeltCastNumberExpr.this.expression.reduceStatics.resolveTypes
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    s"${S}cast(${expression.normalizedSource} as ${typeDeclaration.normalizedSource})"
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    val sourceType = expression.feltType
    val destinationType = typeDeclaration.feltType
    if (sourceType == destinationType)
      throw FeltException(expression.location,
        s"'${expression.normalizedSource.condensed}' cast type is same as source type ${expression.feltType.valueTypeAsFelt} -> ${typeDeclaration.feltType.valueTypeAsFelt}")

    (sourceType.valueType, destinationType.valueType) match {

      case (BrioStringKey, BrioBooleanKey) => castStringToValue
      case (BrioStringKey, BrioIntegerKey) => castStringToValue
      case (BrioStringKey, BrioShortKey) => castStringToValue
      case (BrioStringKey, BrioByteKey) => castStringToValue
      case (BrioStringKey, BrioLongKey) => castStringToValue
      case (BrioStringKey, BrioDoubleKey) => castStringToValue

      case (BrioBooleanKey, BrioStringKey) => castValueToString
      case (BrioByteKey, BrioStringKey) => castValueToString
      case (BrioShortKey, BrioStringKey) => castValueToString
      case (BrioIntegerKey, BrioStringKey) => castValueToString
      case (BrioLongKey, BrioStringKey) => castValueToString
      case (BrioDoubleKey, BrioStringKey) => castValueToString

      case (BrioByteKey, BrioBooleanKey) => assertIncompatible
      case (BrioShortKey, BrioBooleanKey) => assertIncompatible
      case (BrioIntegerKey, BrioBooleanKey) => assertIncompatible
      case (BrioLongKey, BrioBooleanKey) => assertIncompatible
      case (BrioDoubleKey, BrioBooleanKey) => assertIncompatible

      case (BrioBooleanKey, BrioByteKey) => assertIncompatible
      case (BrioBooleanKey, BrioShortKey) => assertIncompatible
      case (BrioBooleanKey, BrioIntegerKey) => assertIncompatible
      case (BrioBooleanKey, BrioLongKey) => assertIncompatible
      case (BrioBooleanKey, BrioDoubleKey) => assertIncompatible

      case (BrioByteKey, BrioShortKey) => castNumberToNumber
      case (BrioByteKey, BrioLongKey) => castNumberToNumber
      case (BrioByteKey, BrioIntegerKey) => castNumberToNumber
      case (BrioByteKey, BrioDoubleKey) => castNumberToNumber

      case (BrioShortKey, BrioByteKey) => castNumberToNumber
      case (BrioShortKey, BrioIntegerKey) => castNumberToNumber
      case (BrioShortKey, BrioLongKey) => castNumberToNumber
      case (BrioShortKey, BrioDoubleKey) => castNumberToNumber

      case (BrioIntegerKey, BrioByteKey) => castNumberToNumber
      case (BrioIntegerKey, BrioShortKey) => castNumberToNumber
      case (BrioIntegerKey, BrioLongKey) => castNumberToNumber
      case (BrioIntegerKey, BrioDoubleKey) => castNumberToNumber

      case (BrioLongKey, BrioByteKey) => castNumberToNumber
      case (BrioLongKey, BrioShortKey) => castNumberToNumber
      case (BrioLongKey, BrioIntegerKey) => castNumberToNumber
      case (BrioLongKey, BrioDoubleKey) => castNumberToNumber

      case (BrioDoubleKey, BrioByteKey) => castNumberToNumber
      case (BrioDoubleKey, BrioShortKey) => castNumberToNumber
      case (BrioDoubleKey, BrioIntegerKey) => castNumberToNumber
      case (BrioDoubleKey, BrioLongKey) => castNumberToNumber

      case _ =>
        assertIncompatible
    }

  }

  private
  def assertIncompatible: String = {
    throw FeltException(expression.location,
      s"'${expression.normalizedSource.condensed}' cast types not compatible ${expression.feltType.valueTypeAsFelt} -> ${typeDeclaration.feltType.valueTypeAsFelt}")
  }

}
