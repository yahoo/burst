/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.data

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.types.FeltTypeDecl
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl
import org.burstsys.felt.model.variables.local.FeltLocVarDecl
import org.burstsys.felt.model.variables.parameter.FeltParamDecl
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

/**
  * antlr parse driven builder for variable declarations in AST
  */
trait HydraParseVarBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  parameters/variables
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitParameterDeclaration(ctx: HydraAnalysisGrammarParser.ParameterDeclarationContext): FeltParamDecl =
    new FeltParamDecl {
      global = HydraParseVarBldr.this.global
      final override val location = HydraLocation(HydraParseVarBldr.this.global, ctx)
      final override val refName: FeltPathExpr = visit(ctx.pathExpression()).asInstanceOf[FeltPathExpr]
      final override val typeDeclaration: FeltTypeDecl = {
        if (ctx.valueTypeDeclaration == null)
          throw FeltException(location, s"missing value type declaration")
        visit(ctx.valueTypeDeclaration).asInstanceOf[FeltTypeDecl]
      }
      final override val initializer: FeltExpression = {
        if (ctx.expression() != null)
          visit(ctx.expression).asInstanceOf[FeltExpression]
        else
          throw FeltException(location, s"no initializer for parameter $typeDeclaration")
      }
    }


  final override
  def visitLocalVariableDeclaration(ctx: HydraAnalysisGrammarParser.LocalVariableDeclarationContext): FeltLocVarDecl =
    new FeltLocVarDecl {
      global = HydraParseVarBldr.this.global
      final override val location = HydraLocation(HydraParseVarBldr.this.global, ctx)
      final override val refName: FeltPathExpr = visit(ctx.variableDeclaration.pathExpression()).asInstanceOf[FeltPathExpr]
      final override val typeDeclaration: FeltTypeDecl = {
        if (ctx.variableDeclaration.valueTypeDeclaration == null)
          throw FeltException(location, s"no type declaration for variable $refName")
        val td = visit(ctx.variableDeclaration.valueTypeDeclaration).asInstanceOf[FeltTypeDecl]
        if (td == null)
          throw FeltException(location, s"no type declaration for variable $refName")
        td
      }
      final override val initializer: FeltExpression = {
        if (ctx.variableDeclaration.expression() != null)
          visit(ctx.variableDeclaration.expression).asInstanceOf[FeltExpression]
        else
          throw FeltException(location, s"no initializer for variable $refName")
      }
      final override val isMutable: Boolean = ctx.variableDeclaration.VAR != null
    }

  final override
  def visitGlobalVariableDeclaration(ctx: HydraAnalysisGrammarParser.GlobalVariableDeclarationContext): FeltGlobVarDecl =
    new FeltGlobVarDecl {
      global = HydraParseVarBldr.this.global
      final override val location = HydraLocation(HydraParseVarBldr.this.global, ctx)
      final override val refName: FeltPathExpr = visit(ctx.variableDeclaration().pathExpression()).asInstanceOf[FeltPathExpr]
      final override val typeDeclaration: FeltTypeDecl = {
        if (ctx.variableDeclaration.valueTypeDeclaration == null)
          throw FeltException(location, s"no type declaration for variable $refName")
        val td = visit(ctx.variableDeclaration.valueTypeDeclaration).asInstanceOf[FeltTypeDecl]
        if (td == null)
          throw FeltException(location, s"no type declaration for variable $refName")
        td
      }
      final override val initializer: FeltExpression = {
        if (ctx.variableDeclaration.expression() != null)
          visit(ctx.variableDeclaration.expression).asInstanceOf[FeltExpression]
        else
          throw FeltException(location, s"no initializer for variable $refName")
      }
      final override val isMutable: Boolean = ctx.variableDeclaration.VAR != null
    }

}
