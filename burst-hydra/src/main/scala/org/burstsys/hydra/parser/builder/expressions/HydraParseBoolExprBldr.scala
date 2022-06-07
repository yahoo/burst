/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.expressions

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.bool.{FeltUnaryBoolExpr, _}
import org.burstsys.felt.model.expressions.cmp._
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

/**
  * antlr parse driven builder for boolean expressions in AST
  */
trait HydraParseBoolExprBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // ComparisonBooleanExpression
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitLessThanOrEqualComparisonBooleanExpressionClause(ctx: HydraAnalysisGrammarParser.LessThanOrEqualComparisonBooleanExpressionClauseContext): FeltNode =
    new FeltCmpBoolExpr {
      global = HydraParseBoolExprBldr.this.global
      final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      final override val op: FeltCmpBoolOp =
        new LESS_THAN_OR_EQUAL {
          global = HydraParseBoolExprBldr.this.global
          final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
        }
      final override val lhs: FeltExpression =
        visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression =
        visit(ctx.expression(1)).asInstanceOf[FeltExpression]

    }

  final override
  def visitNotEqualThanComparisonBooleanExpressionClause(ctx: HydraAnalysisGrammarParser.NotEqualThanComparisonBooleanExpressionClauseContext
                                                        ): FeltNode =
    new FeltCmpBoolExpr {
      global = HydraParseBoolExprBldr.this.global
      final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      final override val op: FeltCmpBoolOp = new NOT_EQUAL {
        global = HydraParseBoolExprBldr.this.global
        final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      }
      final override val lhs: FeltExpression =
        visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression =
        visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

  final override
  def visitLessThanComparisonBooleanExpressionClause(ctx: HydraAnalysisGrammarParser.LessThanComparisonBooleanExpressionClauseContext
                                                    ): FeltNode =
    new FeltCmpBoolExpr {
      global = HydraParseBoolExprBldr.this.global
      final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      final override val op: FeltCmpBoolOp = new LESS_THAN {
        global = HydraParseBoolExprBldr.this.global
        final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      }
      final override val lhs: FeltExpression =
        visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression =
        visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

  final override
  def visitEqualThanComparisonBooleanExpressionClause(ctx: HydraAnalysisGrammarParser.EqualThanComparisonBooleanExpressionClauseContext
                                                     ): FeltNode =
    new FeltCmpBoolExpr {
      global = HydraParseBoolExprBldr.this.global
      final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      final override val op: FeltCmpBoolOp = new EQUAL {
        global = HydraParseBoolExprBldr.this.global
        final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      }
      final override val lhs: FeltExpression =
        visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression =
        visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

  final override
  def visitGreaterThanOrEqualThanComparisonBooleanExpressionClause(ctx: HydraAnalysisGrammarParser.GreaterThanOrEqualThanComparisonBooleanExpressionClauseContext
                                                                  ): FeltNode =
    new FeltCmpBoolExpr {
      global = HydraParseBoolExprBldr.this.global
      final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      final override val op: FeltCmpBoolOp =
        new GREATER_THAN_OR_EQUAL {
          global = HydraParseBoolExprBldr.this.global
          final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
        }
      final override val lhs: FeltExpression =
        visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression =
        visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

  final override
  def visitGreaterThanComparisonBooleanExpressionClause(ctx: HydraAnalysisGrammarParser.GreaterThanComparisonBooleanExpressionClauseContext
                                                       ): FeltNode =
    new FeltCmpBoolExpr {
      global = HydraParseBoolExprBldr.this.global
      final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      final override val op: FeltCmpBoolOp =
        new GREATER_THAN {
          global = HydraParseBoolExprBldr.this.global
          final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
        }
      final override val lhs: FeltExpression =
        visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression =
        visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // UnaryBooleanExpression
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitNotUnaryBooleanExpressionClause(ctx: HydraAnalysisGrammarParser.NotUnaryBooleanExpressionClauseContext): FeltNode =
    new FeltUnaryBoolExpr {
      global = HydraParseBoolExprBldr.this.global
      final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      final override val op: FeltUnaryBoolOp = new NOT {
        global = HydraParseBoolExprBldr.this.global
        final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      }
      final override val rhs: FeltExpression =
        visit(ctx.expression).asInstanceOf[FeltExpression]
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BinaryBooleanExpression
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitAndBinaryBooleanExpressionClause(ctx: HydraAnalysisGrammarParser.AndBinaryBooleanExpressionClauseContext): FeltNode =
    new FeltBinBoolExpr {
      global = HydraParseBoolExprBldr.this.global
      final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      final override val op: FeltBinBoolOp = new AND {
        global = HydraParseBoolExprBldr.this.global
        final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      }
      final override val lhs: FeltExpression =
        visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression =
        visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

  final override
  def visitOrBinaryBooleanExpressionClause(ctx: HydraAnalysisGrammarParser.OrBinaryBooleanExpressionClauseContext): FeltNode =
    new FeltBinBoolExpr {
      global = HydraParseBoolExprBldr.this.global
      final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      final override val op: FeltBinBoolOp = new OR {
        global = HydraParseBoolExprBldr.this.global
        final override val location = HydraLocation(HydraParseBoolExprBldr.this.global, ctx)
      }
      final override val lhs: FeltExpression =
        visit(ctx.expression(0)).asInstanceOf[FeltExpression]
      final override val rhs: FeltExpression =
        visit(ctx.expression(1)).asInstanceOf[FeltExpression]
    }

}
