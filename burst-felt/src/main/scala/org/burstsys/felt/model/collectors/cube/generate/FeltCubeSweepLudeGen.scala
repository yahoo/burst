/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.sweep.splice.{FeltPlacement, FeltVectorAfterPlace, FeltVectorBeforePlace}
import org.burstsys.felt.model.tree.code._
import org.burstsys.vitals.strings.VitalsGeneratingArray

trait FeltCubeSweepLudeGen extends FeltCubeSweepGenerator {

  self: FeltCubeSweepGeneratorContext =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateCollectorPreludesForPlace(pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = {
    analysis.cubes.map(generateCollectorPreludesForPlace(_, pathName, placement)).stringify
  }

  final override
  def generateCollectorPostludesForPlace(pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = {
    analysis.cubes.map(generateCollectorPostludesForPlace(_, pathName, placement)).stringify
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final
  def tmpVar(pathName: BrioPathName, placement: FeltPlacement): String =
    s"cube_ptr_swap_${pathName.replace(".", "_")}_$placement"

  private final
  def generateCollectorPreludesForPlace(cube: FeltCubeDecl, pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = {
    placement match {
      case FeltVectorBeforePlace | FeltVectorAfterPlace =>
        val space = FeltStaticCubeSpace(global, cube.cubeName)
        s"""|
            |${C(s"cube-prelude($pathName, $placement)")}
            |${I}val ${tmpVar(pathName, placement)} = ${space.currentInstCube(pathName)};
            |$I${space.currentInstCube(pathName)} = ${space.currentRelCube(pathName)};
            |""".stripMargin
      case _ => FeltNoCode
    }
  }

  private final
  def generateCollectorPostludesForPlace(cube: FeltCubeDecl, pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = {
    placement match {
      case FeltVectorBeforePlace | FeltVectorAfterPlace =>
        val space = FeltStaticCubeSpace(global, cube.cubeName)
        s"""|
            |${C(s"cube-postlude($pathName, $placement)")}
            |$I${space.currentInstCube(pathName)} = ${tmpVar(pathName, placement)};""".stripMargin
      case _ => FeltNoCode
    }
  }

}
