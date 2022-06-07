/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.flow.pattern

import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.vitals.strings._

import scala.language.postfixOps


/**
 * code generation semantics for [[FeltMatchExpr]]
 */
trait FeltMatchGen {

  self: FeltMatchExpr =>

  lazy val name = s"PAT-MATCH '$feltType'"

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$I${path.generateExpression} match {${generateCases(cases)(cursor indentRight)}${generateDefault(default)(cursor indentRight)}$I}""".stripMargin

  private
  def generateCases(cases: Array[FeltMatchCase])(implicit cursor: FeltCodeCursor): FeltCode = {
    if (cases.nonEmpty)
      s"\n${cases.map(_.generateExpression).stringify.singleLineEnd}"
    else ""
  }

  private
  def generateDefault(default: Option[FeltMatchDefault])(implicit cursor: FeltCodeCursor): FeltCode = {
    default match {
      case None => ""
      case Some(e) => s"${e.generateExpression.singleLineEnd}"
    }
  }

}
