/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.types

import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.{BrioTypeKey, brioDataTypeNameFromKey}
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.mutables.valset.FeltMutableValSet
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

/**
 * A type declaration for a ''set'' of primitive value types
 *
 * @see [[FeltMutableValSet]]
 */
trait FeltSetTypeDecl extends FeltTypeDecl {

  final override val nodeName = "felt-set-type-decl"

  /**
   * the associated brio value type
   *
   * @return
   */
  def valueType: BrioTypeKey

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def canInferTypes: Boolean = true

  final override
  def resolveTypes: this.type = {
    feltType = FeltType.valSet(valueType)
    this
  }

  final override
  def inferInitializer(initializer: FeltExpression): FeltType = {
    feltType
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateType(implicit cursor: FeltCodeCursor): FeltCode =
    s"""scala.Array[${BrioTypes.scalaTypeFromBrioTypeKey(valueType)}]""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"set[${brioDataTypeNameFromKey(valueType).toLowerCase}]"

}
