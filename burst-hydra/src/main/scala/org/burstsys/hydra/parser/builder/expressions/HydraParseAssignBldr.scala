/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.expressions

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.assign.{FeltAssignExpr, FeltUpdateExpr, FeltUpdateOp, MINUS_EQ, PLUS_EQ}
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser._
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

/**
  * antlr parse driven builder for assignment expressions in AST
  */
trait HydraParseAssignBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  final override
  def visitAssignmentExpression(ctx: HydraAnalysisGrammarParser.AssignmentExpressionContext): FeltNode =
    new FeltAssignExpr {
      global = HydraParseAssignBldr.this.global
      final override val location = HydraLocation(HydraParseAssignBldr.this.global, ctx)
      final override val lhs: FeltPathExpr = visit(ctx.pathExpression).asInstanceOf[FeltPathExpr]
      final override val rhs: FeltExpression = {
        if (ctx.expression != null)
          visit(ctx.expression).asInstanceOf[FeltExpression]
        else if (ctx.expression != null)
          visit(ctx.expression).asInstanceOf[FeltExpression]
        else
          throw FeltException(lhs.location, s"invalid assignment '$lhs = ?' (no rhs)")
      }
    }

  final override
  def visitUpdateExpression(ctx: HydraAnalysisGrammarParser.UpdateExpressionContext): FeltNode =
    new FeltUpdateExpr {
      global = HydraParseAssignBldr.this.global
      final override val location = HydraLocation(HydraParseAssignBldr.this.global, ctx)
      final override val lhs: FeltPathExpr = visit(ctx.pathExpression).asInstanceOf[FeltPathExpr]
      final override val rhs: FeltExpression = {
        if (ctx.expression != null)
          visit(ctx.expression).asInstanceOf[FeltExpression]
        else if (ctx.expression != null)
          visit(ctx.expression).asInstanceOf[FeltExpression]
        else
          throw FeltException(lhs.location, s"invalid assignment '$lhs = ?' (no rhs)")
      }

      final override val op: FeltUpdateOp =
        if (ctx.MINUS_EQ != null) new MINUS_EQ {
          global = HydraParseAssignBldr.this.global
          final override val location = HydraLocation(HydraParseAssignBldr.this.global, ctx)
        } else if (ctx.PLUS_EQ() != null) new PLUS_EQ {
          global = HydraParseAssignBldr.this.global
          final override val location = HydraLocation(HydraParseAssignBldr.this.global, ctx)
        } else throw FeltException(location, s"unknown update operator")
    }
}
