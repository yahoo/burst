/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep.lexicon

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor}

/**
 * sweep code generation for [[FeltLexicon]]
 */
trait FeltLexiconSweepGenerator extends FeltSweepGenerator {

  /**
   * generate sweep runtime lexicon declarations
   *
   * @param cursor
   * @return
   */
  def generateRtLexDecls(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltLexiconSweepGenerator {
  def apply(analysis: FeltAnalysisDecl): FeltLexiconSweepGenerator = FeltLexiconSweepGeneratorContext(analysis)
}

private final case
class FeltLexiconSweepGeneratorContext(analysis: FeltAnalysisDecl) extends FeltLexiconSweepGenerator {

  override val global: FeltGlobal = analysis.global

  override
  def generateRtLexDecls(implicit cursor: FeltCodeCursor): FeltCode = {
    analysis.bindLexiconStrings
    s"""|
        |${C("lexicon declarations")}${analysis.global.lexicon.generateDeclaration}""".stripMargin
  }

}
