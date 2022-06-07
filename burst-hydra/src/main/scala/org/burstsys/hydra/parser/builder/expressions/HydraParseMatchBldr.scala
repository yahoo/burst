/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.expressions

import org.burstsys.felt.model.expressions.flow.pattern.{FeltMatchCase, FeltMatchDefault, FeltMatchExpr}
import org.burstsys.felt.model.expressions.{FeltExprBlock, FeltExpression}
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser._
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

import scala.collection.JavaConverters._

/**
  * antlr parse driven builder for match expressions in AST
  */
trait HydraParseMatchBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  final override
  def visitMatchExpression(ctx: HydraAnalysisGrammarParser.MatchExpressionContext): FeltNode =
    new FeltMatchExpr {
      global = HydraParseMatchBldr.this.global
      final override val location = HydraLocation(HydraParseMatchBldr.this.global, ctx)
      final override val path: FeltPathExpr = visit(ctx.pathExpression).asInstanceOf[FeltPathExpr]
      final override val cases: Array[FeltMatchCase] =
        ctx.matchCase.asScala.map(visit(_).asInstanceOf[FeltMatchCase]).toArray
      final override val default: Option[FeltMatchDefault] = {
        if (ctx.matchDefault == null) None
        else Some(visit(ctx.matchDefault).asInstanceOf[FeltMatchDefault])
      }
    }

  final override
  def visitMatchCase(ctx: HydraAnalysisGrammarParser.MatchCaseContext): FeltNode =
    new FeltMatchCase {
      global = HydraParseMatchBldr.this.global
      final override val location = HydraLocation(HydraParseMatchBldr.this.global, ctx)
      final override val expression: FeltExpression =
        visit(ctx.expression).asInstanceOf[FeltExpression]
      final override val expressionBlock: FeltExprBlock =
        visit(ctx.expressionBlock).asInstanceOf[FeltExprBlock]
    }

  final override
  def visitMatchDefault(ctx: HydraAnalysisGrammarParser.MatchDefaultContext): FeltNode =
    new FeltMatchDefault {
      global = HydraParseMatchBldr.this.global
      final override val location = HydraLocation(HydraParseMatchBldr.this.global, ctx)
      final override val expressionBlock: FeltExprBlock =
        visit(ctx.expressionBlock).asInstanceOf[FeltExprBlock]
    }

}
