/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser

import org.burstsys.felt.model.tree.{FeltGlobal, FeltLocation}
import org.antlr.v4.runtime.tree.TerminalNode
import org.antlr.v4.runtime.{ParserRuleContext, Token}

/**
 * A layer on top of [[FeltLocation]] that captures hydra specific location semantics
 */
object HydraLocation {

  def apply(global: FeltGlobal, terminalNode: TerminalNode): FeltLocation = {
    apply(global, terminalNode.getSymbol)
  }

  def apply(global: FeltGlobal, parserRuleContext: ParserRuleContext): FeltLocation = {
    apply(global, parserRuleContext.getStart)
  }

  def apply(global: FeltGlobal, token: Token): FeltLocation = {
    FeltLocation(global, token.getLine, token.getCharPositionInLine)
  }

}
