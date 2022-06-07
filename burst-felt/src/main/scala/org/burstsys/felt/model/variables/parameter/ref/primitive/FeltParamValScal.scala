/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.parameter.ref.primitive

import org.burstsys.felt.model.sweep.symbols.{callExtantParameterSym, callNullParameterSym, callScalarParameterSym, sweepRuntimeSym}
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, FeltNoCode, I, I2, I3, I4, M, T}
import org.burstsys.felt.model.variables.parameter.ref.FeltParamRef

import scala.language.postfixOps

trait FeltParamValScal {

  self: FeltParamRef =>

  final
  def genValScalDecl(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${T(this)}
        |${I}var $parameterNull:Boolean = false; var $parameterValue:${typeDeclaration.generateType} = ${feltType.valueDefaultAsCode}; """.stripMargin
  }

  final
  def genValScalPrepare(implicit cursor: FeltCodeCursor): FeltCode = {
    val initializerCursor = cursor indentRight 2 scopeDown

    def parameterUpdate(implicit cursor: FeltCodeCursor): FeltCode =
      if (feltType.isString && global.lexicon.enabled)
        s"""|${C("special handling for string parameters when lexicon is enabled")}
            |${I}val str = $callScalarParameterSym[String]("${refName.fullPathNoQuotes}");
            |${I}val key = dictionary.keyLookup(str)($sweepRuntimeSym.text);
            |${I}${initializerCursor.callScope.scopeNull} = (key == -1);
            |${I}${initializerCursor.callScope.scopeVal} = key; """.stripMargin
      else
        s"""|${I}${initializerCursor.callScope.scopeNull} = false;
            |${I}${initializerCursor.callScope.scopeVal} = $callScalarParameterSym[${feltType.valueTypeAsCode.capitalize}]("${refName.fullPathNoQuotes}");""".stripMargin

    s"""|
        |$I{  //  ${M(this)}
        |${I2}var ${initializerCursor.callScope.scopeNull}:Boolean = true; var ${initializerCursor.callScope.scopeVal}:${feltType.valueTypeAsCode} = ${feltType.valueDefaultAsCode}; // $nodeName-HDR $varName
        |${I2}if (!$callExtantParameterSym("${refName.fullPathNoQuotes}")) {
        |${initializer.generateExpression(initializerCursor)}
        |$I2} else {
        |${I3}if ($callNullParameterSym("${refName.fullPathNoQuotes}")) {
        |$I4${initializerCursor.callScope.scopeNull} = true;
        |$I3} else {
        |${parameterUpdate(cursor indentRight 3)}
        |$I3}
        |$I2}
        |${I2}if( ${initializerCursor.callScope.scopeNull} ) { $parameterNull = true; } else { $parameterNull = false; $parameterValue = ${initializerCursor.callScope.scopeVal} }  // $nodeName-FTR $varName
        |$I}""".stripMargin
  }

  final
  def genValScalRelease(implicit cursor: FeltCodeCursor): FeltCode = FeltNoCode

  final
  def genValScalRead(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |${cursor.callScope.scopeNull} = $sweepRuntimeSym.${parameterNull}; ${cursor.callScope.scopeVal} = $sweepRuntimeSym.${parameterValue}; // ${refDecl.nodeName}-RD ${refDecl.varName} """.stripMargin

}
