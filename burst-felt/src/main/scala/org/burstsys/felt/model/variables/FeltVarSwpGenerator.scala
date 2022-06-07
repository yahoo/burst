/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I}
import org.burstsys.vitals.strings.VitalsGeneratingArray

/**
 * sweep code generation for global variables & parameters
 */
trait FeltVarSwpGenerator extends FeltSweepGenerator {

  /**
   * sweep code generation for global variables
   *
   * @param cursor
   * @return
   */
  def generateRtGlobalVarDecls(implicit cursor: FeltCodeCursor): FeltCode

  /**
   * sweep code generation for parameters
   *
   * @param cursor
   * @return
   */
  def generateRtParameterDecls(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltVarSwpGenerator {
  def apply(analysis: FeltAnalysisDecl): FeltVarSwpGenerator =
    FeltVarSwpGeneratorContext(analysis)
}

private final case
class FeltVarSwpGeneratorContext(analysis: FeltAnalysisDecl) extends FeltVarSwpGenerator {

  override val global: FeltGlobal = analysis.global

  override
  def generateRtGlobalVarDecls(implicit cursor: FeltCodeCursor): FeltCode = {
    val code = analysis.globalVariables.map {
      variable =>
        s"$I${variable.generateDeclaration}"
    }.stringify

    s"""|
        |${C("global variables declarations")}$code""".stripMargin
  }

  override
  def generateRtParameterDecls(implicit cursor: FeltCodeCursor): FeltCode = {
    val code = analysis.parameters.map {
      parameter =>
        s"""|
            |${parameter.generateDeclaration}""".stripMargin
    }.stringify

    s"""|
        |${C("analysis parameter declarations")}$code""".stripMargin
  }

}
