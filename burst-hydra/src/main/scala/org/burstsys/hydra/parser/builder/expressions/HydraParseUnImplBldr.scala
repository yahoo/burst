/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.expressions

import org.burstsys.felt.model.expressions.FeltUnImplExpr
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser._
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

/**
  * antlr parse driven builder for not implemented expressions in AST
  */
trait HydraParseUnImplBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>


  final override
  def visitUnImplementedExpression(ctx: HydraAnalysisGrammarParser.UnImplementedExpressionContext): FeltNode =
    new FeltUnImplExpr {
      global = HydraParseUnImplBldr.this.global
      final override val location = HydraLocation(HydraParseUnImplBldr.this.global, ctx)
    }


}
