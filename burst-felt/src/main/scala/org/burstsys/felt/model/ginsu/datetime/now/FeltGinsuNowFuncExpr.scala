/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu.datetime.now

import org.burstsys.felt.model.ginsu.FeltGinsuFunction
import org.burstsys.felt.model.sweep.symbols.schemaRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types.FeltType

trait FeltGinsuNowFuncExpr extends FeltGinsuFunction {

  final override val nodeName = "ginsu-now-call"

  final val functionName: String = NOW

  final override val usage: String =
    s"""
       |usage: $functionName() -> long_literal
       |  return current epoch time as a long value. Note this is a single time calculated at
       |  scan begin.
     """.stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    feltType = FeltType.valScal[Long]
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(0)
    s"""|
        |${T(this)}
        |$I${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = $schemaRuntimeSym.$functionName;""".stripMargin
  }

}
