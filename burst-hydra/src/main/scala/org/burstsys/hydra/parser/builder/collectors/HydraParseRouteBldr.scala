/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.collectors

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.route._
import org.burstsys.felt.model.collectors.route.decl.graph.{FeltRouteGraph, FeltRouteStep, FeltRouteStepTo}
import org.burstsys.felt.model.collectors.route.decl.visit.{FeltRoutePathsDecl, FeltRouteStepsDecl}
import org.burstsys.felt.model.collectors.route.decl.{FeltRouteDecl, FeltRouteParameter}
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

import java.util
import scala.collection.JavaConverters._

/**
 * antlr parse driven builder for route collectors in AST
 */
trait HydraParseRouteBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  final override
  def visitRouteDeclaration(ctx: HydraAnalysisGrammarParser.RouteDeclarationContext): FeltNode = {
    new FeltRouteDecl {
      global = HydraParseRouteBldr.this.global
      final override val location = HydraLocation(HydraParseRouteBldr.this.global, ctx)
      final override val graph: FeltRouteGraph = {
        if (ctx.routeGraph == null) throw FeltException(location,
          s"route missing its graph structure"
        )
        else visit(ctx.routeGraph).asInstanceOf[FeltRouteGraph]
      }
      final override val parameters: Array[FeltRouteParameter] =
        ctx.routeParameter.asScala.map(visit(_).asInstanceOf[FeltRouteParameter]).toArray
      final override val pathsDecl: FeltRoutePathsDecl = new FeltRoutePathsDecl {
        final override val location = HydraLocation(HydraParseRouteBldr.this.global, ctx)
        global = HydraParseRouteBldr.this.global
        final override val stepsDecl: FeltRouteStepsDecl = new FeltRouteStepsDecl {
          final override val location = HydraLocation(HydraParseRouteBldr.this.global, ctx)
          global = HydraParseRouteBldr.this.global
        }
      }
    }
  }

  final override
  def visitRouteParameter(ctx: HydraAnalysisGrammarParser.RouteParameterContext): FeltNode =
    new FeltRouteParameter {
      global = HydraParseRouteBldr.this.global
      final override val location = HydraLocation(HydraParseRouteBldr.this.global, ctx)
      final override val parameterName: String =
        if (ctx.MAX_STEPS_PER_ROUTE != null) maxStepsPerRouteParameterName
        else if(ctx.MAX_PARTIAL_PATHS != null) maxPartialPathsParameterName
        else if(ctx.MAX_COMPLETE_PATHS != null) maxCompletePathsParameterName
        else if(ctx.MAX_PATH_TIME() != null) maxPathTimeParameterName
        else throw FeltException(location, s"FELT_ROUTE_UNKNOWN_PARAMETER")
      final override val value: FeltExpression =
        visit(ctx.expression).asInstanceOf[FeltExpression]
    }

  final override
  def visitRouteGraph(ctx: HydraAnalysisGrammarParser.RouteGraphContext): FeltNode =
    new FeltRouteGraph {
      global = HydraParseRouteBldr.this.global
      final override val location = HydraLocation(HydraParseRouteBldr.this.global, ctx)
      final override val steps: Array[FeltRouteStep] =
        ctx.routeStep.asScala.map(visit(_).asInstanceOf[FeltRouteStep]).toArray
    }

  final override
  def visitRouteStep(ctx: HydraAnalysisGrammarParser.RouteStepContext): FeltNode =
    new FeltRouteStep {
      global = HydraParseRouteBldr.this.global
      final override val location = HydraLocation(HydraParseRouteBldr.this.global, ctx)
      final override val traits: Array[StepTrait] = ctx.routeTrait.asScala.map {
        t =>
          if (t.BEGIN_TRAIT() != null) BeginStepTrait
          else if (t.TACIT_TRAIT() != null) TacitStepTrait
          else if (t.ENTER_TRAIT() != null) EnterStepTrait
          else if (t.EXIT_TRAIT() != null) ExitStepTrait
          else if (t.END_TRAIT() != null) EndStepTrait
          else if (t.COMPLETE_TRAIT() != null) CompleteStepTrait
          else null
      }.filter(_ != null).toArray
      final override val stepKey: FeltRouteStepKey = ctx.fixedLiteral.FIXED_LITERAL.getText.toInt
      final override val tos: Array[FeltRouteStepTo] =
        ctx.routeTo.asScala.map(visit(_).asInstanceOf[FeltRouteStepTo]).toArray
    }

  final override
  def visitRouteTo(ctx: HydraAnalysisGrammarParser.RouteToContext): FeltNode =
    new FeltRouteStepTo {
      global = HydraParseRouteBldr.this.global
      final override val location = HydraLocation(HydraParseRouteBldr.this.global, ctx)
      private val expression = ctx.expression.asScala
      final override val stepKey: FeltExpression =
        if (expression.nonEmpty)
          visit(ctx.expression(0)).asInstanceOf[FeltExpression] else null
      final override val minTime: Option[FeltExpression] =
        if (expression.length > 1)
          Some(visit(ctx.expression(1)).asInstanceOf[FeltExpression]) else None
      final override val maxTime: Option[FeltExpression] =
        if (expression.length > 2)
          Some(visit(ctx.expression(2)).asInstanceOf[FeltExpression]) else None
    }

}
