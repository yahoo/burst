/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.data

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

import scala.jdk.CollectionConverters._

/**
 * antlr parse driven builder for data (path, literals, etc) in AST
 */
trait HydraParsePathBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Paths
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitPathExpression(ctx: HydraAnalysisGrammarParser.PathExpressionContext): FeltPathExpr =
    new FeltPathExpr {
      global = HydraParsePathBldr.this.global
      final override val location = HydraLocation(HydraParsePathBldr.this.global, ctx)
      final override val components: Array[String] = ctx.identifier.asScala.map(_.getText).toArray
      final override val key: Option[FeltExpression] = {
        if (ctx.expression != null)
          Some(visit(ctx.expression).asInstanceOf[FeltExpression])
        else None
      }
    }

}
