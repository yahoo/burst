/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.global.ref.mutable

import org.burstsys.felt.model.mutables.valarr.FeltMutableValArr
import org.burstsys.felt.model.tree.code.{C1, FeltCode, FeltCodeCursor, I1}
import org.burstsys.felt.model.variables.global.ref.FeltGlobVarRef

import scala.language.postfixOps

/**
 *
 * @see [[FeltMutableValArr]]
 */
trait FeltGlobVarValArr {

  self: FeltGlobVarRef =>

  final
  def genValArrDecl(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"declare value array global var $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValArrPrep(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"prepare value array global var $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValArrWrite(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

  final
  def genValArrRead(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

}
