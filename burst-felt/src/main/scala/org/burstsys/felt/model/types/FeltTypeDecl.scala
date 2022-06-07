/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.types

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

import scala.reflect.ClassTag

/**
 * A type declaration for any simple or complex primitive value types
 */
trait FeltTypeDecl extends FeltNode {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * reduce/simplify this tree node and its descendants using statically available information
   *
   * @return
   */
  final
  def reduceStatics: FeltTypeDecl = this

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Generate executable code to be used in a Felt sweep.
   *
   * @param cursor
   * @return
   */
  def generateType(implicit cursor: FeltCodeCursor): FeltCode


  def inferInitializer(initializer: FeltExpression): FeltType

}
