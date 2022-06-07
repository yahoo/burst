/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals.primitive

import org.burstsys.felt.model.types.FeltType

/**
 * A fixed literal covers all floating precision integers i.e. double
 */
trait FeltFltPrimitive extends FeltPrimitive {

  final override val nodeName = "felt-float-atom"

  /**
   *
   * @return
   */
  def value: Double

  final override def generateSourceValue: String = value.toString

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    feltType = FeltType.valScal[Double]
    this
  }

}
