/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep.runtime

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.generate.FeltCollectorSweepGenerator
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.control.generate.FeltControlSweepGenerator
import org.burstsys.felt.model.sweep.lexicon.FeltLexiconSweepGenerator
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.variables.FeltVarSwpGenerator
import org.burstsys.felt.model.visits.generate.FeltVisitSweepGenerator
import org.burstsys.vitals.strings.VitalsGeneratingArray

import scala.language.postfixOps

/**
 * sweep runtime code generation
 */
trait FeltRuntimeSweepGenerator extends FeltSweepGenerator {

  /**
   * the analysis being generated
   *
   * @return
   */
  def analysis: FeltAnalysisDecl

  /**
   * generate the sweep runtime constructor
   *
   * @param cursor
   * @return
   */
  def genSwpRuntime(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltRuntimeSweepGenerator {
  def apply(analysis: FeltAnalysisDecl, collectors: FeltCollectorSweepGenerator, visits: FeltVisitSweepGenerator): FeltRuntimeSweepGenerator =
    FeltRuntimeSweepGeneratorContext(analysis: FeltAnalysisDecl, collectors: FeltCollectorSweepGenerator, visits: FeltVisitSweepGenerator)
}

private final case
class FeltRuntimeSweepGeneratorContext(
                                        analysis: FeltAnalysisDecl,
                                        collectors: FeltCollectorSweepGenerator,
                                        visits: FeltVisitSweepGenerator) extends FeltRuntimeSweepGenerator {

  override val global: FeltGlobal = analysis.global

  private val feltTravelerRtClassName: String = runtimeTravelerClassName(global)

  private val variables = FeltVarSwpGenerator(analysis)
  private val lexicon = FeltLexiconSweepGenerator(analysis)
  private val controls = FeltControlSweepGenerator(analysis)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def genSwpRuntime(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$genRtClass
        |${C("generated runtime constructor")}
        |${I}override def newRuntime(call:$feltInvocationClass):$feltRuntimeClass  = {
        |${I2}new ${global.treeGuid}(call)
        |$I}""".stripMargin


  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // internals
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  def genRtClass(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |${C("generated runtime class")}
        |${I}private class ${global.treeGuid}(call:$feltInvocationClass)
        |${I2}extends ${binding.sweepRuntimeClass: String}(call) with $feltTravelerRtClassName {
        |${genRtDeclBlk(cursor indentRight 1)}
        |${genRtPrepareBlk(cursor indentRight 1)}
        |${genRtReleaseBlk(cursor indentRight 1)}
        |${collectors.genRtCollectorBlk(cursor indentRight 1)}
        |${visits.generateRtDynamicVisitDecls(cursor indentRight)}
        |${controls.generateRtCtrlVerbMethods(cursor indentRight)}
        |$I}""".stripMargin

  private
  def genRtDeclBlk(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${variables.generateRtParameterDecls}
        |${variables.generateRtGlobalVarDecls}
        |${lexicon.generateRtLexDecls}
        |${controls.generateRtCtrlVerbDecls}""".stripMargin
  }

  /**
   * the generated prepare is a method in the generated runtime that is called ''before'' each
   * [[org.burstsys.brio.blob.BrioBlob]] traversal
   *
   * @param cursor
   * @return
   */
  private
  def genRtPrepareBlk(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C("prepare method (called before each sweep)")}
        |$I@inline override
        |${I}def generatedPrepare(blob:  $brioBlobClass): $feltRuntimeClass = {
        |${I2}val rt = this; // needed for var initialization
        |${I2}val dictionary = blob.dictionary;
        |${analysis.global.lexicon.genRtLexPrepareBlk(cursor indentRight)}
        |${genRtParmVarsPrepareBlk(cursor indentRight)}
        |${genRtGlobVarsPrepareBlk(cursor indentRight)}
        |${controls.generateRtPrepareBlk(cursor indentRight)}
        |${I2}this
        |$I}""".stripMargin
  }

  /**
   * the generated release is a method in the generated runtime that is called ''after'' each
   * [[org.burstsys.brio.blob.BrioBlob]]  traversal
   *
   * @param cursor
   * @return
   */
  private
  def genRtReleaseBlk(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C("release method (called after each sweep)")}
        |$I@inline override
        |${I}def generatedRelease: $feltRuntimeClass = {
        |${genRtParmVarsReleaseBlk(cursor indentRight)}
        |${I2}this
        |$I}""".stripMargin
  }

  private
  def genRtGlobVarsPrepareBlk(implicit cursor: FeltCodeCursor): FeltCode = {
    val globals = analysis.globalVariables.map {
      variable =>
        s"""|
            |${variable.generatePrepare}""".stripMargin
    }.stringify
    s"""|
        |${C("global vars prepare")}$globals""".stripMargin
  }

  private
  def genRtParmVarsPrepareBlk(implicit cursor: FeltCodeCursor): FeltCode = {
    val parameters = analysis.parameters.map {
      parameter =>
        s"""|
            |${parameter.generatePrepare}""".stripMargin
    }.stringify

    s"""|
        |${C("parameter vals prepare")}$parameters""".stripMargin

  }

  private
  def genRtParmVarsReleaseBlk(implicit cursor: FeltCodeCursor): FeltCode = {
    val parameters = analysis.parameters.map {
      parameter =>
        s"""|
            |${parameter.generateRelease}""".stripMargin
    }.stringify

    s"""|
        |${C("parameter vals release")}$parameters""".stripMargin

  }

}
