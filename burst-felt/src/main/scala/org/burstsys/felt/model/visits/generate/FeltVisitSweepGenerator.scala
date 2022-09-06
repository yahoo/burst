/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits.generate

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.sweep.splice.{FeltSplice, FeltSplicer}
import org.burstsys.felt.model.sweep.symbols.skipVisitPathSym
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3}
import org.burstsys.felt.model.tree.{FeltGlobal, FeltNode}
import org.burstsys.felt.model.visits.FeltVisitRules
import org.burstsys.vitals.strings.VitalsGeneratingArray

/**
 * sweep code generation for artifacts related to 'visits'
 */
trait FeltVisitSweepGenerator extends FeltSweepGenerator with FeltSplicer {


  /**
   * generate visit sweep code
   *
   * @param node
   * @param cursor
   * @return
   */
  def genSwpSkipVisits(node: FeltNode)(implicit cursor: FeltCodeCursor): FeltCode

  /**
   * generate a dispatch table for processing dynamic relations during traversal
   *
   * @param cursor
   * @return
   */
  def genSwpDynamicRelations(implicit cursor: FeltCodeCursor): FeltCode

  /**
   * whatever needs to be generated into runtime class
   * to support dynamic relations
   *
   * @param cursor
   * @return
   */
  def generateRtDynamicVisitDecls(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltVisitSweepGenerator {
  def apply(analysis: FeltAnalysisDecl): FeltVisitSweepGenerator =
    FeltVisitSweepGeneratorContext(analysis: FeltAnalysisDecl)
}

private final case
class FeltVisitSweepGeneratorContext(analysis: FeltAnalysisDecl) extends FeltVisitSweepGenerator {

  override val global: FeltGlobal = analysis.global

  private val dynamicSplicer = FeltDynamicVisitSplicer(analysis)
  private val staticSplicer = FeltStaticVisitSplicer(analysis)

  override def generateRtDynamicVisitDecls(implicit cursor: FeltCodeCursor): FeltCode =
    dynamicSplicer.generateRtDynamicVisitDecls

  override def genSwpDynamicRelations(implicit cursor: FeltCodeCursor): FeltCode =
    dynamicSplicer.genSwpDynamicRelations

  override def collectSplices: Array[FeltSplice] =
    dynamicSplicer.collect.allSplices ++ staticSplicer.collect.allSplices

  override
  def genSwpSkipVisits(node: FeltNode)(implicit cursor: FeltCodeCursor): FeltCode = {

    def cases(implicit cursor: FeltCodeCursor): FeltCode = {
      node.staticVisits.map {
        branch =>
          s"""|
              |${I}case ${branch.pathKey} => false  // '${branch.pathName}'""".stripMargin
      }.stringify
    }

    s"""|
        |${C(s"which paths to visit and which to skip")}
        |$I@inline override
        |${I}def $skipVisitPathSym(pathKey: Int): Boolean = {
        |${I2}pathKey match {${cases(cursor indentRight 2)}
        |${I3}case _ => true
        |$I2}
        |$I}""".stripMargin
  }

}
