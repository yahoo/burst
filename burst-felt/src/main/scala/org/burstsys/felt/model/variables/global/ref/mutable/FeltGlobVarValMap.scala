/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.global.ref.mutable

import org.burstsys.felt.model.mutables.valmap.FeltMutableValMap
import org.burstsys.felt.model.tree.code.{C1, FeltCode, FeltCodeCursor, I1}
import org.burstsys.felt.model.variables.global.ref.FeltGlobVarRef

import scala.language.postfixOps

/**
 *
 * @see [[FeltMutableValMap]]
 */
trait FeltGlobVarValMap {

  self: FeltGlobVarRef =>

  final
  def genValMapDecl(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"declare value map global var $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValMapPrep(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"prepare value map global var $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValMapWrite(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

  final
  def genValMapRead(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

}
