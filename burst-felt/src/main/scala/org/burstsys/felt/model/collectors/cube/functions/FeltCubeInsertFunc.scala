/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.functions

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeRef
import org.burstsys.felt.model.collectors.cube.generate.FeltStaticCubeSpace
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.felt.model.types._

object FeltCubeInsertFunc {
  final val functionName: String = "insert"
}

trait FeltCubeInsertFunc extends FeltCubeFunction {

  final override val nodeName = "felt-cube-insert-func"

  final val functionName: String = FeltCubeInsertFunc.functionName

  final override val usage: String =
    s"""
       |usage: $functionName(cube) -> unit
       |  insert a row if it does not already exist for current dimension cursor key (not necessary if an aggregate
       |  is written after dimensions are asserted)
     """.stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    feltType = FeltType.unit
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    val ref = parameterAsReferenceOrThrow[FeltCubeRef](0, "cube reference")
    val ns = FeltStaticCubeSpace(global, ref.cubeName)
    s"""|
        |${T(this)}
        |$I${ns.currentInstCube}.writeDimension(${ns.cubeBuilderVar}, ${ns.currentInstCube})""".stripMargin
  }

}
