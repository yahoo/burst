/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.parameter.ref.mutable

import org.burstsys.felt.model.mutables.valarr.{FeltMutableValArr, FeltMutableValArrProv}
import org.burstsys.felt.model.tree.code.{C1, FeltCode, FeltCodeCursor, I1}
import org.burstsys.felt.model.variables.parameter.ref.FeltParamRef

import scala.language.postfixOps

/**
 *
 * @see [[FeltMutableValArr]]
 */
trait FeltParamValArr {

  self: FeltParamRef =>

  private
  lazy val binding: FeltMutableValArrProv = global.binding.mutables.valarr

  final
  def genValArrDecl(implicit cursor: FeltCodeCursor): FeltCode = {

    s"""|
        |${C1(s"declare value array parameter $varName")}
        |${I1}""".stripMargin
  }


  final
  def genValArrPrepare(implicit cursor: FeltCodeCursor): FeltCode = {

    s"""|
        |${C1(s"declare value array parameter $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValArrRelease(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"declare value array parameter $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValArrRead(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

}
