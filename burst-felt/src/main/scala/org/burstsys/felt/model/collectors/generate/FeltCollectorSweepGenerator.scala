/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.generate

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.{FeltCubeSweepGenerator, cubeBuilderVariable}
import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.route.generate.{FeltRouteSweepGenerator, routeBuilderVariable}
import org.burstsys.felt.model.collectors.runtime.FeltCollector
import org.burstsys.felt.model.collectors.shrub.decl.FeltShrubDecl
import org.burstsys.felt.model.collectors.shrub.generate.{FeltShrubSweepGenerator, shrubBuilderVariable}
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.collectors.tablet.generate.{FeltTabletSweepGenerator, tabletBuilderVariable}
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.sweep.splice.{FeltPlacement, FeltSplice, FeltSplicer}
import org.burstsys.felt.model.sweep.symbols.collectorBuilderClassName
import org.burstsys.felt.model.sweep.{FeltSweep, FeltSweepGenerator}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I}

import scala.language.postfixOps

/**
 * public entry points for collector related code generation
 */
trait FeltCollectorSweepGenerator extends FeltSweepGenerator with FeltSplicer {

  /**
   * this is 'data' i.e. it goes into the [[FeltRuntime]]
   * for [[FeltCollector]] types being
   * used in this analysis. Collectors are defined and referred to in libraries outside the Felt model...
   *
   * @param cursor
   * @return
   */
  def genRtCollectorBlk(implicit cursor: FeltCodeCursor): FeltCode

  /**
   * this is 'metadata' i.e. it goes into the [[FeltSweep]]
   * for [[FeltCollector]] types being
   * used in this analysis. Collectors are defined and referred to in libraries outside the Felt model...
   *
   * @param cursor
   * @return
   */
  def genSwpCollectorMetadata(implicit cursor: FeltCodeCursor): FeltCode

  /**
   * generate code for a path and place that is before all splices
   *
   * @param pathName
   * @param placement
   * @param cursor
   * @return
   */
  def generateCollectorPreludesForPlace(pathName: BrioPathName, placement: FeltPlacement)
                                       (implicit cursor: FeltCodeCursor): FeltCode

  /**
   * generate code for a path and place that is after all splices
   *
   * @param pathName
   * @param placement
   * @param cursor
   * @return
   */
  def generateCollectorPostludesForPlace(pathName: BrioPathName, placement: FeltPlacement)
                                        (implicit cursor: FeltCodeCursor): FeltCode
}

object FeltCollectorSweepGenerator {
  def apply(analysis: FeltAnalysisDecl): FeltCollectorSweepGenerator =
    FeltCollectorSweepGeneratorContext(analysis: FeltAnalysisDecl)
}

private final case
class FeltCollectorSweepGeneratorContext(analysis: FeltAnalysisDecl) extends FeltCollectorSweepGenerator {

  override val global: FeltGlobal = analysis.global

  override def collectSplices: Array[FeltSplice] = cubes.collectSplices ++
    tablets.collectSplices ++ routes.collectSplices ++ shrubs.collectSplices

  private val cubes = FeltCubeSweepGenerator(analysis)
  private val tablets = FeltTabletSweepGenerator(analysis)
  private val routes = FeltRouteSweepGenerator(analysis)
  private val shrubs = FeltShrubSweepGenerator(analysis)

  override
  def genRtCollectorBlk(implicit cursor: FeltCodeCursor): FeltCode =
    cubes.genRtCollectorBlk ++ tablets.genRtCollectorBlk ++
      routes.genRtCollectorBlk ++ shrubs.genRtCollectorBlk

  override
  def genSwpCollectorMetadata(implicit cursor: FeltCodeCursor): FeltCode =
    cubes.genSwpCollectorMetadata ++ tablets.genSwpCollectorMetadata ++
      routes.genSwpCollectorMetadata ++ shrubs.genSwpCollectorMetadata ++
      generateCollectorBuilderList

  override
  def generateCollectorPreludesForPlace(pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode =
    cubes.generateCollectorPreludesForPlace(pathName, placement) ++
      tablets.generateCollectorPreludesForPlace(pathName, placement) ++
      routes.generateCollectorPreludesForPlace(pathName, placement) ++
      shrubs.generateCollectorPreludesForPlace(pathName, placement)

  override
  def generateCollectorPostludesForPlace(pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode =
    cubes.generateCollectorPostludesForPlace(pathName, placement) ++
      tablets.generateCollectorPostludesForPlace(pathName, placement) ++
      routes.generateCollectorPostludesForPlace(pathName, placement) ++
      shrubs.generateCollectorPostludesForPlace(pathName, placement)


  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // COLLECTOR BUILDER LIST
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  def generateCollectorBuilderList(implicit cursor: FeltCodeCursor): FeltCode = {
    def mappings(implicit cursor: FeltCodeCursor): FeltCode =
      analysis.frames.map {
        frame =>
          frame.collectorDecl match {
            case cube: FeltCubeDecl =>
              s"$I${cubeBuilderVariable(cube.cubeName)}"
            case route: FeltRouteDecl =>
              s"$I${routeBuilderVariable(route.routeName)}"
            case tablet: FeltTabletDecl =>
              s"$I${tabletBuilderVariable(tablet.tabletName)}"
            case shrub: FeltShrubDecl =>
              s"$I${shrubBuilderVariable(shrub.shrubName)}"
            case collector =>
              s"${I}null /* '${collector.frame.frameName}' */"
          }
      }.mkString("\n", ",\n", "")

    s"""|
        |${C("collector builder list")}
        |$I@inline override
        |${I}val collectorBuilders:Array[$collectorBuilderClassName] = Array(
        |${mappings(cursor indentRight)}
        |$I)""".stripMargin
  }


}
