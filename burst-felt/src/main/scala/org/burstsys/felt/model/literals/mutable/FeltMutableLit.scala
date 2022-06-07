/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals.mutable

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.tree.FeltNode

import scala.reflect.ClassTag

/**
 * A type of [[FeltLiteral]] that represents a mutable (value collection).
 */
trait FeltMutableLit[Member <: FeltExpression] extends FeltLiteral {

  /**
   * the expressions that make up the ''members'' of this collection
   *
   * @return
   */
  def members: Array[Member]

  def validate(): Unit = {
    members.foreach(m => m.reduceToLiteral.getOrElse(throw FeltException(m, s"value collection members must statically reduce to literals")))
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ members.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = members

}
