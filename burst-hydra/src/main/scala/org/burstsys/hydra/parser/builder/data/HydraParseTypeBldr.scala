/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.data

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.types.{FeltArrayTypeDecl, FeltMapTypeDecl, FeltPrimTypeDecl, FeltSetTypeDecl}
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr
import org.burstsys.vitals.errors.VitalsException

/**
 * antlr parse driven builder for type declarations in AST
 */
trait HydraParseTypeBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Type Declarations
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def visitValueMapTypeDeclaration(ctx: HydraAnalysisGrammarParser.ValueMapTypeDeclarationContext): FeltNode = {
    new FeltMapTypeDecl {
      global = HydraParseTypeBldr.this.global
      final override val location = HydraLocation(HydraParseTypeBldr.this.global, ctx)
      final override val valueType: BrioTypeKey = {
        if (ctx.valuePrimitiveTypeDeclaration.isEmpty) throw FeltException(location, s"value map declaration missing value type")
        visit(ctx.valuePrimitiveTypeDeclaration(0)).asInstanceOf[FeltPrimTypeDecl].valueType
      }
      final override val keyType: BrioTypeKey = {
        if (ctx.valuePrimitiveTypeDeclaration.size < 2) throw FeltException(location, s"value map declaration missing key type")
        visit(ctx.valuePrimitiveTypeDeclaration(1)).asInstanceOf[FeltPrimTypeDecl].valueType
      }
    }
  }

  final override
  def visitValueArrayTypeDeclaration(ctx: HydraAnalysisGrammarParser.ValueArrayTypeDeclarationContext): FeltNode = {
    new FeltArrayTypeDecl {
      global = HydraParseTypeBldr.this.global
      final override val location = HydraLocation(HydraParseTypeBldr.this.global, ctx)
      final override val valueType: BrioTypeKey = {
        if (ctx.valuePrimitiveTypeDeclaration == null)
          throw FeltException(location, s"value array declaration missing value type")
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
      }
    }
  }

  final override
  def visitValueSetTypeDeclaration(ctx: HydraAnalysisGrammarParser.ValueSetTypeDeclarationContext): FeltNode = {
    new FeltSetTypeDecl {
      global = HydraParseTypeBldr.this.global
      final override val location = HydraLocation(HydraParseTypeBldr.this.global, ctx)
      final override val valueType: BrioTypeKey = {
        if (ctx.valuePrimitiveTypeDeclaration == null)
          throw FeltException(location, s"value set declaration missing value type")
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
      }
    }
  }

  final override
  def visitValuePrimitiveTypeDeclaration(ctx: HydraAnalysisGrammarParser.ValuePrimitiveTypeDeclarationContext): FeltNode = {
    val vt = if (ctx.BOOLEAN_TYPE != null) BrioBooleanKey
    else if (ctx.BYTE_TYPE() != null) BrioByteKey
    else if (ctx.SHORT_TYPE() != null) BrioShortKey
    else if (ctx.INTEGER_TYPE() != null) BrioIntegerKey
    else if (ctx.LONG_TYPE() != null) BrioLongKey
    else if (ctx.DOUBLE_TYPE() != null) BrioDoubleKey
    else if (ctx.STRING_TYPE() != null) BrioStringKey
    else throw VitalsException(s"unknown primitive type")
    new FeltPrimTypeDecl {
      global = HydraParseTypeBldr.this.global
      final override val location = HydraLocation(HydraParseTypeBldr.this.global, ctx)
      final override val valueType: BrioTypeKey = vt
    }
  }

}
