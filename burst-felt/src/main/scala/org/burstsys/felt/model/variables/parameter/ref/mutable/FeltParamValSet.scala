/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.parameter.ref.mutable

import org.burstsys.felt.model.mutables.valset.{FeltMutableValSet, FeltMutableValSetProv}
import org.burstsys.felt.model.sweep.symbols.{callExtantParameterSym, callNullParameterSym, callScalarParameterSym, sweepRuntimeSym}
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.variables.parameter.ref.FeltParamRef

import scala.language.postfixOps

/**
 *
 * @see [[FeltMutableValSet]]
 */
trait FeltParamValSet {

  self: FeltParamRef =>

  private
  lazy val binding: FeltMutableValSetProv = global.binding.mutables.valset

  final
  def genValSetDecl(implicit cursor: FeltCodeCursor): FeltCode = {
    val builderClass = binding.builderClassName
    val mutableClass = binding.mutableClassName
    s"""|
        |${C1(s"declare value set parameter $varName")}
        |${I}val $builderVar:$builderClass = ???; // $varName builder
        |${I}val $parameterNull:Boolean = false; // $varName nullity
        |${I}val $parameterValue:$mutableClass = ???; // $varName mutable """.stripMargin
  }


  final
  def genValSetPrepare(implicit cursor: FeltCodeCursor): FeltCode = {
    val mutableClass = binding.mutableClassName
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

    val code = {
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
    code
  }

  final
  def genValSetRelease(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"release value set parameter $varName")}
        |${I1}""".stripMargin
  }

  final
  def genValSetRead(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C1(s"read value set parameter")}
        |${I1}""".stripMargin
  }


}
