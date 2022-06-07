/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control.generate

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, FeltNoCode, I, I2}

/**
 *
 */
trait FeltControlSweepGenerator extends FeltSweepGenerator {

  /**
   *
   * @param cursor
   * @return
   */
  def generateRtCtrlVerbDecls(implicit cursor: FeltCodeCursor): FeltCode

  /**
   *
   * @param cursor
   * @return
   */
  def generateRtPrepareBlk(implicit cursor: FeltCodeCursor): FeltCode

  /**
   *
   * @param cursor
   * @return
   */
  def generateRtCtrlVerbMethods(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltControlSweepGenerator {
  def apply(analysis: FeltAnalysisDecl): FeltControlSweepGenerator = FeltControlSweepGeneratorContext(analysis: FeltAnalysisDecl)
}

private final case
class FeltControlSweepGeneratorContext(analysis: FeltAnalysisDecl) extends FeltControlSweepGenerator {

  override val global: FeltGlobal = analysis.global

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * generate declaration block into sweep runtime
   *
   * @param cursor
   * @return
   */
  override
  def generateRtCtrlVerbDecls(implicit cursor: FeltCodeCursor): FeltCode = {
    val verbs = analysis.frames.map(_.frameName).map {
      frameName =>
        s"""|
            |$I${controlRelationScopeVarDecl(frameName)}
            |$I${controlMemberScopeVarDecl(frameName)}
            |$I${controlDiscardScopeVarDecl(frameName)}""".stripMargin

    }.mkString
    s"""|
        |${C("control verbs declarations...")}$verbs""".stripMargin
  }

  /**
   * generate prepare block into sweep runtime
   *
   * @param cursor
   * @return
   */
  override
  def generateRtPrepareBlk(implicit cursor: FeltCodeCursor): FeltCode = {
    val verbs = analysis.frames.map(_.frameName).map {
      frameName =>
        s"""|
            |$I${controlRelationScopeValue(frameName)} = -1;    // NO RELATION CONTROL VERB PATH CURRENTLY ACTIVE
            |$I${controlMemberScopeValue(frameName)} = -1;      // NO MEMBER CONTROL VERB PATH CURRENTLY ACTIVE
            |$I${controlDiscardScopeValue(frameName)} = false;  // NO CONTROL VERB DISCARD CURRENTLY ACTIVE """.stripMargin

    }.mkString
    s"""|
        |${C("control verbs prepare...")}$verbs""".stripMargin
  }

  /**
   * generate special verb methods into sweep runtime
   *
   * @param cursor
   * @return
   */
  override
  def generateRtCtrlVerbMethods(implicit cursor: FeltCodeCursor): FeltCode = {
    val controlMemberCode = genRtSkipCtrlMemberPath
    val controlRelationCode = genRtSkipCtrlRelationPath
    if (controlMemberCode.isEmpty && controlRelationCode.isEmpty)
      s"""|
          |${C("no control verb runtime methods...")}""".stripMargin
    else
      s"""|
          |${C("control verb runtime methods...")}$controlMemberCode$controlRelationCode""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * member skip method
   *
   * @param cursor
   * @return
   */
  private
  def genRtSkipCtrlMemberPath(implicit cursor: FeltCodeCursor): FeltCode = {
    val frames = analysis.controlVerbs.map(_.frame.frameName).distinct
    if (frames.isEmpty) return FeltNoCode
    val predicate = if (frames.isEmpty) "false;" else frames.map {
      frameName =>
        s"$feltSchemaSym.visitInScope(visitPath, ${controlMemberScopeName(frameName)})"
    }.mkString(" && ")
    s"""|
        |$I@inline override
        |${I}def skipControlMemberPath(visitPath: Int): Boolean = {
        |${I2}$predicate
        |$I}""".stripMargin
  }

  /**
   * relation skip method
   *
   * @param cursor
   * @return
   */
  private
  def genRtSkipCtrlRelationPath(implicit cursor: FeltCodeCursor): FeltCode = {
    val frames = analysis.controlVerbs.map(_.frame.frameName).distinct
    if (frames.isEmpty) return FeltNoCode
    val predicate = if (frames.isEmpty) "false;" else frames.map {
      frameName =>
        s"$feltSchemaSym.visitInScope(visitPath, ${controlRelationScopeName(frameName)})"
    }.mkString(" && ")
    s"""|
        |$I@inline override
        |${I}def skipControlRelationPath(visitPath: Int): Boolean = {
        |${I2}$predicate
        |$I}""".stripMargin
  }

}
