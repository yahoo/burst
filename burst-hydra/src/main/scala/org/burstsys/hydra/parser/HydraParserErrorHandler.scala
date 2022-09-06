/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.tree.{FeltGlobal, FeltLocation, FeltContextualizer}
import org.antlr.v4.runtime.{BaseErrorListener, RecognitionException, Recognizer}

import scala.collection.mutable.ArrayBuffer

final case
class HydraParserErrorHandler(source: String) extends BaseErrorListener with FeltContextualizer {

  case class ErrorLocation(line: Int, col: Int, message: String)

  private[this] val _errors = new ArrayBuffer[ErrorLocation]()

  override def syntaxError(recognizer: Recognizer[_, _], offendingSymbol: Any,
                           lineIndex: Int, charIndex: Int, msg: String, e: RecognitionException): Unit = {
    try {
      _errors += ErrorLocation(lineIndex, charIndex, contextualize(lineIndex, charIndex, msg))
    } catch {
      case t: Throwable =>
        log error s"Error handler threw $t"
        throw t
    }
  }

  def errors: Array[ErrorLocation] = _errors.toArray

  def errorString: String = this.errors.map(_.message).mkString("\n")

  def hadErrors(): Boolean = _errors.nonEmpty

  def throwIfError(global: FeltGlobal): Unit = {
    if (_errors.nonEmpty)
      throw FeltException(FeltLocation(global, errors.head.line, errors.head.col), errorString)
  }
}
