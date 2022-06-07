/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.collectors

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.tablet.decl.{FeltTabletDecl, FeltTabletMembersDecl}
import org.burstsys.felt.model.types.FeltPrimTypeDecl
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr

/**
 * antlr parse driven builder for tablet collectors in AST
 */
trait HydraParseTabletBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  final override
  def visitTabletDeclaration(ctx: HydraAnalysisGrammarParser.TabletDeclarationContext): FeltNode =
    new FeltTabletDecl {
      global = HydraParseTabletBldr.this.global
      final override val location = HydraLocation(HydraParseTabletBldr.this.global, ctx)
      final override val typeDeclaration: FeltPrimTypeDecl = {
        if (ctx.valuePrimitiveTypeDeclaration() == null)
          throw FeltException(location, s"tablet missing type specification!")
        visit(ctx.valuePrimitiveTypeDeclaration()).asInstanceOf[FeltPrimTypeDecl]
      }
      final override val membersDecl: FeltTabletMembersDecl = new FeltTabletMembersDecl {
        final override val location = HydraLocation(HydraParseTabletBldr.this.global, ctx)
        global = HydraParseTabletBldr.this.global
      }

    }

}
