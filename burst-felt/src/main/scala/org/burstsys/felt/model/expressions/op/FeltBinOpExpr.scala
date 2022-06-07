/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.op

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.literals.primitive.FeltNullPrimitive
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * Base Type for any expressions of the form 'expression' 'op' 'expression' (binary operations)
 */
trait FeltBinOpExpr extends FeltOpExpr {

  /**
   * left hand side of the operation
   *
   * @return
   */
  def lhs: FeltExpression

  /**
   * right hand side of the operation
   *
   * @return
   */
  def rhs: FeltExpression

  /**
   *
   * @return
   */
  def op: FeltBinaryOp

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ lhs.treeApply(rule) ++ rhs.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = Array(lhs, rhs)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * generic code generation for binary operations in FELT AST expressions
   *
   * @param tag
   * @param cursor
   * @return
   */
  final
  def scopeBinaryOp(tag: String)(implicit cursor: FeltCodeCursor): FeltCode = {
    // TODO LEXICON special case for string lexical comparison???

    val lhsCursor = cursor indentRight 1 scopeDown
    val rhsCursor = cursor indentRight 1 scopeDown

    // TODO this is really ugly - perhaps ok but feels so wrong (these are not method calls at least)
    val coerce = (feltType.valueType, lhs.feltType.valueType, rhs.feltType.valueType) match {
      case (BrioByteKey, _, _) => ".toByte"
      case (BrioShortKey, _, _) => ".toShort"
      case (BrioIntegerKey, _, _) => ".toInt"
      case (BrioLongKey, _, _) => ".toLong"
      case _ => FeltNoCode
    }

    s"""|
        |${I}var ${lhsCursor.callScope.scopeNull}:Boolean = false; var ${lhsCursor.callScope.scopeVal}:${lhs.feltType.valueTypeAsCode} = ${lhs.feltType.valueDefaultAsCode}; // $tag-LHS DECL${lhs.generateExpression(lhsCursor)}
        |${I}var ${rhsCursor.callScope.scopeNull}:Boolean = false; var ${rhsCursor.callScope.scopeVal}:${rhs.feltType.valueTypeAsCode} = ${rhs.feltType.valueDefaultAsCode}; // $tag-RHS DECL${rhs.generateExpression(rhsCursor)}
        |${I}if( ${lhsCursor.callScope.scopeNull}|| ${rhsCursor.callScope.scopeNull} ) { ${cursor.callScope.scopeNull} = true } else { ${cursor.callScope.scopeVal} = (${lhsCursor.callScope.scopeVal} ${op.symbol} ${rhsCursor.callScope.scopeVal})$coerce } // $tag-OP""".stripMargin

  }

  /**
   * generic null semantics are if either side is null, then the expression is null
   *
   * @return
   */
  override
  def reduceToNull: Option[FeltNullPrimitive] = {
    if (lhs.reduceToNull.nonEmpty || rhs.reduceToNull.nonEmpty) return Some(
      new FeltNullPrimitive {
        sync(FeltBinOpExpr.this)

        override def location: FeltLocation = FeltBinOpExpr.this.location
      }
    )
    None
  }


}
