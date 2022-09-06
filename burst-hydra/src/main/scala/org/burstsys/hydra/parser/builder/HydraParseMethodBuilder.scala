/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder

import org.burstsys.felt.model.types.FeltTypeDecl
import org.burstsys.felt.model.method.{FeltMethodDecl, FeltReturnExpr}
import org.burstsys.felt.model.variables.parameter.FeltParamDecl
import org.burstsys.felt.model.expressions.{FeltExprBlock, FeltExpression}
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation

import scala.jdk.CollectionConverters._

/**
  * antlr parse driven builder for method expressions in AST
  */
trait HydraParseMethodBuilder extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  final override
  def visitMethodDeclaration(ctx: HydraAnalysisGrammarParser.MethodDeclarationContext): FeltNode =
    new FeltMethodDecl {
      global = HydraParseMethodBuilder.this.global
      final override val location = HydraLocation(HydraParseMethodBuilder.this.global, ctx)
      final override val methodName: String = extractIdentifier(ctx.identifier)
      final override val parameters: Array[FeltParamDecl] =
        ctx.parameterDeclaration.asScala.map(visit(_).asInstanceOf[FeltParamDecl]).toArray
      final override val expressionBlock: FeltExprBlock = visit(ctx.expressionBlock).asInstanceOf[FeltExprBlock]
      final override val returnType: FeltTypeDecl = visit(ctx.valueTypeDeclaration).asInstanceOf[FeltTypeDecl]
    }

  final override
  def visitReturnExpression(ctx: HydraAnalysisGrammarParser.ReturnExpressionContext): FeltNode =
    new FeltReturnExpr {
      global = HydraParseMethodBuilder.this.global
      final override val location = HydraLocation(HydraParseMethodBuilder.this.global, ctx)
      final override val expression: FeltExpression = visit(ctx.expression()).asInstanceOf[FeltExpression]
    }

}
