/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.global.ref.mutable

import org.burstsys.felt.model.mutables.valset.FeltMutableValSet
import org.burstsys.felt.model.tree.code.{C1, FeltCode, FeltCodeCursor, I1}
import org.burstsys.felt.model.variables.global.ref.FeltGlobVarRef

import scala.language.postfixOps

/**
 *
 * @see [[FeltMutableValSet]]
 */
trait FeltGlobVarValSet {

  self: FeltGlobVarRef =>

  final
  def genValSetDecl(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"declare value set global var $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValSetPrep(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"prepare value set global var $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValSetWrite(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

  final
  def genValSetRead(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

}
