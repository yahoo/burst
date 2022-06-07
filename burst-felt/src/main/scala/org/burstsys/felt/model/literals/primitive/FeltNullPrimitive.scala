/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals.primitive

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}
import org.burstsys.felt.model.types.FeltType

/**
 * A literal (static) value expression that reduces to a null within the felt model
 */
trait FeltNullPrimitive extends FeltPrimitive with FeltBoolExpr {

  final override val nodeName = "felt-null-atom"

  /**
   * the null value (always null)
   */
  final val value: Any = null

  final override def generateSourceValue: String = "null"

  private[this]
  var wasCoerced: Boolean = false

  def coerce(t: FeltType): Unit = {
    if (wasCoerced && feltType != t)
      throw FeltException(location, s"$printSource: attempt to coerce NULL twice")
    feltType = t
    wasCoerced = true
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // static reduction
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override def reduceToNull: Option[FeltNullPrimitive] = Some(this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def resolveTypes: this.type = {
    if (!wasCoerced)
      feltType = FeltType.any
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I${cursor.callScope.scopeNull} = true; // FELT-NULL-ATOM """.stripMargin
  }

}
