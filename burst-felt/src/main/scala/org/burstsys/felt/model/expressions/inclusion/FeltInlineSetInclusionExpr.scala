/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.inclusion

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.tree._
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * an expression determining if a source value is in the set defined by the members expressions
 * ===OPTIMIZATIONS===
 * <ol>
 * <li> </li>
 * </ol>
 */
trait FeltInlineSetInclusionExpr extends FeltInclusionExpr {

  final override val nodeName = "felt-inline-set-inclusion-expr"

  /**
   * the source data to test for inclusion
   *
   * @return
   */
  def value: FeltExpression

  /**
   * the set members to test against
   *
   * @return
   */
  def members: Array[FeltExpression]

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
    rule(this) ++ value.treeApply(rule) ++ members.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = members ++ value.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExpression =
    reduceToLiteral match {
      case Some(l) => l
      case None => new FeltInlineSetInclusionExpr {
        sync(FeltInlineSetInclusionExpr.this)
        final override val value: FeltExpression = FeltInlineSetInclusionExpr.this.value.reduceStatics.resolveTypes
        final override val invert: Boolean = FeltInlineSetInclusionExpr.this.invert
        final override val members: Array[FeltExpression] = FeltInlineSetInclusionExpr.this.members.map(_.reduceStatics.resolveTypes)
        final override val location: FeltLocation = FeltInlineSetInclusionExpr.this.location
      }
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def canInferTypes: Boolean = members.forall(_.canInferTypes)

  final override
  def resolveTypes: this.type = {
    value.resolveTypes
    members.resolveTypes()
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
    s"${value.normalizedSource} ${if (invert) "not" else ""} in (${members.map(_.normalizedSource).mkString(", ")})"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    val valueCursor = cursor scopeDown
    val memberCursors = members map (m => cursor scopeDown)

    def valueCode(expression: FeltExpression)(implicit cursor: FeltCodeCursor): FeltCode =
      s"""|${I}var ${cursor.callScope.scopeNull}:Boolean = false; var ${cursor.callScope.scopeVal}:${expression.feltType.valueTypeAsCode} = ${expression.feltType.valueDefaultAsCode}; // $nodeName-VALUE
          |${expression.generateExpression(cursor)}""".stripMargin

    def memberCode(i: Int, expression: FeltExpression)(implicit cursor: FeltCodeCursor): FeltCode =
      s"""|
          |${I}var ${cursor.callScope.scopeNull}:Boolean = false; var ${cursor.callScope.scopeVal}:${expression.feltType.valueTypeAsCode} = ${expression.feltType.valueDefaultAsCode}; // $nodeName-MEMBER-$i
          |${expression.generateExpression(cursor)}""".stripMargin

    def membersCode: String =
      (for (i <- members.indices) yield {
        memberCode(i, members(i))(memberCursors(i))
      }).toArray.mkString

    def membersNull: String = members.indices.map {
      i =>
        s"""|
            |$I2${memberCursors(i).callScope.scopeNull}""".stripMargin

    }.mkString(" || ")

    val equalityOp = if (invert) "!=" else "=="

    val combineOp = if (invert) "&&" else "||"

    def membersValue: String = members.indices.map {
      i =>
        s"""|
            |$I2(${valueCursor.callScope.scopeVal} $equalityOp ${memberCursors(i).callScope.scopeVal})""".stripMargin
    }.mkString(s" $combineOp ")

    val s =
      s"""|
          |${T(this)}
          |${valueCode(value)(valueCursor)}$membersCode
          |${I}if( ${valueCursor.callScope.scopeNull}|| $membersNull) { ${cursor.callScope.scopeNull} = true; } else {
          |$I2${cursor.callScope.scopeVal} = $membersValue;
          |$I}""".stripMargin
    s

  }

}
