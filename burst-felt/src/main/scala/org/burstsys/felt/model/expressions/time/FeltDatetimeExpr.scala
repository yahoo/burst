/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.time

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.literals.primitive.FeltFixPrimitive
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.types.FeltType
import org.joda.time.format.ISODateTimeFormat

import scala.reflect.ClassTag

/**
 * an expression that performs a ''cast'' from the type of a given expression to
 * another type (if compatible)
 */
trait FeltDatetimeExpr extends FeltExpression {

  final override val nodeName = "felt-datetime-expr"

  /**
   * the expression to be ''cast''
   *
   * @return
   */
  def spec: String

  final val usage: String =
    s"""|USAGE: datetime( <iso_date_string> ) -> <epoch_time>:long
        | return a long representing the current ms epoch time as specified by the iso 8601 string specification
        |""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = true

  final override
  def resolveTypes: this.type = {
    feltType = FeltType.valScal[Long]
    this
  }

  private[this]
  lazy val _ticks: Long = {
    val parser = ISODateTimeFormat.dateTimeParser()
    parser.parseDateTime(spec).getMillis
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceToLiteral: Option[FeltLiteral] = {
    Some(
      new FeltFixPrimitive {
        sync(FeltDatetimeExpr.this)
        final override val location = FeltDatetimeExpr.this.location
        final override val value: Long = _ticks
      }
    )
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExpression = {
    reduceToLiteral.get
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    s"""|${S}datetime("$spec") """.stripMargin
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

}
