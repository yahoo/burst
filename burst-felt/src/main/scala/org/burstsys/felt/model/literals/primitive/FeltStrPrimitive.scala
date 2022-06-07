/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals.primitive

import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}
import org.burstsys.felt.model.types.FeltType

/**
 * A literal (static) value expression that reduces to a string within the felt model
 * <p/>
 * === The [[org.burstsys.felt.model.sweep.lexicon.FeltLexicon]] mechanics===
 * At scan ''prepare'' time, all string literals (and all other string usages)
 * are look'ed up in the dictionary and the correct key value for the current brio blob dictionary are stored
 * in the runtime. The removes all runtime dictionary lookups and the attendant UTF8 codec ops.
 */
trait FeltStrPrimitive extends FeltPrimitive {

  final override val nodeName = "felt-str-atom"

  /**
   * the string value
   *
   * @return
   */
  def value: String

  final
  def lexiconIndex: Int = _lexiconIndex

  final
  def lexiconIndex_=(i: Int): Unit = _lexiconIndex = i

  final override def generateSourceValue: String = s""""$value""""

  final override def generateCodeValue: String = if (global.lexicon.enabled)
    s"${sweepRuntimeSym}.${global.lexicon.valueName(_lexiconIndex)}" else generateSourceValue

  final def generateCodeNull: String = if (global.lexicon.enabled)
    s"${sweepRuntimeSym}.${global.lexicon.nullityName(_lexiconIndex)}" else "false"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    feltType = FeltType.valScal[String]
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // string extraction
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _lexiconIndex: Int = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I${cursor.callScope.scopeNull} = $generateCodeNull; ${cursor.callScope.scopeVal} = $generateCodeValue; // FELT-STR-ATOM [${generateSourceValue}=${_lexiconIndex}] """.stripMargin
  }

}
