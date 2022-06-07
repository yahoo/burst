/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate

import org.burstsys.felt.model.tree.code._
import org.burstsys.vitals.errors.VitalsException

import scala.language.postfixOps

trait FeltCubeSweepMetaGen extends FeltCubeSweepGenerator {

  self: FeltCubeSweepGeneratorContext =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def genSwpCollectorMetadata(implicit cursor: FeltCodeCursor): FeltCode = {
    validateCubeLimits()
    s"""|$generateCollectorBuilderDeclarations""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final
  def validateCubeLimits(): Unit = {
    analysis.cubes.foreach {
      cube =>
        if (cube.limit.isEmpty) throw VitalsException(s"cube ${cube.cubeName} has no limit specified!")
    }
  }

  private final
  def generateCollectorBuilderDeclarations(implicit cursor: FeltCodeCursor): FeltCode = {
    analysis.cubes.map {
      cube =>
        s"""|
            |${C(s"cube builder for '${cube.cubeName}'")}
            |${I}val ${cubeBuilderVariable(cube.cubeName)}:${binding.collectors.cubes.builderClassName} = ${cube.generateDeclaration(cursor indentRight)}""".stripMargin
    }.mkString
  }

}
