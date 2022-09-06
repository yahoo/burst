/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.data

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.literals.mutable.FeltAssociation
import org.burstsys.felt.model.literals.mutable.FeltValArrLit
import org.burstsys.felt.model.literals.mutable.FeltValMapLit
import org.burstsys.felt.model.literals.mutable.FeltValSetLit
import org.burstsys.felt.model.literals.primitive.FeltBoolPrimitive
import org.burstsys.felt.model.literals.primitive.FeltFixPrimitive
import org.burstsys.felt.model.literals.primitive.FeltFltPrimitive
import org.burstsys.felt.model.literals.primitive.FeltNullPrimitive
import org.burstsys.felt.model.literals.primitive.FeltStrPrimitive
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.HydraAnalysisGrammarBaseVisitor
import org.burstsys.hydra.grammar.HydraAnalysisGrammarParser
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

import scala.jdk.CollectionConverters._

/**
  * antlr parse driven builder for data (path, literals, etc) in AST
  */
trait HydraParseLiteralBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  literals
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitFixedLiteral(ctx: HydraAnalysisGrammarParser.FixedLiteralContext): FeltNode =
    new FeltFixPrimitive {
      global = HydraParseLiteralBldr.this.global
      final override val location = HydraLocation(HydraParseLiteralBldr.this.global, ctx)
      final override val value: Long = {
        val v = ctx.FIXED_LITERAL.getText.stripSuffix("L")
        v.toLong
      }
    }

  final override
  def visitFloatLiteral(ctx: HydraAnalysisGrammarParser.FloatLiteralContext): FeltNode =
    new FeltFltPrimitive {
      global = HydraParseLiteralBldr.this.global
      final override val location = HydraLocation(HydraParseLiteralBldr.this.global, ctx)
      final override val value: Double = ctx.FLOAT_LITERAL.getText.toDouble
    }

  final override
  def visitStringLiteral(ctx: HydraAnalysisGrammarParser.StringLiteralContext): FeltNode =
    new FeltStrPrimitive {
      global = HydraParseLiteralBldr.this.global
      final override val location = HydraLocation(HydraParseLiteralBldr.this.global, ctx)
      final override val value: String = extractStringLiteral(ctx.STRING_LITERAL())
    }

  final override
  def visitNullLiteral(ctx: HydraAnalysisGrammarParser.NullLiteralContext): FeltNode =
    new FeltNullPrimitive {
      global = HydraParseLiteralBldr.this.global
      final override val location = HydraLocation(HydraParseLiteralBldr.this.global, ctx)
    }

  final override
  def visitBooleanLiteral(ctx: HydraAnalysisGrammarParser.BooleanLiteralContext): FeltNode =
    new FeltBoolPrimitive {
      global = HydraParseLiteralBldr.this.global
      final override val location = HydraLocation(HydraParseLiteralBldr.this.global, ctx)
      final override val value: Boolean = ctx.BOOLEAN_LITERAL.getText.toBoolean
    }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Collection Literals
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitArrayLiteral(ctx: HydraAnalysisGrammarParser.ArrayLiteralContext): FeltNode =
    new FeltValArrLit {
      global = HydraParseLiteralBldr.this.global
      final override val location = HydraLocation(HydraParseLiteralBldr.this.global, ctx)
      final override val members: Array[FeltExpression] =
        ctx.expression.asScala.map(visit(_).asInstanceOf[FeltExpression]).toArray
    }

  final override
  def visitSetLiteral(ctx: HydraAnalysisGrammarParser.SetLiteralContext): FeltNode =
    new FeltValSetLit {
      global = HydraParseLiteralBldr.this.global
      final override val location = HydraLocation(HydraParseLiteralBldr.this.global, ctx)
      final override val members: Array[FeltExpression] =
        ctx.expression.asScala.map(visit(_).asInstanceOf[FeltExpression]).toArray
    }

  final override
  def visitMapLiteral(ctx: HydraAnalysisGrammarParser.MapLiteralContext): FeltNode =
    new FeltValMapLit {
      global = HydraParseLiteralBldr.this.global
      final override val location = HydraLocation(HydraParseLiteralBldr.this.global, ctx)
      final override val members: Array[FeltAssociation] =
        ctx.mapAssociation.asScala.map(visit(_).asInstanceOf[FeltAssociation]).toArray
    }

  final override
  def visitMapAssociation(ctx: HydraAnalysisGrammarParser.MapAssociationContext): FeltNode =
    new FeltAssociation {
      global = HydraParseLiteralBldr.this.global
      final override val location = HydraLocation(HydraParseLiteralBldr.this.global, ctx)
      final override val key: FeltExpression = {
        val ve = ctx.expression(0)
        visit(ve).asInstanceOf[FeltExpression]
      }
      final override val value: FeltExpression = {
        val ve = ctx.expression(1)
        visit(ve).asInstanceOf[FeltExpression]
      }
    }

}
