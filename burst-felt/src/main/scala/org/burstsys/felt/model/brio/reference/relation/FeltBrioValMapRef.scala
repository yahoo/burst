/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.reference.relation

import org.burstsys.brio.types.BrioTypes.BrioStringKey
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.reference.FeltReference
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, I2, I3, I4, T}

import scala.language.postfixOps

/**
 * code generator for a [[FeltReference]]  to a value map Brio relation
 * ==Example==
 * <pre>  if( user.sessions.events.parameters["User Level"] == "1") </pre>
 * == TODO ==
 * <ol>
 * <li>Port this code to use the [[org.burstsys.felt.model.sweep.lexicon.FeltLexicon]] which
 * will eliminate repeated dictionary lookups of statically defined strings</li>
 * <li>only string->string maps are coded up</li>
 * </ol>
 * <H1>TODO '''NOTE:''' THIS IS CURRENTLY ONLY IMPLEMENTED FOR STRING->STRING maps!!!</H1>
 */
trait FeltBrioValMapRef extends Any {

  self: FeltBrioStdRef =>

  protected
  def generateValMapRefRead(implicit cursor: FeltCodeCursor): FeltCode = {

    val keyExpr = refName.key.getOrElse(
      throw FeltException(location, s"value map key expression not specified!")
    )

    val keyExprCursor = cursor indentRight 1 scopeDown

    s"""|
        |${T(this)}
        |${I}var ${keyExprCursor.callScope.scopeNull}:Boolean = false; var ${keyExprCursor.callScope.scopeVal}:${feltType.valueTypeAsCode} = ${feltType.valueDefaultAsCode}; // $nodeName-SOURCE
        |${I}if ($parentInstanceIsNull) { ${cursor.callScope.scopeNull} = true; } else { ${keyExpr.generateExpression(keyExprCursor)}
        |${I2}if (${keyExprCursor.callScope.scopeNull} ) {  ${cursor.callScope.scopeNull} = true; } else {
        |${I3}val instance = $parentInstance; val schematic = $brioSchemaSym.schematic(${parentStructure.structureTypeKey}, instance.versionKey($blobReaderSym));
        |${I3}if( instance.relationIsNull($blobReaderSym, schematic, $relationOrdinal) ) { ${cursor.callScope.scopeNull} = true;  } else {
        |${typeSpecificGeneration(keyExprCursor)(cursor indentRight 3)}
        |$I3}
        |$I2}
        |$I}""".stripMargin
  }

  /**
   * key/value type specific code generation. Mostly about string key or value details...
   *
   * @param keyExprCursor
   * @param cursor
   * @return
   */
  private
  def typeSpecificGeneration(keyExprCursor: FeltCodeCursor)(implicit cursor: FeltCodeCursor): FeltCode = {

    (keyType, valueType) match {

      // special case for string key and string value
      case (BrioStringKey, BrioStringKey) =>
        if (global.lexicon.enabled)
          s"""|${T(this)}
              |${I}if(${keyExprCursor.callScope.scopeNull}) {
              |${I2}${cursor.callScope.scopeNull} = true;
              |${I}} else {
              |${I2}instance.valueMapStringString($blobReaderSym, schematic, $relationOrdinal, ${keyExprCursor.callScope.scopeVal}) match {
              |${I3}case -1 ⇒ ${cursor.callScope.scopeNull} = true;
              |${I3}case valueKey ⇒  ${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = valueKey;
              |${I2}}
              |$I} // FELT-BRIO-VAL-MAP""".stripMargin
        else
          s"""|${T(this)}
              |$I$blobDictionarySym.keyLookup(${keyExprCursor.callScope.scopeVal})($schemaRuntimeSym.text) match {
              |${I2}case $brioDictionaryNotFound ⇒ ${cursor.callScope.scopeNull} = true;
              |${I2}case keyKey ⇒ instance.valueMapStringString($blobReaderSym, schematic, $relationOrdinal, keyKey) match {
              |${I3}case $brioDictionaryNotFound ⇒ ${cursor.callScope.scopeNull} = true;
              |${I3}case valueKey ⇒ $blobDictionarySym.stringLookup(valueKey)($schemaRuntimeSym.text) match {
              |${I4}case null ⇒ ${cursor.callScope.scopeNull} = true;
              |${I4}case str ⇒ ${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = str;
              |$I3}
              |$I2}
              |$I} // FELT-BRIO-VAL-MAP""".stripMargin



      // special case for string key and non string value
      case (BrioStringKey, _) => throw FeltException(location, s"this type of value map not implemented yet")

      // special case for non string key and string value
      case (_, BrioStringKey) => throw FeltException(location, s"this type of value map not implemented yet")

      // all other cases have no strings and general case
      case (_, _) => throw FeltException(location, s"this type of value map not implemented yet")
        s"""
           |instance.valueMap$valueTypeName$keyTypeName($blobReaderSym, schematic, $relationOrdinal, mapKey: Long)""".stripMargin

      case _ => ???
    }
  }

}
