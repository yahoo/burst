/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.types

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}


/**
 * the set of types that can be declared brio/scala primitive atomic types
 */
trait FeltPrimTypeDecl extends FeltTypeDecl {

  final override val nodeName = "felt-prim-type-decl"

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
    feltType = FeltType.valScal(valueType)
    this
  }

  final override
  def inferInitializer(initializer: FeltExpression): FeltType = {
    /*
        if(initializer.feltType != feltType)
          throw FeltException(location, s"initializer type mismatch $this")
    */
    feltType
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateType(implicit cursor: FeltCodeCursor): FeltCode = feltType.valueTypeAsCode

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = s"${feltType.valueTypeAsFelt}"

}
