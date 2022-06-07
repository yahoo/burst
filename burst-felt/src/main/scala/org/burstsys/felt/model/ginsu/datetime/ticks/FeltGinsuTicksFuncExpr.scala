/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu.datetime.ticks

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.ginsu.FeltGinsuFunction
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps

trait FeltGinsuTicksFuncExpr extends FeltGinsuFunction {

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
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    val expr = parameters(0).resolveTypes
    if (!expr.feltType.isFixedValue)
      throw FeltException(location, s"$functionName wrong parameter type '${expr.feltType.valueTypeAsFelt}'! $usage")

    val readCursor = cursor indentRight 1 scopeDown

    s"""|
        |${T(this)}
        |${I}var ${readCursor.callScope.scopeNull}:Boolean = false; var ${readCursor.callScope.scopeVal}:${feltType.valueTypeAsCode} = ${feltType.valueDefaultAsCode}; // $nodeName-DECL
        |${expr.generateExpression(readCursor)}
        |${I}if(${readCursor.callScope.scopeNull}) {
        |$I2${cursor.callScope.scopeNull} = true;
        |$I} else {
        |$I2${cursor.callScope.scopeNull} = false;
        |$I2${cursor.callScope.scopeVal} = $sweepRuntimeSym.$functionName(${readCursor.callScope.scopeVal});
        |$I}""".stripMargin

  }

}
