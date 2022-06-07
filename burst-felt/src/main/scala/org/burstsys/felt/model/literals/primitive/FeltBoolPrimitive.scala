/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals.primitive

import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.types.FeltType

/**
 * A literal (static) value expression that reduces to a boolean value within the felt model
 */
trait FeltBoolPrimitive extends FeltPrimitive with FeltBoolExpr {

  final override val nodeName = "felt-bool-atom"

  /**
   * the boolean/logical value
   */
  def value: Boolean

  final override def generateSourceValue: String = value.toString

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    feltType = FeltType.valScal[Boolean]
    this
  }

}
