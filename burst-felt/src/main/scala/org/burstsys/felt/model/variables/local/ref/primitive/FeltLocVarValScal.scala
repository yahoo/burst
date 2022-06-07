/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.local.ref.primitive

import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, I2, M, T}
import org.burstsys.felt.model.variables.local.ref.FeltLocVarRef

import scala.language.postfixOps

/**
 *
 */
trait FeltLocVarValScal {

  self: FeltLocVarRef =>

  final
  def genValScalDecl(implicit cursor: FeltCodeCursor): FeltCode = {

    val initializerCursor = cursor indentRight 2 scopeDown

    val initializerCode = initializer.generateExpression(initializerCursor)

    s"""|
        |${T(this)}
        |${I}var $variableNull:Boolean = false; var $variableValue:${typeDeclaration.generateType} = ${feltType.valueDefaultAsCode}; // $nodeName-DECL $varName
        |$I{ //  ${M(this)}
        |${I2}var ${initializerCursor.callScope.scopeNull}:Boolean = false; var ${initializerCursor.callScope.scopeVal}:${feltType.valueTypeAsCode} = ${feltType.valueDefaultAsCode}; // $nodeName-HDR $varName$initializerCode
        |${I2}if( ${initializerCursor.callScope.scopeNull} ) { $variableNull = true; } else { $variableNull = false; $variableValue = ${initializerCursor.callScope.scopeVal} } // $nodeName-FTR $varName
        |$I}""".stripMargin
  }


  final
  def genValScalPrep(implicit cursor: FeltCodeCursor): FeltCode = {
    ???
  }

  final
  def genValScalRead(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${I}if( ${variableNull}  ) { ${cursor.callScope.scopeNull} = true; } else { ${cursor.callScope.scopeVal} = ${variableValue};  } // ${refDecl.nodeName}-RD  ${refDecl.varName}""".stripMargin
  }

  final
  def genValScalWrite(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${I}if( ${cursor.callScope.scopeNull} ) { ${variableNull} = true; } else { ${variableNull} = false; ${variableValue}; = ${cursor.callScope.scopeVal} } // ${refDecl.nodeName}-WR ${refDecl.varName}""".stripMargin
  }

}
