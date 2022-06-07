/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.expressions

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.variables.local.FeltLocVarDecl
import org.burstsys.felt.model.expressions.{FeltExprBlock, FeltExpression}
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser._
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

import scala.collection.JavaConverters._

/**
  * antlr parse driven builder for expression blocks in AST
  */
trait HydraParseExprBlockBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  final override
  def visitExpressionBlock(ctx: HydraAnalysisGrammarParser.ExpressionBlockContext): FeltNode =
    new FeltExprBlock {
      global = HydraParseExprBlockBldr.this.global
      final override val location = HydraLocation(HydraParseExprBlockBldr.this.global, ctx)
      final override val nsName: String = global.newAnonymousName
      final override val variables: Array[FeltLocVarDecl] =
        ctx.localVariableDeclaration.asScala.map(visit(_).asInstanceOf[FeltLocVarDecl]).toArray
      final override val statements: Array[FeltExpression] =
        ctx.expression.asScala.map{
          e=>
            val node = visit(e)
            if(node == null)
              throw FeltException(HydraLocation(HydraParseExprBlockBldr.this.global, e), s"expression block had bad statement ${e.getText}")
            node.asInstanceOf[FeltExpression]
        }.toArray.filter(_ != null)

    }

}
