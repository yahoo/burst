/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.expressions

import org.burstsys.felt
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.cast.FeltCastNumberExpr
import org.burstsys.felt.model.expressions.function.unknownFunction
import org.burstsys.felt.model.expressions.time.FeltDatetimeExpr
import org.burstsys.felt.model.types.FeltTypeDecl
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

import scala.collection.JavaConverters._

/**
 * antlr parse driven builder for function expressions in AST
 */
trait HydraParseFuncBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Visits
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitFunctionExpression(ctx: HydraAnalysisGrammarParser.FunctionExpressionContext): FeltNode = {
    val location = HydraLocation(HydraParseFuncBldr.this.global, ctx)
    val functionName = ctx.functionType.getText
    val dispatch = (
      felt.model.poly.functions.dispatch(location) // star functions
        orElse felt.model.brio.functions.dispatch(location) // brio functions
        orElse felt.model.collectors.cube.functions.dispatch(location) // felt cube functions
        orElse felt.model.collectors.route.functions.dispatch(location) // zap route functions
        orElse felt.model.collectors.tablet.functions.dispatch(location) // zap tablet functions
        orElse felt.model.control.functions.dispatch(location) //  control verbs
        orElse felt.model.ginsu.functions.dispatch(location) // ginsu functions
        orElse unknownFunction(location)
      ) (functionName)
    dispatch.parameters = ctx.expression.asScala.map(visit(_).asInstanceOf[FeltExpression]).toArray
    dispatch
  }

  final override
  def visitCastExpression(ctx: HydraAnalysisGrammarParser.CastExpressionContext): FeltNode =
    new FeltCastNumberExpr {
      global = HydraParseFuncBldr.this.global
      final override val location = HydraLocation(HydraParseFuncBldr.this.global, ctx)
      final override val typeDeclaration: FeltTypeDecl = {
        if (ctx.valueTypeDeclaration() == null)
          throw FeltException(location, s"no type declaration for cast function")
        val td = visit(ctx.valueTypeDeclaration).asInstanceOf[FeltTypeDecl]
        if (td == null)
          throw FeltException(location, s"no type declaration for cast function")
        td
      }
      final override val expression: FeltExpression =
        visit(ctx.expression).asInstanceOf[FeltExpression]
    }

  final override
  def visitDateTimeExpression(ctx: HydraAnalysisGrammarParser.DateTimeExpressionContext): FeltNode =
    new FeltDatetimeExpr {
      global = HydraParseFuncBldr.this.global
      final override val location = HydraLocation(HydraParseFuncBldr.this.global, ctx)
      final override val spec: String = {
        val str = ctx.STRING_LITERAL
        if (str == null)
          throw FeltException(location, s"datetime function with no string spec")
        extractStringLiteral(str)
      }
    }
}
