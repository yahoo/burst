/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.generate

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.generate.FeltCollectorSweepGenerator
import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.route.generate.splice.FeltRouteSplicer
import org.burstsys.felt.model.sweep.splice.{FeltPlacement, FeltSplice}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, FeltNoCode, I}
import org.burstsys.vitals.strings.VitalsGeneratingArray

/**
 *
 */
trait FeltRouteSweepGenerator extends FeltCollectorSweepGenerator {

}

object FeltRouteSweepGenerator {
  def apply(analysis: FeltAnalysisDecl): FeltRouteSweepGenerator =
    FeltRouteSweepGeneratorContext(analysis: FeltAnalysisDecl)
}

final case
class FeltRouteSweepGeneratorContext(analysis: FeltAnalysisDecl) extends AnyRef
  with FeltRouteSweepGenerator {

  override def global: FeltGlobal = analysis.global

  private def routes: Array[FeltRouteDecl] = analysis.routes

  private val splicer = FeltRouteSplicer(analysis)

  override def collectSplices: Array[FeltSplice] = splicer.collectSplices

  override
  def genRtCollectorBlk(implicit cursor: FeltCodeCursor): FeltCode = genRtRouteBlk

  override
  def genSwpCollectorMetadata(implicit cursor: FeltCodeCursor): FeltCode = genSwpRouteBuilder

  override
  def generateCollectorPreludesForPlace(pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode =
    FeltNoCode

  override
  def generateCollectorPostludesForPlace(pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode =
    FeltNoCode

  def genSwpRouteMetadata(implicit cursor: FeltCodeCursor): FeltCode = {
    if (routes.isEmpty) FeltNoCode else
      s"""|
          |${C(s"route builder(s)")}$genSwpRouteBuilder""".stripMargin
  }

  /**
   *
   * @param cursor
   * @return
   */
  def genRtRouteBlk(implicit cursor: FeltCodeCursor): FeltCode = {

    def route(route: FeltRouteDecl)(implicit cursor: FeltCodeCursor): FeltCode = {
      val reference = route.reference
      s"""|
          |${C(s"route variable(s) for '${route.routeName}'")}
          |${I}var ${reference.rootVariable} : ${binding.collectors.routes.collectorClassName} = _ ;
          |${I}var ${reference.instanceVariable} : ${binding.collectors.routes.collectorClassName} = _ ;
          |${I}var ${reference.stepIsFirstVariable} : Boolean = _ ;
          |${I}var ${reference.stepIsLastVariable} : Boolean = _ ;
          |${I}var ${reference.pathIsFirstVariable} : Boolean = _ ;
          |${I}var ${reference.pathIsLastVariable} : Boolean = _ ;
          |""".stripMargin
    }

    if (routes.isEmpty) FeltNoCode
    else
      s"""|
          |${C("route variable(s)")}
          |${routes.map(route).stringify}""".stripMargin
  }

  private
  def genSwpRouteBuilder(implicit cursor: FeltCodeCursor): FeltCode = {

    def route(route: FeltRouteDecl)(implicit cursor: FeltCodeCursor): FeltCode = {
      s"""|
          |${C(s"route builder for '${route.routeName}'")}
          |${I}val ${route.reference.builderVariable}:${binding.collectors.routes.builderClassName} = ${route.generateDeclaration} ; """.stripMargin
    }

    if (routes.isEmpty) FeltNoCode
    else
      s"""|
          |${C("route builder(s)")}
          |${routes.map(route).stringify}""".stripMargin
  }

}
