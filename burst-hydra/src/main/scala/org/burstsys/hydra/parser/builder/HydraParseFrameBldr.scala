/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder

import java.util.concurrent.atomic.AtomicInteger
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.collectors.decl.FeltCollectorDecl
import org.burstsys.felt.model.visits.decl.{FeltDynamicVisitDecl, FeltStaticVisitDecl, FeltVisitDecl}
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.time.VitalsTimeZones
import org.joda.time.DateTimeZone

import scala.collection.JavaConverters._

/**
 * antlr parse driven builder for FELT frames in AST
 */
trait HydraParseFrameBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  private[this]
  val _frameIdGenerator = new AtomicInteger(0)

  override def visitFrameDeclaration(ctx: HydraAnalysisGrammarParser.FrameDeclarationContext): FeltNode = {
    new FeltFrameDecl {
      global = HydraParseFrameBldr.this.global
      final override val location: FeltLocation = HydraLocation(HydraParseFrameBldr.this.global, ctx)
      final override val frameId: Int = _frameIdGenerator.getAndIncrement
      final override val timezone: DateTimeZone = ctx.frameProperty.asScala.find(_.TZ() != null) match {
        case None => VitalsTimeZones.BurstDefaultTimeZone
        case Some(p) =>
          val value = visit(p.expression).asInstanceOf[FeltExpression].reduceToStrAtomOrThrow.value
          try {
            DateTimeZone.forID(value)
          } catch safely {
            case t: IllegalArgumentException => throw FeltException(
              HydraLocation(HydraParseFrameBldr.this.global, ctx),
              s"bad frame time zone '$value'"
            )
          }
      }
      final override val variables: Array[FeltGlobVarDecl] = ctx.globalVariableDeclaration.asScala.map(visit(_).asInstanceOf[FeltGlobVarDecl]).toArray
      final override val visits: Array[FeltVisitDecl] =
        (ctx.staticVisitDeclaration.asScala.map(visit(_).asInstanceOf[FeltStaticVisitDecl]) ++
          ctx.dynamicVisitDeclaration.asScala.map(visit(_).asInstanceOf[FeltDynamicVisitDecl])).toArray
      final override val frameName: String = extractIdentifier(ctx.identifier)
      final override val collectorDecl: FeltCollectorDecl[_, _] = {
        val declaration = if (ctx.cubeDeclaration != null)
          ctx.cubeDeclaration
        else if (ctx.routeDeclaration != null)
          ctx.routeDeclaration
        else if (ctx.tabletDeclaration != null)
          ctx.tabletDeclaration
        else throw FeltException(location, s"no collector declaration")
        visit(declaration).asInstanceOf[FeltCollectorDecl[_, _]]
      }
      assertFrame()
    }
  }

}
