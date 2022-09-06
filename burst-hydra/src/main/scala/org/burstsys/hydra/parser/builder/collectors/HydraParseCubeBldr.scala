/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser.builder.collectors

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation._
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive._
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.take._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.coerce._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.duration._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.grain._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.enum._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.split._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.ordinal._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.verbatim._
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.{FeltPathExpr, FeltSimplePath}
import org.burstsys.felt.model.types.FeltPrimTypeDecl
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarBaseVisitor, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.HydraLocation
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr
import org.burstsys.vitals.errors.VitalsException

import scala.jdk.CollectionConverters._

/**
 * antlr parse driven builder for cube collectors in AST
 */
trait HydraParseCubeBldr extends HydraAnalysisGrammarBaseVisitor[FeltNode] {

  self: HydraParseAnalysisBldr =>

  final override
  def visitCubeDeclaration(ctx: HydraAnalysisGrammarParser.CubeDeclarationContext): FeltNode = {
    val root: FeltCubeDecl = new FeltCubeDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      // required cube target
      final override val refTarget: FeltPathExpr =
        if (ctx.pathExpression != null) visit(ctx.pathExpression).asInstanceOf[FeltPathExpr] else null
      final override val aggregations: FeltCubeAggsNode = {
        if (ctx.cubeAggregations == null)
          new FeltCubeAggsNode {
            global = HydraParseCubeBldr.this.global
            final override val columns: Array[FeltCubeAggDecl] = Array.empty
          }
        else {
          val n = visit(ctx.cubeAggregations).asInstanceOf[FeltCubeAggsNode]
          n.cubeDecl = this
          n
        }
      }
      final override val dimensions: FeltCubeDimsNode = {
        if (ctx.cubeDimensions == null)
          new FeltCubeDimsNode {
            global = HydraParseCubeBldr.this.global
            final override val columns: Array[FeltCubeDimDecl] = Array.empty
          }
        else {
          val n = visit(ctx.cubeDimensions).asInstanceOf[FeltCubeDimsNode]
          n.cubeDecl = this
          n
        }
      }
      final override val subCubes: Array[FeltCubeDecl] =
        ctx.subCubeDeclaration.asScala.map(visit(_).asInstanceOf[FeltCubeDecl]).toArray
      final override val limit: Option[FeltExpression] = ctx.cubeProperty.asScala.find(_.LIMIT() != null) match {
        case None => None
        case Some(p) => Some(visit(p.expression).asInstanceOf[FeltExpression])
      }
    }
    root.setRootCube()
  }

  final override
  def visitSubCubeDeclaration(ctx: HydraAnalysisGrammarParser.SubCubeDeclarationContext): FeltNode = {
    new FeltCubeDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)

      final override val refTarget: FeltPathExpr = {
        if (ctx.pathExpression == null)
          throw FeltException(location, s"subcube missing refTarget parameter!")
        visit(ctx.pathExpression).asInstanceOf[FeltPathExpr]
      }
      final override val aggregations: FeltCubeAggsNode = {
        if (ctx.cubeAggregations == null) {
          new FeltCubeAggsNode {
            global = HydraParseCubeBldr.this.global
            final override val columns: Array[FeltCubeAggDecl] = Array.empty
          }
        } else {
          val children = ctx.cubeAggregations
          val n = visit(children).asInstanceOf[FeltCubeAggsNode]
          n.cubeDecl = this
          n
        }
      }
      final override val dimensions: FeltCubeDimsNode = {
        if (ctx.cubeDimensions == null)
          new FeltCubeDimsNode {
            global = HydraParseCubeBldr.this.global
            final override val columns: Array[FeltCubeDimDecl] = Array.empty
          }
        else {
          val children = ctx.cubeDimensions
          val n = visit(children).asInstanceOf[FeltCubeDimsNode]
          n.cubeDecl = this
          n
        }
      }
      final override val subCubes: Array[FeltCubeDecl] =
        ctx.subCubeDeclaration.asScala.map(visit(_).asInstanceOf[FeltCubeDecl]).toArray
    }
  }

  final override
  def visitCubeDimensions(ctx: HydraAnalysisGrammarParser.CubeDimensionsContext): FeltCubeDimsNode = {
    new FeltCubeDimsNode {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val columns: Array[FeltCubeDimDecl] =
        ctx.dimension.asScala.map {
          d =>
            visit(d) match {
              case dim: FeltCubeDimDecl => dim.reduceStatics.resolveTypes
              case unknown => if (unknown == null) throw FeltException(location, s"bad dimension")
              else throw FeltException(unknown.location, s"bad dimension $unknown")
            }
        }.toArray
    }
  }

  final override
  def visitCubeAggregations(ctx: HydraAnalysisGrammarParser.CubeAggregationsContext): FeltCubeAggsNode = {
    new FeltCubeAggsNode {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val columns: Array[FeltCubeAggDecl] =
        ctx.aggregate.asScala.map {
          a =>
            visit(a) match {
              case agg: FeltCubeAggDecl => agg.reduceStatics.resolveTypes
              case unknown => if (unknown == null) throw FeltException(location, s"bad aggregation")
              else throw FeltException(unknown.location, s"bad aggregation $unknown")
            }
        }.toArray
    }
  }

  final override
  def visitCastDimension(ctx: HydraAnalysisGrammarParser.CastDimensionContext): FeltNode =
    new FeltCubeDimCoerceDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
    }

  final override
  def visitVerbatimDimension(ctx: HydraAnalysisGrammarParser.VerbatimDimensionContext): FeltNode =
    new FeltCubeDimVerbatimDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
    }

  final override
  def visitDatetimeGrainDimension(ctx: HydraAnalysisGrammarParser.DatetimeGrainDimensionContext): FeltNode =
    new FeltCubeDimGrainDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val semanticType: FeltDimSemType =
        if (ctx.datetimeGrainType.YEAR_GRAIN() != null) YEAR_GRAIN_DIMENSION_SEMANTIC
        else if (ctx.datetimeGrainType().HALF_GRAIN() != null) HALF_GRAIN_DIMENSION_SEMANTIC
        else if (ctx.datetimeGrainType().QUARTER_GRAIN() != null) QUARTER_GRAIN_DIMENSION_SEMANTIC
        else if (ctx.datetimeGrainType().MONTH_GRAIN() != null) MONTH_GRAIN_DIMENSION_SEMANTIC
        else if (ctx.datetimeGrainType().WEEK_GRAIN() != null) WEEK_GRAIN_DIMENSION_SEMANTIC
        else if (ctx.datetimeGrainType().DAY_GRAIN() != null) DAY_GRAIN_DIMENSION_SEMANTIC
        else if (ctx.datetimeGrainType().HOUR_GRAIN() != null) HOUR_GRAIN_DIMENSION_SEMANTIC
        else if (ctx.datetimeGrainType().MINUTE_GRAIN() != null) MINUTE_GRAIN_DIMENSION_SEMANTIC
        else if (ctx.datetimeGrainType().SECOND_GRAIN() != null) SECOND_GRAIN_DIMENSION_SEMANTIC
        else throw VitalsException(s"unknown calendar grain ${ctx.datetimeGrainType().getText}")

    }

  final override
  def visitDatetimeOrdinalDimension(ctx: HydraAnalysisGrammarParser.DatetimeOrdinalDimensionContext): FeltNode =
    new FeltCubeDimOrdinalDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val semanticType: FeltDimSemType =
        if (ctx.datetimeOrdinalType().YEAR_OF_ERA_ORDINAL() != null) YEAR_OF_ERA_ORDINAL_DIMENSION_SEMANTIC
        else if (ctx.datetimeOrdinalType().MONTH_OF_YEAR_ORDINAL() != null) MONTH_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC
        else if (ctx.datetimeOrdinalType().WEEK_OF_YEAR_ORDINAL() != null) WEEK_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC
        else if (ctx.datetimeOrdinalType().DAY_OF_MONTH_ORDINAL() != null) DAY_OF_MONTH_ORDINAL_DIMENSION_SEMANTIC
        else if (ctx.datetimeOrdinalType().DAY_OF_WEEK_ORDINAL() != null) DAY_OF_WEEK_ORDINAL_DIMENSION_SEMANTIC
        else if (ctx.datetimeOrdinalType().DAY_OF_YEAR_ORDINAL() != null) DAY_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC
        else if (ctx.datetimeOrdinalType().HOUR_OF_DAY_ORDINAL() != null) HOUR_OF_DAY_ORDINAL_DIMENSION_SEMANTIC
        else if (ctx.datetimeOrdinalType().SECOND_OF_MINUTE_ORDINAL() != null) SECOND_OF_MINUTE_ORDINAL_DIMENSION_SEMANTIC
        else if (ctx.datetimeOrdinalType().MINUTE_OF_HOUR_ORDINAL() != null) MINUTE_OF_HOUR_ORDINAL_DIMENSION_SEMANTIC
        else throw VitalsException(s"unknown calendar ordinal ${ctx.datetimeOrdinalType().getText}")
    }

  final override
  def visitDatetimeDurationDimension(ctx: HydraAnalysisGrammarParser.DatetimeDurationDimensionContext): FeltNode =
    new FeltCubeDimDurationDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val semanticType: FeltDimSemType =
        if (ctx.datetimeDurationType().WEEK_DURATION() != null) WEEK_DURATION_DIMENSION_SEMANTIC
        else if (ctx.datetimeDurationType.DAY_DURATION() != null) DAY_DURATION_DIMENSION_SEMANTIC
        else if (ctx.datetimeDurationType.HOUR_DURATION() != null) HOUR_DURATION_DIMENSION_SEMANTIC
        else if (ctx.datetimeDurationType.MINUTE_DURATION() != null) MINUTE_DURATION_DIMENSION_SEMANTIC
        else if (ctx.datetimeDurationType.SECOND_DURATION() != null) SECOND_DURATION_DIMENSION_SEMANTIC
        else throw VitalsException(s"unknown time grain")
    }

  final override
  def visitSplitDimension(ctx: HydraAnalysisGrammarParser.SplitDimensionContext): FeltNode =
    new FeltCubeDimSplitDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
      final override val splitValues: Array[FeltExpression] =
        ctx.expression.asScala.map(visit(_).asInstanceOf[FeltExpression]).toArray
    }

  final override
  def visitEnumDimension(ctx: HydraAnalysisGrammarParser.EnumDimensionContext): FeltNode =
    new FeltCubeDimEnumDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
      final override val enumValues: Array[FeltExpression] =
        ctx.expression.asScala.map(visit(_).asInstanceOf[FeltExpression]).toArray

    }

  override
  def visitMaxAggregate(ctx: HydraAnalysisGrammarParser.MaxAggregateContext): FeltCubeAggMaxDecl =
    new FeltCubeAggMaxDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
    }

  override
  def visitMinAggregate(ctx: HydraAnalysisGrammarParser.MinAggregateContext): FeltCubeAggMinDecl =
    new FeltCubeAggMinDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
    }

  override
  def visitProjectAggregate(ctx: HydraAnalysisGrammarParser.ProjectAggregateContext): FeltCubeAggProjectDecl =
    new FeltCubeAggProjectDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
    }

  override
  def visitSumAggregate(ctx: HydraAnalysisGrammarParser.SumAggregateContext): FeltCubeAggSumDecl =
    new FeltCubeAggSumDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
    }

  override
  def visitUniqueAggregate(ctx: HydraAnalysisGrammarParser.UniqueAggregateContext): FeltCubeAggUniqueDecl =
    new FeltCubeAggUniqueDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
    }


  override
  def visitTopAggregate(ctx: HydraAnalysisGrammarParser.TopAggregateContext): FeltNode =
    new FeltCubeAggTopDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
      final override val topValues: Array[FeltExpression] =
        ctx.expression.asScala.map(visit(_).asInstanceOf[FeltExpression]).toArray
    }


  override
  def visitBottomAggregate(ctx: HydraAnalysisGrammarParser.BottomAggregateContext): FeltNode =
    new FeltCubeAggBottomDecl {
      global = HydraParseCubeBldr.this.global
      final override val location = HydraLocation(HydraParseCubeBldr.this.global, ctx)
      final override val refName: FeltPathExpr = FeltSimplePath(extractIdentifier(ctx.identifier))
      final override val valueType: BrioTypeKey =
        visit(ctx.valuePrimitiveTypeDeclaration).asInstanceOf[FeltPrimTypeDecl].valueType
      final override val topValues: Array[FeltExpression] =
        ctx.expression.asScala.map(visit(_).asInstanceOf[FeltExpression]).toArray
    }
}
