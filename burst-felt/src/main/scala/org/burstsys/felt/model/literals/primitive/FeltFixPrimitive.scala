/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals.primitive

import org.burstsys.felt.model.types._

/**
 * A fixed literal covers all fixed precision integers e.g. byte, short, int, long
 */
trait FeltFixPrimitive extends FeltPrimitive {

  final override val nodeName = "felt-fix-atom"

  /**
   * the fixed value - always represented by a long datatype
   */
  def value: Long

  final override def generateSourceValue: String = {
    if (feltType == FeltType.valScal[Long] && (value > Integer.MAX_VALUE) || value < Integer.MIN_VALUE) {
      s"${value}L"
    } else s"$value"
  }

  private var wasCoerce: Boolean = false

  def coerce(t: FeltType): Unit = {
    feltType = t
    wasCoerce = true
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    if (wasCoerce) return this

    // HACKITY HACK HACK
    if (value < Byte.MaxValue && value > Byte.MinValue) feltType = FeltType.valScal[Byte]
    else if (value < Short.MaxValue && value > Short.MinValue) feltType = FeltType.valScal[Short]
    else if (value < Int.MaxValue && value > Int.MinValue) feltType = FeltType.valScal[Int]
    else feltType = FeltType.valScal[Long]
    this
  }

}

