/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.cast

import org.burstsys.brio.types.BrioTypes
import org.burstsys.felt.model.tree.code._

import scala.language.postfixOps

/**
 * generate code for casting of one value to another if neither of the values is a string.
 */
trait FeltCastNumberGen {

  self: FeltCastNumberExpr =>

  def castNumberToNumber(implicit cursor: FeltCodeCursor): FeltCode = {

    val readCursor = cursor indentRight 1 scopeDown

    val function: String = typeDeclaration.feltType.valueType match {
      case BrioTypes.BrioBooleanKey => "toBoolean"
      case BrioTypes.BrioByteKey => "toByte"
      case BrioTypes.BrioShortKey => "toShort"
      case BrioTypes.BrioIntegerKey => "toInt"
      case BrioTypes.BrioLongKey => "toLong"
      case BrioTypes.BrioDoubleKey => "toDouble"
      case _ => ???
    }

    s"""|
        |${T(this)}
        |${I}var ${readCursor.callScope.scopeNull}:Boolean = false; var ${readCursor.callScope.scopeVal}:${expression.feltType.valueTypeAsCode} = ${expression.feltType.valueDefaultAsCode}; // $nodeName-DECL
        |${expression.generateExpression(readCursor)}
        |${I}if(${readCursor.callScope.scopeNull}) {
        |$I2${cursor.callScope.scopeNull} = true;
        |$I} else {
        |$I2${cursor.callScope.scopeVal} = ${readCursor.callScope.scopeVal}.$function;;
        |$I}""".stripMargin

  }

}
