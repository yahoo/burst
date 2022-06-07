/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl
import org.burstsys.felt.model.variables.parameter.FeltParamDecl
import org.burstsys.felt.model.schema.decl.{FeltSchemaDecl, FeltSchemaExtension}
import org.burstsys.felt.model.tree.{FeltGlobal, FeltNode}
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.collectors.{HydraParseCubeBldr, HydraParseRouteBldr, HydraParseTabletBldr}
import org.burstsys.hydra.parser.builder.data.{HydraParseLiteralBldr, HydraParsePathBldr, HydraParseTypeBldr, HydraParseVarBldr}
import org.burstsys.hydra.parser.builder.expressions.{HydraParseUnImplBldr, _}
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.strings
import org.burstsys.vitals.time.VitalsTimeZones
import org.antlr.v4.runtime.tree.TerminalNode
import org.joda.time.DateTimeZone

import scala.collection.JavaConverters._
import scala.language.postfixOps

/**
 * antlr parse driven builder for top level analysis expression in AST
 */
final case
class HydraParseAnalysisBldr(global: FeltGlobal) extends HydraAnalysisGrammarBaseVisitor[FeltNode]
  with HydraParseRouteBldr with HydraParseTypeBldr with HydraParsePathBldr
  with HydraParseExprBldr with HydraParseCubeBldr with HydraParseVarBldr
  with HydraParseUnImplBldr with HydraParseVisitBuilder with HydraParseBoolExprBldr
  with HydraParseFuncBldr with HydraParseTabletBldr with HydraParseCondBldr
  with HydraParseMatchBldr with HydraParseAssignBldr with HydraParseExprBlockBldr
  with HydraParseMethodBuilder with HydraParseLiteralBldr with HydraInclusionExprBldr
  with HydraParseFrameBldr {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Analysis
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  override
  def visitAnalysisDeclaration(ctx: HydraAnalysisGrammarParser.AnalysisDeclarationContext): FeltNode = {

    // find the analysis name
    if (ctx.identifier == null)
      throw FeltException(HydraLocation(HydraParseAnalysisBldr.this.global, ctx), s"missing name for the analysis")

    val aName = extractIdentifier(ctx.identifier)

    // optional timezone property (default otherwise)
    val tzProperty: DateTimeZone = ctx.analysisProperty.asScala.find(_.TZ() != null) match {
      case None => VitalsTimeZones.BurstDefaultTimeZone
      case Some(p) =>
        val value = visit(p.expression).asInstanceOf[FeltExpression].reduceToStrAtomOrThrow.value
        try {
          DateTimeZone.forID(value)
        } catch safely {
          case t: IllegalArgumentException =>
            throw FeltException(HydraLocation(HydraParseAnalysisBldr.this.global, ctx), s"bad analysis time zone '$value'")
        }
    }

    new FeltAnalysisDecl {
      global = HydraParseAnalysisBldr.this.global
      final override val sourcePrefix: String = "hydra"
      final override val location = HydraLocation(HydraParseAnalysisBldr.this.global, ctx)
      final override val analysisName: String = aName
      final override val schemaDecl: FeltSchemaDecl = visit(ctx.schemaDeclaration).asInstanceOf[FeltSchemaDecl]
      final override val timezone: DateTimeZone = tzProperty
      final override val frames: Array[FeltFrameDecl] =
        ctx.frameDeclaration.asScala.map(visit(_).asInstanceOf[FeltFrameDecl]).toArray
      final override val parameters: Array[FeltParamDecl] =
        ctx.parameterDeclaration.asScala.map(visit(_).asInstanceOf[FeltParamDecl]).toArray
      final override val variables: Array[FeltGlobVarDecl] =
        ctx.globalVariableDeclaration.asScala.map(visit(_).asInstanceOf[FeltGlobVarDecl]).toArray
      global.analysis = this
      assertCollectors()
    }
  }

  override def visitSchemaDeclaration(ctx: HydraAnalysisGrammarParser.SchemaDeclarationContext): FeltSchemaDecl = {
    // optional schema property (default otherwise)
    val schemaPath: FeltPathExpr = if (ctx.pathExpression == null) {
      log info burstStdMsg(s"analysis missing schema specification using default '${global.brioSchema.name}'")
      new FeltPathExpr {
        global = HydraParseAnalysisBldr.this.global
        final override val components: Array[String] = global.brioSchema.name.split(".")
        final override val key: Option[FeltExpression] = None
      }
    } else {
      val path = visit(ctx.pathExpression).asInstanceOf[FeltPathExpr]
      path.isPassive = true
      global.bind(BrioSchema(path.fullPathAndKeyNoQuotes))
      path
    }
    new FeltSchemaDecl {
      global = HydraParseAnalysisBldr.this.global
      final override val location = HydraLocation(HydraParseAnalysisBldr.this.global, ctx)
      final override val schemaName: FeltPathExpr = schemaPath
      final override val schemaExtensions: Array[FeltSchemaExtension] =
        ctx.schemaExtension.asScala.map(visit(_).asInstanceOf[FeltSchemaExtension]).toArray
    }
  }

  override def visitSchemaExtension(ctx: HydraAnalysisGrammarParser.SchemaExtensionContext): FeltSchemaExtension = {
    new FeltSchemaExtension {
      global = HydraParseAnalysisBldr.this.global
      final override val location = HydraLocation(HydraParseAnalysisBldr.this.global, ctx)
      final override val schemaPath: FeltPathExpr =  visit(ctx.pathExpression(0)).asInstanceOf[FeltPathExpr]
      final override val schemaExtension: FeltPathExpr =  visit(ctx.pathExpression(1)).asInstanceOf[FeltPathExpr]
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  utility
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def extractIdentifier(identifier: HydraAnalysisGrammarParser.IdentifierContext): String = {
    identifier.IDENTIFIER.getText
  }

  def extractStringLiteral(literal: TerminalNode): String = strings.extractStringLiteral(literal.getText)

}
