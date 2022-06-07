/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.ginsu.group

import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps

/**
 * generate the Ginsu split function.
 * TODO: This should generate a split signature that does not require the creation of an ARRAY (see [[org.burstsys.ginsu.functions.group.GinsuSplitFunctions]])
 */
trait FeltGinsuGrpEnumFuncExpr extends FeltGinsuGrpFuncExpr {

  final override val nodeName = "ginsu-enum-call"

  final override val usage: String =
    s"""
       |usage: $functionName(value_expression*) -> value_expression
       |  bucket/group a value expression based on a set of expression defined matches""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    parameters.foreach(_.resolveTypes)
    feltType = FeltType.combine(parameters.map(_.feltType): _*)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountAtLeast(2)

    val domain = generatedDomain

    val domainExpressions = domain.map(_._2).mkString

    val domainNullity = domain.map(_._1).map {
      c => c.callScope.scopeNull
    }.mkString(" || ")

    val domainEnums = domain.take(parameters.length - 1).map(_._1).map {
      c => c.callScope.scopeVal
    }.mkString(", ")

    val domainTest = domain.map(_._1).last.callScope.scopeVal

    val functionName = s"${feltType.valueTypeAsFelt}EnumSlice"

    s"""|
        |${T(this)}
        |$domainExpressions
        |${I}if($domainNullity) {
        |$I2${cursor.callScope.scopeNull} = true;
        |$I} else {
        |$I2${cursor.callScope.scopeNull} = false;
        |$I2${cursor.callScope.scopeVal} = $sweepRuntimeSym.$functionName(
        |${I3}Array($domainEnums),
        |${I3}$domainTest
        |$I2);
        |$I}""".stripMargin

  }

}
