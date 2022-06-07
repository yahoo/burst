/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.inclusion

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * a boolean comparison expression testing range inclusion or exclusion
 */
trait FeltRangeInclusionExpr extends FeltInclusionExpr {

  final override val nodeName = "felt-range-inclusion-expr"

  /**
   * the source data to test for inclusion
   *
   * @return
   */
  def value: FeltExpression

  /**
   * the left hand expression (lower bound inclusive)
   *
   * @return
   */
  def lowerBound: FeltExpression

  /**
   * the right hand expression (upper bound inclusive)
   *
   * @return
   */
  def upperBound: FeltExpression

  /**
   * is this expression inverted?
   *
   * @return
   */
  def invert: Boolean

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ value.treeApply(rule) ++ lowerBound.treeApply(rule) ++ upperBound.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = value.asArray ++ lowerBound.asArray ++ upperBound.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExpression =
    reduceToLiteral match {
      case Some(l) => l
      case None => new FeltRangeInclusionExpr {
        sync(FeltRangeInclusionExpr.this)
        final override val invert: Boolean = FeltRangeInclusionExpr.this.invert
        final override val value: FeltExpression = FeltRangeInclusionExpr.this.value.reduceStatics.resolveTypes
        final override val lowerBound: FeltExpression = FeltRangeInclusionExpr.this.lowerBound.reduceStatics.resolveTypes
        final override val upperBound: FeltExpression = FeltRangeInclusionExpr.this.upperBound.reduceStatics.resolveTypes
        final override val location: FeltLocation = FeltRangeInclusionExpr.this.location
      }
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def canInferTypes: Boolean = lowerBound.canInferTypes

  final override
  def resolveTypes: this.type = {
    value.resolveTypes
    lowerBound.resolveTypes
    upperBound.resolveTypes
    feltType = FeltType.valScal[Boolean]
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceToLiteral: Option[FeltLiteral] = None

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"${value.normalizedSource} ${if (invert) "not" else ""} between (${lowerBound.normalizedSource} , ${upperBound.normalizedSource})"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {

    val valueCursor = cursor indentRight 1 scopeDown
    val lowerCursor = cursor indentRight 1 scopeDown
    val upperCursor = cursor indentRight 1 scopeDown

    val opCode = if (!invert)
      s"""${cursor.callScope.scopeVal} = ${valueCursor.callScope.scopeVal} >= ${lowerCursor.callScope.scopeVal} && ${valueCursor.callScope.scopeVal} <= ${upperCursor.callScope.scopeVal}""".stripMargin
    else
      s"""${cursor.callScope.scopeVal} = ${valueCursor.callScope.scopeVal} < ${lowerCursor.callScope.scopeVal}|| ${valueCursor.callScope.scopeVal} > ${upperCursor.callScope.scopeVal}""".stripMargin

    s"""|
        |${T(this)}
        |${I}var ${valueCursor.callScope.scopeNull}:Boolean = false; var ${valueCursor.callScope.scopeVal}:${value.feltType.valueTypeAsCode} = ${value.feltType.valueDefaultAsCode}; // $nodeName-SOURCE
        |${value.generateExpression(valueCursor)}
        |${I}var ${lowerCursor.callScope.scopeNull}:Boolean = false; var ${lowerCursor.callScope.scopeVal}:${lowerBound.feltType.valueTypeAsCode} = ${lowerBound.feltType.valueDefaultAsCode}; // $nodeName-LOWER
        |${lowerBound.generateExpression(lowerCursor)}
        |${I}var ${upperCursor.callScope.scopeNull}:Boolean = false; var ${upperCursor.callScope.scopeVal}:${upperBound.feltType.valueTypeAsCode} = ${upperBound.feltType.valueDefaultAsCode}; // $nodeName-UPPER
        |${upperBound.generateExpression(upperCursor)}
        |${I}if( ${valueCursor.callScope.scopeNull}|| ${lowerCursor.callScope.scopeNull}|| ${upperCursor.callScope.scopeNull} ) { ${cursor.callScope.scopeNull} = true } else {
        |$I2$opCode
        |$I}""".stripMargin
  }

}
