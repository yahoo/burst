/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.functions

import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, I2, M}
import org.burstsys.felt.model.types.FeltType

object FeltBrioIsFirstFunc {
  final val functionName: String = "isFirst"
}

/**
 *
 */
trait FeltBrioIsFirstFunc extends FeltBrioFunction with FeltBoolExpr {

  final override val nodeName = "felt-brio-is-first-func"

  final override val usage: String =
    s"""
       |usage: $functionName(<path_to_reference_vector>):boolean
     """.stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    parameters.foreach(_.resolveTypes)
    feltType = FeltType.valScal(parameters.head.feltType.keyType)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    val brioRef = parameterAsReferenceOrThrow[FeltBrioStdRef](0, "brio-path")
    val node = brioRef.refDecl
    s"""|
        |$I{ // ${M(this)}
        |$I2${cursor.callScope.scopeVal} = ${latticeVectorIsFirstViaSweepRuntime(node.brioNode.pathName)}
        |$I2${cursor.callScope.scopeNull} = false;
        |$I}""".stripMargin
  }

}
