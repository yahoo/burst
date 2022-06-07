/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.expressions

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.inclusion.{FeltInlineSetInclusionExpr, FeltRangeInclusionExpr, FeltRefSetInclusionExpr}
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr
import org.burstsys.hydra.parser.builder.data.HydraParseVarBldr

import scala.collection.JavaConverters._

/**
  * antlr parse driven builder for boolean expressions in AST
  */
trait HydraInclusionExprBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  final override
  def visitInvertedInlineSetInclusionClause(ctx: HydraAnalysisGrammarParser.InvertedInlineSetInclusionClauseContext): FeltNode =
    new FeltInlineSetInclusionExpr {
      global = HydraInclusionExprBldr.this.global
      final override val location = HydraLocation(HydraInclusionExprBldr.this.global, ctx)
      final override val invert: Boolean = true
      final override val value: FeltExpression = visit(ctx.expression.asScala.head).asInstanceOf[FeltExpression]
      final override val members: Array[FeltExpression] =
        ctx.expression.asScala.tail.map(visit(_).asInstanceOf[FeltExpression]).toArray
    }

  final override
  def visitInlineSetInclusionClause(ctx: HydraAnalysisGrammarParser.InlineSetInclusionClauseContext): FeltNode =
    new FeltInlineSetInclusionExpr {
      global = HydraInclusionExprBldr.this.global
      final override val location = HydraLocation(HydraInclusionExprBldr.this.global, ctx)
      final override val invert: Boolean = false
      final override val value: FeltExpression = visit(ctx.expression.asScala.head).asInstanceOf[FeltExpression]
      final override val members: Array[FeltExpression] =
        ctx.expression.asScala.tail.map(visit(_).asInstanceOf[FeltExpression]).toArray
    }

  final override
  def visitInvertedRefSetInclusionClause(ctx: HydraAnalysisGrammarParser.InvertedRefSetInclusionClauseContext): FeltNode =
    new FeltRefSetInclusionExpr {
      global = HydraInclusionExprBldr.this.global
      final override val location = HydraLocation(HydraInclusionExprBldr.this.global, ctx)
      final override val invert: Boolean = true
      final override val source: FeltPathExpr = visit(ctx.pathExpression(0)).asInstanceOf[FeltPathExpr]
      final override val memberPath: FeltPathExpr = visit(ctx.pathExpression(1)).asInstanceOf[FeltPathExpr]
    }

  final override
  def visitRefSetInclusionClause(ctx: HydraAnalysisGrammarParser.RefSetInclusionClauseContext): FeltNode =
    new FeltRefSetInclusionExpr {
      global = HydraInclusionExprBldr.this.global
      final override val location = HydraLocation(HydraInclusionExprBldr.this.global, ctx)
      final override val invert: Boolean = false
      final override val source: FeltPathExpr = visit(ctx.pathExpression(0)).asInstanceOf[FeltPathExpr]
      final override val memberPath: FeltPathExpr = visit(ctx.pathExpression(1)).asInstanceOf[FeltPathExpr]
    }

  final override
  def visitInvertedRangeInclusionClause(ctx: HydraAnalysisGrammarParser.InvertedRangeInclusionClauseContext): FeltNode =
    new FeltRangeInclusionExpr {
      global = HydraInclusionExprBldr.this.global
      final override val location = HydraLocation(HydraInclusionExprBldr.this.global, ctx)
      final override val invert: Boolean = true
      final override val value: FeltExpression = visit(ctx.expression.asScala.head).asInstanceOf[FeltExpression]
      final override val lowerBound: FeltExpression =
        visit(ctx.expression(1)).asInstanceOf[FeltExpression]
      final override val upperBound: FeltExpression =
        visit(ctx.expression(2)).asInstanceOf[FeltExpression]
    }

  final override
  def visitRangeInclusionClause(ctx: HydraAnalysisGrammarParser.RangeInclusionClauseContext): FeltNode =
    new FeltRangeInclusionExpr {
      global = HydraInclusionExprBldr.this.global
      final override val location = HydraLocation(HydraInclusionExprBldr.this.global, ctx)
      final override val invert: Boolean = false
      final override val value: FeltExpression = visit(ctx.expression.asScala.head).asInstanceOf[FeltExpression]
      final override val lowerBound: FeltExpression =
        visit(ctx.expression(1)).asInstanceOf[FeltExpression]
      final override val upperBound: FeltExpression =
        visit(ctx.expression(2)).asInstanceOf[FeltExpression]
    }

}
