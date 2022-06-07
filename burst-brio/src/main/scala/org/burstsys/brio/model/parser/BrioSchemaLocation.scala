/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.parser

import org.antlr.v4.runtime.tree.TerminalNode
import org.antlr.v4.runtime.{ParserRuleContext, Token}

/**
  *
  * @param lineNumber
  * @param charPositionInLine
  */
final case
class BrioSchemaLocation(lineNumber: Int, charPositionInLine: Int) {
  def columnNumber: Int = charPositionInLine + 1
}

/**
  *
  */
object BrioSchemaLocation {

  def locate(terminalNode: TerminalNode): BrioSchemaLocation = {
    locate(terminalNode.getSymbol)
  }

  def locate(parserRuleContext: ParserRuleContext): BrioSchemaLocation = {
    locate(parserRuleContext.getStart)
  }

  def locate(token: Token): BrioSchemaLocation = {
    BrioSchemaLocation(token.getLine, token.getCharPositionInLine)
  }

}
