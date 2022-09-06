/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.visits.decl._
import org.burstsys.felt.model.expressions.{FeltExprBlock, FeltExpression}
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation

import scala.jdk.CollectionConverters._

/**
 * antlr parse driven builder for visit expressions in AST
 */
trait HydraParseVisitBuilder extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  final override
  def visitStaticVisitDeclaration(ctx: HydraAnalysisGrammarParser.StaticVisitDeclarationContext): FeltNode =
    new FeltStaticVisitDecl {
      global = HydraParseVisitBuilder.this.global
      final override val location = HydraLocation(HydraParseVisitBuilder.this.global, ctx)
      final override val traverseTarget: FeltPathExpr = visit(ctx.pathExpression).asInstanceOf[FeltPathExpr]
      final override val variables: Array[FeltGlobVarDecl] =
        ctx.globalVariableDeclaration.asScala.map(visit(_).asInstanceOf[FeltGlobVarDecl]).toArray
      final override val actions: Array[FeltActionDecl] =
        ctx.actionDeclaration.asScala.map(visit(_).asInstanceOf[FeltActionDecl]).toArray
      final override val ordinalExpression: Option[FeltExpression] = {
        if(ctx.expression() == null)
          None
        else
          Some(visit(ctx.expression).asInstanceOf[FeltExpression])
      }
    }

  final override
  def visitDynamicVisitDeclaration(ctx: HydraAnalysisGrammarParser.DynamicVisitDeclarationContext): FeltNode =
    new FeltDynamicVisitDecl {
      global = HydraParseVisitBuilder.this.global
      final override val location = HydraLocation(HydraParseVisitBuilder.this.global, ctx)
      final override val visitedCollector: FeltPathExpr = visit(ctx.pathExpression(0)).asInstanceOf[FeltPathExpr]
      final override val traverseTarget: FeltPathExpr = visit(ctx.pathExpression(1)).asInstanceOf[FeltPathExpr]
      final override val variables: Array[FeltGlobVarDecl] =
        ctx.globalVariableDeclaration.asScala.map(visit(_).asInstanceOf[FeltGlobVarDecl]).toArray
      final override val actions: Array[FeltActionDecl] =
        ctx.actionDeclaration.asScala.map(visit(_).asInstanceOf[FeltActionDecl]).toArray
      final override val ordinalExpression: Option[FeltExpression] = {
        if(ctx.expression() == null)
          None
        else
          Some(visit(ctx.expression).asInstanceOf[FeltExpression])
      }
    }

  final override
  def visitActionDeclaration(ctx: HydraAnalysisGrammarParser.ActionDeclarationContext): FeltNode =
    new FeltActionDecl {
      global = HydraParseVisitBuilder.this.global
      final override val location = HydraLocation(HydraParseVisitBuilder.this.global, ctx)
      final override val expressionBlock: FeltExprBlock = {
        val e = visit(ctx.expressionBlock()).asInstanceOf[FeltExprBlock]
        e.outerBlock = true
        e
      }
      final override val actionType: FeltActionType = {
        global = HydraParseVisitBuilder.this.global
        if (ctx.actionType.PRE != null) FeltPreActionType
        else if (ctx.actionType.POST != null) FeltPostActionType
        else if (ctx.actionType.SITU != null) FeltSituActionType
        else if (ctx.actionType.BEFORE != null) FeltBeforeActionType
        else if (ctx.actionType.AFTER != null) FeltAfterActionType
        else throw FeltException(location, s"unknown action type")
      }
    }

}
