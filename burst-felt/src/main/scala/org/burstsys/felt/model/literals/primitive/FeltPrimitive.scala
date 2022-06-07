/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals.primitive

import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}

import scala.reflect.ClassTag

/**
 * A literal (static) value expression that reduces to a bytecode primitive
 * (atomic simple datatype)
 */
trait FeltPrimitive extends FeltLiteral {

  /**
   * the underlying static value
   *
   * @return
   */
  def value: Any

  /**
   * code generate an appropriate literal ''value'' representation. This is used in
   * various places including normalized intermediate '''source''' forms as well as
   * final '''code''' generation (see `generateCodeValue()`)
   * in many cases.
   *
   * @return
   */
  def generateSourceValue: String

  /**
   * generate a sweep time generated code value for a literal. This default is
   * to be the same as `generateSourceValue()`. Minimally this is to allow for lexicon
   * string references to be replaced by dictionary keys but may be useful for other
   * tricks of the trade.
   *
   * @return
   */
  def generateCodeValue: String = generateSourceValue // default is they are the same

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceToLiteral: Option[FeltLiteral] =
    Some(this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = $generateCodeValue; // FELT-${feltType.valueTypeAsFelt.toUpperCase}-ATOM """.stripMargin
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = generateSourceValue

}
