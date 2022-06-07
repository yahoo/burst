/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.analysis.generate

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.brio.reference._
import org.burstsys.felt.model.collectors.generate.FeltCollectorSweepGenerator
import org.burstsys.felt.model.lattice.FeltLatSwpGen
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.sweep.runtime.FeltRuntimeSweepGenerator
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.tree.{FeltGlobal, printFeatures}
import org.burstsys.felt.model.visits.generate.FeltVisitSweepGenerator
import org.burstsys.vitals.strings._

import scala.language.postfixOps

/**
 * generate the sweep and the sweep runtime for an analysis
 */
trait FeltAnalysisSweepGenerator extends FeltSweepGenerator {

  /**
   * the provided analysis tree root
   *
   * @return
   */
  def analysis: FeltAnalysisDecl

  /**
   * generate the sweep and the sweep runtime for an analysis
   *
   * @param cursor
   * @return
   */
  def generateSweep(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltAnalysisSweepGenerator {

  def apply(analysis: FeltAnalysisDecl): FeltAnalysisSweepGenerator =
    FeltAnalysisSweepGeneratorContext(analysis)

}

/**
 * Base felt sweep generation functions - bind to a concrete language implementation such as Hydra
 */
private final case
class FeltAnalysisSweepGeneratorContext(analysis: FeltAnalysisDecl) extends FeltAnalysisSweepGenerator {

  override val global: FeltGlobal = analysis.global

  private val collectors = FeltCollectorSweepGenerator(analysis)
  private val visits = FeltVisitSweepGenerator(analysis)
  private val lattice = FeltLatSwpGen(analysis, collectors, visits)
  private val runtime = FeltRuntimeSweepGenerator(analysis, collectors, visits)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def generateSweep(implicit cursor: FeltCodeCursor): FeltCode = {
    val features = printFeatures(
      (cursor.global.lexicon.enabled, "lexicon"),
      (cursor.global.features.ctrlVerbs, "control-verbs")
    )

    val sweepStaticSpliceCalls = lattice.genSwpStaticSpliceCalls
    val sweepStaticSpliceBodies = lattice.genSwpStaticSpliceBodies(cursor indentRight)
    val sweepDynamicRelations = visits.genSwpDynamicRelations(cursor indentRight)
    val sweepDynamicSpliceBodies = lattice.genSwpDynamicSpliceBodies

    // generate them first then place them in order in final assembly locations...
    val sweepApply = genSwpApplyMethod(cursor indentRight)
    val sweepSkipVisits = visits.genSwpSkipVisits(analysis)(cursor indentRight)
    val sweepSkipTunnels = genSwpSkipTunnels(analysis)(cursor indentRight)
    val sweepRuntime = runtime.genSwpRuntime(cursor indentRight)
    val sweepCollectorMetadata = collectors.genSwpCollectorMetadata(cursor indentRight)


    s"""|
        |${C(s"begin generated felt 'sweep' with $features")}
        |${I}final class ${analysis.sweepClassName} extends ${global.binding.sweepClass: String} {
        |${I2}override val $feltSchemaSym = new ${global.travelerClassName}
        |${I2}override val sweepName: String = "${analysis.analysisName}"
        |${I2}override val sweepClassName: $vitalsUidClass = "${analysis.sweepClassName}"
        |$sweepRuntime
        |$sweepApply
        |$sweepSkipVisits
        |$sweepSkipTunnels
        |$sweepCollectorMetadata
        |$sweepStaticSpliceCalls
        |$sweepStaticSpliceBodies
        |$sweepDynamicRelations
        |$sweepDynamicSpliceBodies
        |$I}
        |${C(s"end generated hydra felt sweep ${analysis.sweepClassName}")}
        |""".stripMargin.stripEmptyLines
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  def genSwpApplyMethod(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C(s"top level traversal entry point")}
        |$I@inline override
        |${I}def apply($schemaRuntimeSym: $feltRuntimeClass): Unit = {
        |$I2$feltSchemaSym($schemaRuntimeSym.asInstanceOf[${global.treeGuid}], this)
        |$I}""".stripMargin
  }

}
