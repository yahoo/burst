/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals.mutable

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.FeltType

import scala.reflect.ClassTag

/**
 * A literal (static) value expression that reduces to a value map association within the felt model. Used
 * as part of [[FeltValMapLit]] declarations.
 */
trait FeltAssociation extends FeltExpression {

  final override val nodeName = "felt-assoc"

  /**
   * the ''key'' to lookup the ''value''
   *
   * @return
   */
  def key: FeltExpression

  /**
   * the ''values'' looked up by the ''key''
   *
   * @return
   */
  def value: FeltExpression

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ key.treeApply(rule) ++ value.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = Array(key, value)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltAssociation = new FeltAssociation {
    sync(FeltAssociation.this)
    final override val key: FeltExpression = FeltAssociation.this.key.reduceStatics.resolveTypes
    final override val value: FeltExpression = FeltAssociation.this.value.reduceStatics.resolveTypes
    final override val location: FeltLocation = FeltAssociation.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override def canInferTypes: Boolean = key.canInferTypes && value.canInferTypes

  final override
  def resolveTypes: this.type = {
    key.resolveTypes
    value.resolveTypes
    feltType = FeltType.combine(key.feltType, value.feltType)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |???""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"${key.normalizedSource} -> ${value.normalizedSource}"

}
