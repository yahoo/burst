/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.parser

import org.antlr.v4.runtime.RecognitionException

/**
  *
  * @param message
  * @param cause
  * @param line
  * @param charPositionInLine
  */
final case
class BrioSchemaException(message: String, cause: RecognitionException, line: Int, charPositionInLine: Int)
  extends RuntimeException {

  def lineNumber: Int = line

  def columnNumber: Int = charPositionInLine + 1

  def getErrorMessage: String = message

  override
  def getMessage: String = s"line $lineNumber:$columnNumber: $message"
}

/**
  *
  */
object BrioSchemaException {
  import BrioSchemaParser._
  def apply(nodeLocation: BrioSchemaLocation, message: String): BrioSchemaException = {
    BrioSchemaException(message, null, nodeLocation.lineNumber, nodeLocation.columnNumber)
  }

  def apply(node: BrioParseNode, message: String): BrioSchemaException = {
    BrioSchemaException(message, null, node.location.lineNumber, node.location.columnNumber)
  }

  def apply(message: String): BrioSchemaException = {
    BrioSchemaException(message, null, 0, 0)
  }
}