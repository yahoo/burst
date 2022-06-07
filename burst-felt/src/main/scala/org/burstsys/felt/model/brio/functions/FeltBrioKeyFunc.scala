/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.functions

import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.latticeValueMapKeyViaSweepRuntime
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, I2, M}
import org.burstsys.felt.model.types.FeltType

object FeltBrioKeyFunc {
  final val functionName: String = "key"
}

/**
 * an expression that returns the ''key'' for a relationship provided in the first parameter that has
 * a value provided in the second parameter. Generally this is a map function i.e. given a value, find the map's key.
 */
trait FeltBrioKeyFunc extends FeltBrioFunction {

  final override val nodeName = "felt-brio-key-func"

  final override val usage: String =
    s"""
       |usage: $functionName(<path_to_value_map>, value_expr+):boolean
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
  // code generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    val brioRef = parameterAsReferenceOrThrow[FeltBrioStdRef](0, "brio-path")
    val decl = brioRef.refDecl
    s"""|
        |$I{ // ${M(this)}
        |$I2${cursor.callScope.scopeVal} = ${latticeValueMapKeyViaSweepRuntime(decl.brioNode.pathName)}
        |$I}""".stripMargin
  }

}
