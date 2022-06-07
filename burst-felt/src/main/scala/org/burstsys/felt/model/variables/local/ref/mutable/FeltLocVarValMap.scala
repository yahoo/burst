/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.local.ref.mutable

import org.burstsys.felt.model.mutables.valmap.FeltMutableValMap
import org.burstsys.felt.model.tree.code.{C1, FeltCode, FeltCodeCursor, I1}
import org.burstsys.felt.model.variables.local.ref.FeltLocVarRef

import scala.language.postfixOps

/**
 *
 * @see [[FeltMutableValMap]]
 */
trait FeltLocVarValMap {

  self: FeltLocVarRef =>

  final
  def genValMapDecl(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"declare value map local var $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValMapPrep(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"declare value map local var $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValMapRead(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

  final
  def genValMapWrite(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }
}
