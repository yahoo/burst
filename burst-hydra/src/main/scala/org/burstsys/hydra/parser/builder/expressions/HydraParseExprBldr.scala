/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.expressions

import org.burstsys.felt.model.expressions.math._
import org.burstsys.felt.model.expressions.{FeltExpression, FeltParenExpr}
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

/**
  * antlr parse driven builder for unary and binary expressions in AST
  */
trait HydraParseExprBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BinaryExpression
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitAddBinaryExpressionClause(ctx: HydraAnalysisGrammarParser.AddBinaryExpressionClauseContext): FeltNode =
    new FeltBinMathExpr {
      global = HydraParseExprBldr.this.global
      final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      final override val op: FeltBinMathOp = new ADD {
        global = HydraParseExprBldr.this.global
        final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      }
      final override val lhs: FeltExpression = {
        val ve = ctx.expression(0)
        val vve = visit(ve)
        vve.asInstanceOf[FeltExpression]
      }
      final override val rhs: FeltExpression = {
        val ve = ctx.expression(1)
        val vve = visit(ve)
        vve.asInstanceOf[FeltExpression]
      }
    }

  final override
  def visitSubtractBinaryExpressionClause(ctx: HydraAnalysisGrammarParser.SubtractBinaryExpressionClauseContext): FeltNode =
    new FeltBinMathExpr {
      global = HydraParseExprBldr.this.global
      final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      final override val op: FeltBinMathOp = new SUBTRACT {
        global = HydraParseExprBldr.this.global
        final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      }
      final override val lhs: FeltExpression = visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression = visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

  final override
  def visitDivideBinaryExpressionClause(ctx: HydraAnalysisGrammarParser.DivideBinaryExpressionClauseContext): FeltNode =
    new FeltBinMathExpr {
      global = HydraParseExprBldr.this.global
      final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      final override val op: FeltBinMathOp = new DIVIDE {
        global = HydraParseExprBldr.this.global
        final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      }
      final override val lhs: FeltExpression = visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression = visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

  final override
  def visitMultiplyBinaryExpressionClause(ctx: HydraAnalysisGrammarParser.MultiplyBinaryExpressionClauseContext): FeltNode =
    new FeltBinMathExpr {
      global = HydraParseExprBldr.this.global
      final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      final override val op: FeltBinMathOp = new MULTIPLY {
        global = HydraParseExprBldr.this.global
        final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      }
      final override val lhs: FeltExpression = visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression = visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

  final override
  def visitModuloBinaryExpressionClause(ctx: HydraAnalysisGrammarParser.ModuloBinaryExpressionClauseContext): FeltNode =
    new FeltBinMathExpr {
      global = HydraParseExprBldr.this.global
      final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      final override val op: FeltBinMathOp = new MODULO {
        global = HydraParseExprBldr.this.global
        final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      }
      final override val lhs: FeltExpression = visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression = visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // UnaryExpression
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitPositiveUnaryExpressionClause(ctx: HydraAnalysisGrammarParser.PositiveUnaryExpressionClauseContext): FeltNode =
    new FeltUnaryMathExpr {
      global = HydraParseExprBldr.this.global
      final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      final override val op: FeltUnaryMathOp = new POSIT {
        global = HydraParseExprBldr.this.global
        final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      }
      final override val rhs: FeltExpression = visit(ctx.expression()).asInstanceOf[FeltExpression]
    }

  final override
  def visitNegativeUnaryExpressionClause(ctx: HydraAnalysisGrammarParser.NegativeUnaryExpressionClauseContext): FeltNode =
    new FeltUnaryMathExpr {
      global = HydraParseExprBldr.this.global
      final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      final override val op: FeltUnaryMathOp = new NEGATE {
        global = HydraParseExprBldr.this.global
        final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      }
      final override val rhs: FeltExpression = visit(ctx.expression()).asInstanceOf[FeltExpression]
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // ParenthesizedExpression
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  final override
  def visitParenthesizedExpressionClause(ctx: HydraAnalysisGrammarParser.ParenthesizedExpressionClauseContext): FeltNode =
    new FeltParenExpr {
      global = HydraParseExprBldr.this.global
      final override val location = HydraLocation(HydraParseExprBldr.this.global, ctx)
      final override val expression: FeltExpression = visit(ctx.expression()).asInstanceOf[FeltExpression]
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PathExpression
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitPathExpressionClause(ctx: HydraAnalysisGrammarParser.PathExpressionClauseContext): FeltPathExpr =
    visit(ctx.pathExpression).asInstanceOf[FeltPathExpr]

}
