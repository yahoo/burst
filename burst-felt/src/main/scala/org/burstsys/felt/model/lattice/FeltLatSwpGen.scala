/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.generate.FeltCollectorSweepGenerator
import org.burstsys.felt.model.lattice.sweep._
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.sweep.splice.{FeltPlacement, _}
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3}
import org.burstsys.felt.model.visits.generate.FeltVisitSweepGenerator
import org.burstsys.vitals.strings.VitalsString

import scala.language.postfixOps

/**
 * code generation for the ''lattice'' (the brio object tree traversal machinery)
 */
trait FeltLatSwpGen extends FeltSweepGenerator {

  /**
   * current analysis being generated
   *
   * @return
   */
  def analysis: FeltAnalysisDecl

  /**
   * code generate calls to the collected splices
   *
   * @param cursor
   * @return
   */
  def genSwpStaticSpliceCalls(implicit cursor: FeltCodeCursor): FeltCode

  /**
   *
   * @param treeNodes
   * @param placement
   * @param cursor
   * @return
   */
  def generateSpliceCallForPlace(treeNodes: Array[BrioNode], placement: FeltPlacement)
                                (implicit cursor: FeltCodeCursor): FeltCode

  /**
   * code generate methods for the collected splices
   *
   * @param cursor
   * @return
   */
  def genSwpStaticSpliceBodies(implicit cursor: FeltCodeCursor): FeltCode

  def genSwpDynamicSpliceBodies(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltLatSwpGen {

  def apply(analysis: FeltAnalysisDecl,
            collectors: FeltCollectorSweepGenerator,
            visits: FeltVisitSweepGenerator): FeltLatSwpGen =
    FeltLatticeSweepGeneratorContext(analysis: FeltAnalysisDecl,
      collectors: FeltCollectorSweepGenerator,
      visits: FeltVisitSweepGenerator)

}

private[lattice] final case
class FeltLatticeSweepGeneratorContext(
                                        analysis: FeltAnalysisDecl,
                                        collectors: FeltCollectorSweepGenerator,
                                        visits: FeltVisitSweepGenerator
                                      ) extends FeltLatSwpGen
  with FeltLatRefScalSwpGen with FeltLatRefVecSwpGen with FeltLatValMapSwpGen with FeltLatValVecSwpGen
  with FeltLatSpliceSwpGen {

  override val global: FeltGlobal = analysis.global

  // initialize by collecting splices
  this ++= collectors.collectSplices
  this ++= visits.collectSplices

  override
  def genSwpStaticSpliceCalls(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${genSwpRootSplices(cursor indentRight)}
        |${genSwpRefScalSplices(cursor indentRight)}
        |${genSwpRefVecSplices(cursor indentRight)}
        |${genSwpValMapSplices(cursor indentRight)}
        |${genSwpValVecSplices(cursor indentRight)}
        |""".stripMargin.stripEmptyLines
  }

  override
  def genSwpDynamicSpliceBodies(implicit cursor: FeltCodeCursor): FeltCode = doSplices(cursor indentRight)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  def doSplices(implicit cursor: FeltCodeCursor): FeltCode = {
    if (dynamicSplices.isEmpty) return s"${C("no dynamic splices...")}"
    val splices = dynamicSplices.map {
      sm =>
        sm.generateSpliceMethodBody(cursor newIndent 1)
    }.mkString
    s"${C("dynamic splices...")}$splices"
  }

  private
  def genSwpRootSplices(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |${C("root splices")}
        |$generateRootSplice""".stripMargin

  private
  def generateRootSplice(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$I@inline override
        |${I}def rootSplice($schemaRuntimeSym: $feltRuntimeClass, path:Int, placement: Int): Unit = {
        |${sweepRuntimeClassVal(global)(cursor indentRight 1)}
        |${I2}placement match {
        |${I3}case ${FeltTraverseCommencePlace.key} => // $FeltTraverseCommencePlace
        |${generateSpliceCallForPlace(Array(brioSchema.rootNode), FeltTraverseCommencePlace)(cursor indentRight 3)}
        |${I3}case ${FeltTraverseCompletePlace.key} => // $FeltTraverseCompletePlace
        |${generateSpliceCallForPlace(Array(brioSchema.rootNode), FeltTraverseCompletePlace)(cursor indentRight 3)}
        |${I3}case _ =>
        |$I2}
        |$I}""".stripMargin

}
