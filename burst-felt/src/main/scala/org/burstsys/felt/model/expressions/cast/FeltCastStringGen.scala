/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.cast

import org.burstsys.brio.types.BrioTypes
import org.burstsys.felt.model.tree.code._

import scala.language.postfixOps

/**
 * generate code for casting of one value to another if one of the values is a string.
 */
trait FeltCastStringGen {

  self: FeltCastNumberExpr =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def castStringToValue(implicit cursor: FeltCodeCursor): FeltCode = {
    if (global.lexicon.enabled) castStringToValueLexicon else castStringToValueNormal
  }

  final
  def castValueToString(implicit cursor: FeltCodeCursor): FeltCode = {
    if (global.lexicon.enabled) castValueToStringLexicon else castValueToStringNormal
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // castStringToValue
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * version for lexicon disabled
   * @param cursor
   * @return
   */
  private final
  def castStringToValueNormal(implicit cursor: FeltCodeCursor): FeltCode = {

    val readCursor = cursor indentRight 1 scopeDown

    val function: String = typeDeclaration.feltType.valueType match {
      case BrioTypes.BrioBooleanKey => "toBoolean"
      case BrioTypes.BrioByteKey => "toByte"
      case BrioTypes.BrioShortKey => "toShort"
      case BrioTypes.BrioIntegerKey => "toInt"
      case BrioTypes.BrioLongKey => "toLong"
      case BrioTypes.BrioDoubleKey => "toDouble"
    }

    s"""|
        |${T(this)}
        |${I}var ${readCursor.callScope.scopeNull}:Boolean = false; var ${readCursor.callScope.scopeVal}:${expression.feltType.valueTypeAsCode} = ${expression.feltType.valueDefaultAsCode}; // $nodeName-DECL
        |${expression.generateExpression(readCursor)}
        |${I}if(${readCursor.callScope.scopeNull}) {
        |$I2${cursor.callScope.scopeNull} = true;
        |$I} else {
        |${I2}try {
        |$I3${cursor.callScope.scopeVal} = ${readCursor.callScope.scopeVal}.$function;
        |$I2} catch {
        |${I3}case nfe:${classOf[NumberFormatException].getName} ⇒
        |$I4${cursor.callScope.scopeNull} = true;
        |$I2}
        |$I}""".stripMargin

  }

  /**
   * if we are using the lexicon, then strings are generally manifest as their dictionary 'keys'.
   * This means we have to convert that key to a string via a dictionary lookup before
   * casting to a long.
   *
   * @param cursor
   * @return
   */
  private final
  def castStringToValueLexicon(implicit cursor: FeltCodeCursor): FeltCode = {

    val readCursor = cursor indentRight 1 scopeDown

    val function: String = typeDeclaration.feltType.valueType match {
      case BrioTypes.BrioBooleanKey => "toBoolean"
      case BrioTypes.BrioByteKey => "toByte"
      case BrioTypes.BrioShortKey => "toShort"
      case BrioTypes.BrioIntegerKey => "toInt"
      case BrioTypes.BrioLongKey => "toLong"
      case BrioTypes.BrioDoubleKey => "toDouble"
    }

    s"""|
        |${T(this)}
        |${I}var ${readCursor.callScope.scopeNull}:Boolean = false; var ${readCursor.callScope.scopeVal}:${expression.feltType.valueTypeAsCode} = ${expression.feltType.valueDefaultAsCode}; // $nodeName-DECL
        |${expression.generateExpression(readCursor)}
        |${I}if(${readCursor.callScope.scopeNull}) {
        |$I2${cursor.callScope.scopeNull} = true;
        |$I} else {
        |${I2}try {
        |${C("first lookup the dictionary value (expensive op because of string codec)")}
        |${I3}val tmpString = runtime.dictionary.stringLookup(${readCursor.callScope.scopeVal})(runtime.text)
        |${I3}if(tmpString == null) { // dictionary term not found...
        |${I4}${cursor.callScope.scopeNull} = true;
        |${I3}} else {
        |${I4}${cursor.callScope.scopeNull} = false;
        |${I4}${cursor.callScope.scopeVal} = tmpString.$function;
        |${I3}}
        |$I2} catch {
        |${I3}case nfe:${classOf[NumberFormatException].getName} ⇒
        |$I4${cursor.callScope.scopeNull} = true;
        |$I2}
        |$I}""".stripMargin

  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // castStringToValue
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * version for lexicon disabled
   * @param cursor
   * @return
   */
  private final
  def castValueToStringNormal(implicit cursor: FeltCodeCursor): FeltCode = {

    val readCursor = cursor indentRight 1 scopeDown

    s"""|
        |${T(this)}
        |${I}var ${readCursor.callScope.scopeNull}:Boolean = false; var ${readCursor.callScope.scopeVal}:${expression.feltType.valueTypeAsCode} = ${expression.feltType.valueDefaultAsCode}; // $nodeName-DECL
        |${expression.generateExpression(readCursor)}
        |${I}if(${readCursor.callScope.scopeNull}) {
        |$I2${cursor.callScope.scopeNull} = true;
        |$I} else {
        |$I2${cursor.callScope.scopeVal} = ${readCursor.callScope.scopeVal}.toString;
        |$I}""".stripMargin

  }

  /**
   * if we are using the lexicon, then strings are generally manifest as their dictionary 'keys'.
   * This means we after we convert the value to a string, we have to lookup/add the string to the dictionary
   * and return that key as the cast value.
   *
   * @param cursor
   * @return
   */
  private final
  def castValueToStringLexicon(implicit cursor: FeltCodeCursor): FeltCode = {
    val readCursor = cursor indentRight 1 scopeDown

    s"""|
        |${T(this)}
        |${I}var ${readCursor.callScope.scopeNull}:Boolean = false; var ${readCursor.callScope.scopeVal}:${expression.feltType.valueTypeAsCode} = ${expression.feltType.valueDefaultAsCode}; // $nodeName-DECL
        |${expression.generateExpression(readCursor)}
        |${I}if(${readCursor.callScope.scopeNull}) {
        |$I2${cursor.callScope.scopeNull} = true;
        |$I} else {
        |$I2${cursor.callScope.scopeVal} = ${readCursor.callScope.scopeVal}.toString;
        |$I}""".stripMargin

  }


}
