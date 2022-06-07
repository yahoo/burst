/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.global.ref.primitive

import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, I2, M}
import org.burstsys.felt.model.variables.global.ref.FeltGlobVarRef

import scala.language.postfixOps

trait FeltGlobVarValScal {

  self: FeltGlobVarRef =>

  final
  def genValScalDecl(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${I}var $variableNull:Boolean = false; var $variableValue:${typeDeclaration.generateType} = ${feltType.valueDefaultAsCode}; // $nodeName-DECL $varName""".stripMargin
  }

  final
  def genValScalPrep(implicit cursor: FeltCodeCursor): FeltCode = {
    val initializerCursor = cursor indentRight 1 scopeDown

    s"""|
        |$I{ // ${M(this)}
        |${I2}var ${initializerCursor.callScope.scopeNull}:Boolean = false; var ${initializerCursor.callScope.scopeVal}:${feltType.valueTypeAsCode} = ${feltType.valueDefaultAsCode}; // $nodeName-HDR $varName
        |${initializer.generateExpression(initializerCursor)}
        |${I2}if( ${initializerCursor.callScope.scopeNull} ) { $variableNull = true; } else { $variableNull = false; $variableValue = ${initializerCursor.callScope.scopeVal} }  // $nodeName-FTR $varName
        |$I}""".stripMargin
  }

  final
  def genValScalWrite(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${I}if( ${cursor.callScope.scopeNull} ) { $sweepRuntimeSym.${variableNull} = true } else { $sweepRuntimeSym.${variableNull} = false; $sweepRuntimeSym.${variableValue} = ${cursor.callScope.scopeVal} }  // ${refDecl.nodeName}-WR ${refDecl.varName} """.stripMargin
  }

  final
  def genValScalRead(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$I${cursor.callScope.scopeNull} = $sweepRuntimeSym.${variableNull}; ${cursor.callScope.scopeVal} = $sweepRuntimeSym.${variableValue}; // ${refDecl.nodeName}-RD ${refDecl.varName} """.stripMargin


}
