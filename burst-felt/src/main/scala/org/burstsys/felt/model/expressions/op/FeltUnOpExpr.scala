/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.op

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}

import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * Base Type for any expressions of the form  'op' 'expression' (unary operations)
 */
trait FeltUnOpExpr extends FeltOpExpr {

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
  def op: FeltUnaryOp

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ rhs.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = rhs.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * generic code generation for unary operations in FELT AST expressions
   *
   * @param tag
   * @param cursor
   * @return
   */
  final
  def scopeUnaryOp(tag: String)(implicit cursor: FeltCodeCursor): FeltCode = {

    val rhsCursor = cursor indentRight 1 scopeDown

    s"""|
        |${I}var ${rhsCursor.callScope.scopeNull}:Boolean = false; var ${rhsCursor.callScope.scopeVal}:${rhs.feltType.valueTypeAsCode} = ${rhs.feltType.valueDefaultAsCode}; // $tag-RHS DECL
        |${rhs.generateExpression(rhsCursor)}
        |${I}if( ${rhsCursor.callScope.scopeNull} ) { ${cursor.callScope.scopeNull} = true } else { ${cursor.callScope.scopeVal} = ${op.symbol} ${rhsCursor.callScope.scopeVal} } // $tag-OP""".stripMargin
  }

}
