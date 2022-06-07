/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.parameter.ref.mutable

import org.burstsys.felt.model.mutables.valmap.{FeltMutableValMap, FeltMutableValMapProv}
import org.burstsys.felt.model.tree.code.{C1, FeltCode, FeltCodeCursor, I1}
import org.burstsys.felt.model.variables.parameter.ref.FeltParamRef

import scala.language.postfixOps

/**
 *
 * @see [[FeltMutableValMap]]
 */
trait FeltParamValMap {

  self: FeltParamRef =>

  private
  lazy val binding: FeltMutableValMapProv = global.binding.mutables.valmap

  final
  def genValMapDecl(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"declare value map parameter $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValMapPrepare(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"declare value map parameter $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValMapRelease(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"declare value map parameter $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValMapRead(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

}
