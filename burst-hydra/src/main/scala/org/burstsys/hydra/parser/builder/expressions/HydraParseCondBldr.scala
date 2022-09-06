/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.expressions

import org.burstsys.felt.model.expressions.FeltExprBlock
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.flow.conditional.FeltCondExpr
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser._
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

import scala.jdk.CollectionConverters._

/**
  * antlr parse driven builder for conditional expressions in AST
  */
trait HydraParseCondBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  final override
  def visitConditionalExpression(ctx: HydraAnalysisGrammarParser.ConditionalExpressionContext): FeltNode =
    new FeltCondExpr {
      global = HydraParseCondBldr.this.global
      final override val location = HydraLocation(HydraParseCondBldr.this.global, ctx)
      final override val ifConditionTest: FeltBoolExpr =
        visit(ctx.ifConditionTest.conditionTest).asInstanceOf[FeltBoolExpr]
      final override val ifExpressionBlock: FeltExprBlock =
        visit(ctx.ifExpressionBlock.expressionBlock).asInstanceOf[FeltExprBlock]
      final override val elseIfConditionTest: Array[FeltBoolExpr] =
        ctx.elseIfConditionTest.asScala.map(visit(_).asInstanceOf[FeltBoolExpr]).toArray
      final override val elseIfExpressionBlock: Array[FeltExprBlock] =
        ctx.elseIfExpressionBlock.asScala.map(visit(_).asInstanceOf[FeltExprBlock]).toArray
      final override val elseExpressionBlock: Option[FeltExprBlock] = {
        if (ctx.elseExpressionBlock == null) None
        else Some(visit(ctx.elseExpressionBlock()).asInstanceOf[FeltExprBlock])
      }
      final override val elseNullExpressionBlock: Option[FeltExprBlock] = {
        if (ctx.elseNullExpressionBlock == null) None
        else Some(visit(ctx.elseNullExpressionBlock()).asInstanceOf[FeltExprBlock])
      }
    }

  final override
  def visitConditionTest(ctx: HydraAnalysisGrammarParser.ConditionTestContext): FeltNode =
    visit(ctx.expression())

}
