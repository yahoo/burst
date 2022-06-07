/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.local.ref.mutable

import org.burstsys.felt.model.mutables.valarr.FeltMutableValArr
import org.burstsys.felt.model.tree.code.{C1, FeltCode, FeltCodeCursor, I1}
import org.burstsys.felt.model.variables.local.ref.FeltLocVarRef

import scala.language.postfixOps

/**
 *
 * @see [[FeltMutableValArr]]
 */
trait FeltLocVarValArr {

  self: FeltLocVarRef =>

  final
  def genValArrDecl(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"declare value array local var $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValArrPrepare(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"prepare value array local var $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValArrRead(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

  final
  def genValArrWrite(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

}
